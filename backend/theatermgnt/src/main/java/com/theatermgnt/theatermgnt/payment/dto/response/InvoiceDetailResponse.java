package com.theatermgnt.theatermgnt.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.theatermgnt.theatermgnt.booking.dto.response.BookingSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailResponse {
    private String id;
    private String bookingId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private BookingSummaryResponse bookingDetails;
}
