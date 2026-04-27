package com.theatermgnt.theatermgnt.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvoiceRefundedEvent {
    private String invoiceId;
    private String bookingId;
}
