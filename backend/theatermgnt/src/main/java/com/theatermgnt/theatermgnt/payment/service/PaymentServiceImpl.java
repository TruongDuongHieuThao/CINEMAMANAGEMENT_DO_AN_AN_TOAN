package com.theatermgnt.theatermgnt.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.booking.service.BookingService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentDetailsResponse;
import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.entity.PaymentType;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentMethodRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;
import com.theatermgnt.theatermgnt.revenue.service.RevenueAggregationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BookingService bookingService;
    private final RevenueAggregationService revenueAggregationService;

    @Override
    @Transactional
    public PaymentDetailsResponse processCashPayment(String invoiceId) {
        try {
            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));

            if (invoice.getStatus() == InvoiceStatus.PAID) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
            }

            var cashMethod = paymentMethodRepository.findByName("Cash").orElseThrow(() -> {
                log.error("Cash payment method not found in database");
                return new AppException(ErrorCode.PAYMENT_METHOD_NOT_EXISTED);
            });

            String txnRef = "CASH" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

            Payment payment = Payment.builder()
                    .invoiceId(invoiceId)
                    .paymentMethodId(cashMethod.getId())
                    .amount(invoice.getTotalAmount())
                    .paymentType(PaymentType.BOOKING)
                    .transactionCode(txnRef)
                    .status(PaymentStatus.SUCCESS)
                    .paymentDate(LocalDateTime.now())
                    .description("Cash payment for invoice: " + invoiceId)
                    .build();
            paymentRepository.save(payment);

            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            try {
                bookingService.confirmBookingPayment(invoice.getBookingId());
            } catch (Exception e) {
                log.error("Error confirming booking {} after cash payment", invoice.getBookingId(), e);
                throw e;
            }

            try {
                revenueAggregationService.processPaymentForRevenue(payment);
            } catch (Exception e) {
                log.error("Error aggregating revenue for payment {}", payment.getId(), e);
            }

            return PaymentDetailsResponse.builder()
                    .code("00")
                    .message("Cash payment successful")
                    .id(payment.getId())
                    .transactionCode(txnRef)
                    .invoiceId(invoiceId)
                    .amount(invoice.getTotalAmount())
                    .status(PaymentStatus.SUCCESS.name())
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing cash payment", e);
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }
    }
}
