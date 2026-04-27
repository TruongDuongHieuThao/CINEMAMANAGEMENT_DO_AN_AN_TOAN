package com.theatermgnt.theatermgnt.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;

import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String invoiceId; // Foreign key to Invoice

    @Column(nullable = false)
    private String paymentMethodId; // Foreign key to PaymentMethod

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // VND amount

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType; // booking, refund

    @Column(unique = true)
    private String transactionCode; // VNPay txnRef or bank transaction ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String description;

    private LocalDateTime paymentDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
