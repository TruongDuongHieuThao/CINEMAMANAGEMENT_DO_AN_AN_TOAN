package com.theatermgnt.theatermgnt.payment.service;

import java.util.Map;

import com.theatermgnt.theatermgnt.payment.dto.response.PaymentDetailsResponse;

public interface PaymentService {

    /**
     * Process cash payment for invoice
     */
    PaymentDetailsResponse processCashPayment(String invoiceId);
}
