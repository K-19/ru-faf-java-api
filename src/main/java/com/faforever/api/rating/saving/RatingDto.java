package com.faforever.api.rating.saving;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class RatingDto {
    public String leaderboard;
    public List<LeaderboardRatingDto> users;
}
