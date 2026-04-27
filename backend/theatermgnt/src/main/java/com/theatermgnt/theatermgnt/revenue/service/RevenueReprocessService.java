package com.theatermgnt.theatermgnt.revenue.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueReprocessService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RevenueAggregationService revenueAggregationService;

    @Transactional
    public void reprocessAllPayments() {
        log.info("Starting revenue reprocessing for all payments...");

        List<Payment> successfulPayments = paymentRepository.findByStatusIn(List.of(PaymentStatus.SUCCESS));

        log.info("Found {} payments to reprocess", successfulPayments.size());

        int successCount = 0;
        int failCount = 0;

        for (Payment payment : successfulPayments) {
            try {
                revenueAggregationService.processPaymentForRevenue(payment);
                successCount++;

                if (successCount % 100 == 0) {
                    log.info("Processed {} payments so far...", successCount);
                }
            } catch (Exception e) {
                failCount++;
                log.error("Failed to reprocess payment {}: {}", payment.getId(), e.getMessage());
            }
        }

        // Process refunded invoices to subtract revenue
        log.info("Processing refunded invoices...");
        List<Invoice> refundedInvoices = invoiceRepository.findByStatus(InvoiceStatus.REFUNDED);

        log.info("Found {} refunded invoices to process", refundedInvoices.size());

        int refundCount = 0;
        int refundFailCount = 0;

        for (Invoice invoice : refundedInvoices) {
            try {
                revenueAggregationService.processInvoiceRefundForRevenue(invoice.getId());
                refundCount++;

                if (refundCount % 100 == 0) {
                    log.info("Processed {} refunded invoices so far...", refundCount);
                }
            } catch (Exception e) {
                refundFailCount++;
                log.error("Failed to process refund for invoice {}: {}", invoice.getId(), e.getMessage());
            }
        }

        log.info(
                "Revenue reprocessing completed. Payments - Success: {}, Failed: {}. Refunds - Success: {}, Failed: {}",
                successCount,
                failCount,
                refundCount,
                refundFailCount);
    }
}
