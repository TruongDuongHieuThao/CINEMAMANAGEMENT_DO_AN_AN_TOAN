package com.theatermgnt.theatermgnt.common.dto.response;

import java.time.LocalDate;

import com.theatermgnt.theatermgnt.common.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseUserResponse {
    /// Abstract class to be extended by CustomerResponse and StaffResponse
    String accountId;
    String accountType;
    String username;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    String address;
    Gender gender;
    LocalDate dob;
}
