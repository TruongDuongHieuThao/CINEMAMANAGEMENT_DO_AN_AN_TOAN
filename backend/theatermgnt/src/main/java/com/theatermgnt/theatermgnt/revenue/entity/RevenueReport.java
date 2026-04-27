package com.theatermgnt.theatermgnt.revenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.theatermgnt.theatermgnt.revenue.enums.ReportType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "revenue_reports")
public class RevenueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String cinemaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ReportType reportType;

    @Column(nullable = false)
    LocalDate startDate;

    @Column(nullable = false)
    LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal totalTicketRevenue;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal totalComboRevenue;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal netRevenue;

    @Column(nullable = false)
    LocalDateTime generatedAt;
}
