package com.theatermgnt.theatermgnt.account.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import com.theatermgnt.theatermgnt.common.enums.Gender;
import com.theatermgnt.theatermgnt.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseAccountCreationRequest {
    @Size(min = 3, message = "INVALID_USERNAME")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    String email;

    @Pattern(regexp = "^0[0-9]{9}$", message = "INVALID_PHONE_NUMBER_FORMAT")
    String phoneNumber;

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @Size(max = 50, message = "FIRST_NAME_TOO_LONG")
    String firstName;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @Size(max = 50, message = "LAST_NAME_TOO_LONG")
    String lastName;

    @Size(max = 200, message = "ADDRESS_TOO_LONG")
    String address;

    @NotNull(message = "GENDER_REQUIRED")
    Gender gender;

    @DobConstraint(min = 6, message = "INVALID_DOB")
    @NotNull(message = "DOB_REQUIRED")
    LocalDate dob;
}
