package com.theatermgnt.theatermgnt.movie.dto.response;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.theatermgnt.theatermgnt.common.enums.MovieStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Response đơn giản cho danh sách phim
 * Chỉ chứa thông tin cần thiết để hiển thị trong list/card view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieSimpleResponse {

    String id;
    String title;
    String slug;
    String posterUrl;
    String trailerUrl;

    Integer durationMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate releaseDate;

    MovieStatus status;
    String ageRatingCode;
    String director;
    Set<GenreInfo> genres;
    Boolean needsArchiveWarning;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GenreInfo {
        String id;
        String name;
    }
}
