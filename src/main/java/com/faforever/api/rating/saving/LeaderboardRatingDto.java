package com.faforever.api.rating.saving;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
public class LeaderboardRatingDto {
  public Long place;
  public Integer userId;
  public String login;
  public Double mean;
  public Double rating;
  public Integer totalGames;
  public Integer wonGames;
}
