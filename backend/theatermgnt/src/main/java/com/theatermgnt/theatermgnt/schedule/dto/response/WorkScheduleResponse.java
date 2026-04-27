package com.theatermgnt.theatermgnt.schedule.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleResponse {
    String id;
    String userId;
    String userName;
    String shiftTypeId;
    String shiftTypeName;
    LocalTime shiftStart;
    LocalTime shiftEnd;
    LocalDate workDate;
}
