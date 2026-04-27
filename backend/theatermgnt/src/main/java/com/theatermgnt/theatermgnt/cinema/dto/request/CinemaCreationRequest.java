package com.theatermgnt.theatermgnt.cinema.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CinemaCreationRequest {

    @Size(min = 3, message = "CINEMA_NAME_INVALID")
    String name;

    @Size(min = 3, message = "CINEMA_ADDRESS_INVALID")
    String address;

    @Size(min = 3, message = "CINEMA_CITY_INVALID")
    String city;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "CINEMA_PHONE_NUMBER_INVALID")
    String phoneNumber;

    Integer buffer;

    String managerId;
}
