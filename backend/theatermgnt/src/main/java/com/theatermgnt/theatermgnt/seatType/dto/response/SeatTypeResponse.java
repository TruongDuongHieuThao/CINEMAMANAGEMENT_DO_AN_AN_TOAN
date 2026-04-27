package com.theatermgnt.theatermgnt.seatType.dto.response;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatTypeResponse {
    String id;
    String typeName;
    BigDecimal basePriceModifier;
}
