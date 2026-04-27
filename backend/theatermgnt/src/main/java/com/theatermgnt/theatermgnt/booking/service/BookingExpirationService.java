package com.theatermgnt.theatermgnt.booking.service;

import java.time.Instant;
import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingExpirationService {
    private static final Logger logger = LoggerFactory.getLogger(BookingExpirationService.class);

    private final BookingRepository bookingRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedDelay = 10_000) // mỗi 10 giây
    @Transactional
    public void expireBookings() {

        Instant now = Instant.now();

        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        logger.info("Found {} expired bookings to process", expiredBookings.size());

        for (Booking booking : expiredBookings) {

            // 1. Update booking status
            booking.setStatus(BookingStatus.EXPIRED);

            // 2. Release seats
            screeningSeatRepository.releaseSeatsByBooking(booking.getId().toString());

            // 3. Update invoice status to FAILED
            invoiceRepository.findByBookingId(booking.getId().toString()).ifPresent(invoice -> {
                if (invoice.getStatus() == InvoiceStatus.PENDING) {
                    invoice.setStatus(InvoiceStatus.FAILED);
                    invoiceRepository.save(invoice);
                    logger.info(
                            "Updated invoice {} to FAILED for expired booking {}", invoice.getId(), booking.getId());

                    // 4. Update payment status to FAILED
                    List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
                    for (Payment payment : payments) {
                        if (payment.getStatus() == PaymentStatus.PENDING) {
                            payment.setStatus(PaymentStatus.FAILED);
                            paymentRepository.save(payment);
                            logger.info(
                                    "Updated payment {} to FAILED for expired booking {}",
                                    payment.getId(),
                                    booking.getId());
                        }
                    }
                }
            });
        }

        bookingRepository.saveAll(expiredBookings);
        logger.info("Successfully processed {} expired bookings", expiredBookings.size());
    }
}
