package com.theatermgnt.theatermgnt.revenue.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyRevenueCreateRequest {

    @NotBlank
    String cinemaId;

    @NotNull
    LocalDate reportDate;

    @NotNull
    BigDecimal ticketRevenue;

    @NotNull
    BigDecimal comboRevenue;

    @NotNull
    BigDecimal netRevenue;

    @NotNull
    Integer totalTransactions;
}
