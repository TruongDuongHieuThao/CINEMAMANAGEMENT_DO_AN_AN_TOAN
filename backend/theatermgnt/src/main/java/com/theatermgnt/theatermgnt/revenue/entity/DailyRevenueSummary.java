package com.theatermgnt.theatermgnt.revenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "daily_revenue_summary")
public class DailyRevenueSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String cinemaId;

    @Column(nullable = false)
    LocalDate reportDate;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal ticketRevenue;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal comboRevenue;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal netRevenue;

    @Column(nullable = false)
    Integer totalTransactions;
}
