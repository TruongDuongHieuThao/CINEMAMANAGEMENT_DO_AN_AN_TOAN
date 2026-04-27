package com.theatermgnt.theatermgnt.ShiftType.dto.request;

import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateShiftTypeRequest {

    String name;
    LocalTime startTime;
    LocalTime endTime;
}
