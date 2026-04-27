package com.theatermgnt.theatermgnt.revenue.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.theatermgnt.theatermgnt.revenue.enums.ReportType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueReportResponse {
    String id;
    String cinemaId;
    ReportType reportType;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal totalTicketRevenue;
    BigDecimal totalComboRevenue;
    BigDecimal netRevenue;

    LocalDateTime generatedAt;
}
