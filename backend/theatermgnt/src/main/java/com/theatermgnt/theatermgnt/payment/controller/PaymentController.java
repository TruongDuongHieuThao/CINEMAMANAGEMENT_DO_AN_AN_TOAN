package com.theatermgnt.theatermgnt.payment.controller;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentDetailsResponse;
import com.theatermgnt.theatermgnt.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process cash payment for invoice
     * POST /api/theater-mgnt/payment/cash/{invoiceId}
     */
    @PostMapping("/cash/{invoiceId}")
    public ApiResponse<PaymentDetailsResponse> processCashPayment(@PathVariable String invoiceId) {
        log.info("Processing cash payment for invoice: {}", invoiceId);
        PaymentDetailsResponse response = paymentService.processCashPayment(invoiceId);
        return ApiResponse.<PaymentDetailsResponse>builder().result(response).build();
    }
}
