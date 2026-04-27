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
public class MovieRevenueResponse {
    String id;
    String movieId;
    String cinemaId;
    LocalDate reportDate;
    Integer totalTicketsSold;
    BigDecimal totalRevenue;
}
