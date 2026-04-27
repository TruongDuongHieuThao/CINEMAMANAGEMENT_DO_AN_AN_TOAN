package com.theatermgnt.theatermgnt.revenue.entity;

import java.time.LocalDateTime;

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
@Table(
        name = "revenue_processing_log",
        indexes = {@Index(name = "idx_payment_id", columnList = "paymentId", unique = true)})
public class RevenueProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String paymentId;

    @Column(nullable = false)
    String paymentStatus; // SUCCESS, REFUNDED

    @Column(nullable = false)
    LocalDateTime processedAt;

    String errorMessage;
}
