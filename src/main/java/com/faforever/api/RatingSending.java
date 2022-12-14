package com.faforever.api;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.rating.saving.LeaderboardRatingRepository;
import com.faforever.api.rating.saving.ServerRatingSaver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RatingSending {

  private final FafApiProperties properties;

  public RatingSending(FafApiProperties properties) {
    this.properties = properties;
  }

  @Bean
  CommandLineRunner sendingRating(LeaderboardRatingRepository leaderboardRatingRepository) {
    return args -> {
      String urlForSendRating = properties.getRating().getUrlForSendRating();
      int sendingPeriod = properties.getRating().getSendingPeriod();
      ServerRatingSaver saver = new ServerRatingSaver(leaderboardRatingRepository, urlForSendRating, sendingPeriod);
      saver.start();
    };
  }
}
