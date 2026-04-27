package com.theatermgnt.theatermgnt.payment.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatisticsResponse {
    private Long totalInvoices;
    private Long pendingInvoices;
    private Long paidInvoices;
    private Long failedInvoices;
    private Long refundedInvoices;
    private BigDecimal totalRevenue;
    private BigDecimal pendingAmount;
    private BigDecimal refundedAmount;
}
