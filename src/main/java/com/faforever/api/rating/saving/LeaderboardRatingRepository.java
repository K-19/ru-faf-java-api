package com.faforever.api.rating.saving;

import com.faforever.api.data.domain.LeaderboardRating;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRatingRepository extends CrudRepository<LeaderboardRating, Integer> {
}
