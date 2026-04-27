package com.theatermgnt.theatermgnt.priceConfig.dto.response;

import java.math.BigDecimal;

import com.theatermgnt.theatermgnt.common.enums.DayType;
import com.theatermgnt.theatermgnt.common.enums.TimeSlot;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceConfigResponse {
    String id;
    DayType dayType;
    TimeSlot timeSlot;
    BigDecimal price;

    String seatTypeName;
}
