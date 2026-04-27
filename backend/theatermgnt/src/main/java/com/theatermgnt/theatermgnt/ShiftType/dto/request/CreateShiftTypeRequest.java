package com.theatermgnt.theatermgnt.ShiftType.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateShiftTypeRequest {

    @NotBlank
    String name;

    @NotNull
    LocalTime startTime;

    @NotNull
    LocalTime endTime;
}
