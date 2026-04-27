package com.theatermgnt.theatermgnt.ShiftType.dto.response;

import java.time.LocalTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftTypeResponse {

    String id;
    String cinemaId;

    String name;
    LocalTime startTime;
    LocalTime endTime;
    boolean deleted;
}
