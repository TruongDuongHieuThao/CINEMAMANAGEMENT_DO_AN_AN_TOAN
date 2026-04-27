package com.theatermgnt.theatermgnt.movie.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgeRatingResponse {
    String id;
    String code;
    String description;
}
