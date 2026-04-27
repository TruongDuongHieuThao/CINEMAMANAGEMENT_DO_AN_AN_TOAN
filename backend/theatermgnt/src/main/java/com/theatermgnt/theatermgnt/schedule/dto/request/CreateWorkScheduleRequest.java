package com.theatermgnt.theatermgnt.schedule.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWorkScheduleRequest {

    List<String> userIds;

    @NotBlank
    String shiftTypeId;

    @NotNull
    LocalDate workDate;
}
