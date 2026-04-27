package com.theatermgnt.theatermgnt.review.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotBlank(message = "CUSTOMER_ID_REQUIRED")
    String customerId;

    @NotBlank(message = "MOVIE_ID_REQUIRED")
    String movieId;

    String screeningId;

    @NotNull(message = "RATING_REQUIRED")
    @DecimalMin(value = "0.5", message = "RATING_MIN_0_5")
    @DecimalMax(value = "10.0", message = "RATING_MAX_10")
    BigDecimal rating;

    @Size(max = 5000, message = "COMMENT_TOO_LONG")
    String comment;

    Boolean isSpoiler = false;
}
