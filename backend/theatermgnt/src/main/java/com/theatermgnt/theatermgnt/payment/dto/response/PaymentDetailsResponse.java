package com.theatermgnt.theatermgnt.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {

    private String id;
    private String invoiceId;
    private String paymentMethodId;
    private BigDecimal amount;
    private String paymentType;
    private String transactionCode;
    private String status;
    private String description;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;

    // For VNPay redirect
    private String paymentUrl;
    private String code;
    private String message;
}
