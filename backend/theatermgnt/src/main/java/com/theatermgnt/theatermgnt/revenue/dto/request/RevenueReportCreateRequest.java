package com.theatermgnt.theatermgnt.revenue.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.theatermgnt.theatermgnt.revenue.enums.ReportType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportCreateRequest {

    @NotBlank
    String cinemaId;

    @NotNull
    ReportType reportType;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate endDate;

    @NotNull
    BigDecimal totalTicketRevenue;

    @NotNull
    BigDecimal totalComboRevenue;

    @NotNull
    BigDecimal netRevenue;
}
