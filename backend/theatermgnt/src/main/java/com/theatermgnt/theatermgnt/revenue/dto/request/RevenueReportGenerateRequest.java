package com.theatermgnt.theatermgnt.revenue.dto.request;

import java.time.LocalDate;

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
public class RevenueReportGenerateRequest {

    String cinemaId;

    @NotNull
    ReportType reportType;

    LocalDate startDate;

    LocalDate endDate;
}
