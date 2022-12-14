package com.faforever.api.rating.saving;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class DataDto {
    public List<RatingDto> rating;
}
