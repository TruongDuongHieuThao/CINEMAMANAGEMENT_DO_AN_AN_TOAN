package com.theatermgnt.theatermgnt.screening.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningResponse {
    String id;
    String movieId;
    String movieName;
    String roomId;
    String roomName;
    String cinemaId;
    String cinemaName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String status;
}
