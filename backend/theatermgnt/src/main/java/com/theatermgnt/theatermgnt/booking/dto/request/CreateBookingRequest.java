package com.theatermgnt.theatermgnt.booking.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookingRequest {
    String customerId;
    String screeningId;
    List<String> screeningSeatIds;

    String customerName;
    String firstName;
    String lastName;
    String email;
}
