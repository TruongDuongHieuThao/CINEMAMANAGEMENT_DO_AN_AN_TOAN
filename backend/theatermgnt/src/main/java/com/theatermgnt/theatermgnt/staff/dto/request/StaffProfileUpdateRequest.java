package com.theatermgnt.theatermgnt.staff.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.theatermgnt.theatermgnt.common.enums.Gender;
import com.theatermgnt.theatermgnt.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffProfileUpdateRequest {
    @Size(max = 50, message = "FIRST_NAME_TOO_LONG")
    String firstName;

    @Size(max = 50, message = "LAST_NAME_TOO_LONG")
    String lastName;

    @Pattern(regexp = "^0[0-9]{9}$", message = "INVALID_PHONE_NUMBER_FORMAT")
    String phoneNumber;

    String jobTitle;
    String cinemaId;

    @Size(max = 200, message = "ADDRESS_TOO_LONG")
    String address;

    String avatarUrl;
    Gender gender;

    @DobConstraint(min = 6, message = "INVALID_DOB")
    LocalDate dob;

    List<String> roles;
}
