package com.theatermgnt.theatermgnt.review.dto.response;

import java.math.BigDecimal;
import java.util.Map;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieRatingStatsResponse {

    String movieId;
    BigDecimal averageRating;
    Long totalReviews;
    Map<BigDecimal, Long> ratingDistribution;
}
