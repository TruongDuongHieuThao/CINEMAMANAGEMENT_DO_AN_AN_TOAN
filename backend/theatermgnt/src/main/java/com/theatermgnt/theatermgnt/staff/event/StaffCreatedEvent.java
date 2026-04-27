package com.theatermgnt.theatermgnt.staff.event;

import com.theatermgnt.theatermgnt.staff.entity.Staff;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Builder
public class StaffCreatedEvent {
    Staff staff;
    String rawPassword;
}
