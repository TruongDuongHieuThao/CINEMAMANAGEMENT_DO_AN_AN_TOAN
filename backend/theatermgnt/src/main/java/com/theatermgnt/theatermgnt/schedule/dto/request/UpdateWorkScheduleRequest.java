package com.theatermgnt.theatermgnt.schedule.dto.request;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWorkScheduleRequest {
    String shiftTypeId;
    LocalDate workDate;
}
