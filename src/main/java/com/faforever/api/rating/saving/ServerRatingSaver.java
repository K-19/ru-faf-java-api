package com.faforever.api.rating.saving;

import com.faforever.api.data.domain.LeaderboardRating;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ServerRatingSaver extends Thread {

  private final LeaderboardRatingRepository leaderboardRatingRepository;
  private final String urlForSendRating;
  private final int sendingPeriod;

  public ServerRatingSaver(LeaderboardRatingRepository leaderboardRatingRepository, String urlForSendRating, int sendingPeriod) {
    super();
    this.leaderboardRatingRepository = leaderboardRatingRepository;
    this.urlForSendRating = urlForSendRating;
    this.sendingPeriod = sendingPeriod;
  }

  @Override
  public void run() {
    while(true) {
      Map<String, List<LeaderboardRating>> map = new TreeMap<>();
      Iterable<LeaderboardRating> ratingList = leaderboardRatingRepository.findAll();
      for (LeaderboardRating rating : ratingList) {
        String leaderboardName = rating.getLeaderboard().getTechnicalName();
        if (!map.containsKey(leaderboardName)) {
          map.put(leaderboardName, new ArrayList<>());
        }
        map.get(leaderboardName).add(rating);
      }
      try {
        DataDto dataDto = new DataDto(new ArrayList<>());
        for (String leaderboard : map.keySet()) {
          RatingDto ratingDto = new RatingDto(leaderboard, new ArrayList<>());
          map.get(leaderboard).sort(Comparator.comparing(LeaderboardRating::getRating).reversed());
          long place = 1L;
          for (LeaderboardRating rating : map.get(leaderboard)) {
            LeaderboardRatingDto dto = LeaderboardRatingDto.builder()
              .place(place++)
              .mean(rating.getMean())
              .userId(rating.getPlayer().getId())
              .login(rating.getPlayer().getLogin())
              .rating(rating.getRating())
              .totalGames(rating.getTotalGames())
              .wonGames(rating.getWonGames())
              .build();
            ratingDto.getUsers().add(dto);
          }
          dataDto.getRating().add(ratingDto);
        }
        sendRating(dataDto);

        sleep(sendingPeriod * 1000L);

      } catch (InterruptedException e) {
        log.info("Rating sending thread interrupted.");
        e.printStackTrace();
      }
    }
  }

  private void sendRating(DataDto data) {
    log.info("===================================");
    log.info("Sending player rating to the server: " + urlForSendRating);

    try {
      URL url = new URL(urlForSendRating);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(data);
      log.info(json);

      OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
      osw.write(json);
      osw.flush();

      int responseCode = connection.getResponseCode();
      log.info("Rating sending completed. Server response code: " + responseCode);
      log.info("Next rating send expected in " + sendingPeriod + " seconds");

      osw.close();
    } catch (IOException e) {
      log.info("Report sending failed. Error message: " + e.getMessage());
      e.printStackTrace();
    } finally {
      log.info("===================================");
    }
  }
}
