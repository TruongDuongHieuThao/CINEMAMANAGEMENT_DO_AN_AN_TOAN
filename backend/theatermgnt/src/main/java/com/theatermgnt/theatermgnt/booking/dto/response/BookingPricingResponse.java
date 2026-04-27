package com.theatermgnt.theatermgnt.booking.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingPricingResponse {
    UUID bookingId;
    BigDecimal subTotal;
}
