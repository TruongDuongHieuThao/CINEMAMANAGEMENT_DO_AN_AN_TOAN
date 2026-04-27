package com.theatermgnt.theatermgnt.revenue.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyRevenueResponse {
    String id;
    String cinemaId;
    LocalDate reportDate;
    BigDecimal ticketRevenue;
    BigDecimal comboRevenue;
    BigDecimal netRevenue;
    Integer totalTransactions;
}
