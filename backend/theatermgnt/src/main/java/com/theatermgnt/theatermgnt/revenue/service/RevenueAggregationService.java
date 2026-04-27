package com.theatermgnt.theatermgnt.revenue.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.bookingCombo.entity.BookingCombo;
import com.theatermgnt.theatermgnt.bookingCombo.repository.BookingComboRepository;
import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.revenue.entity.DailyRevenueSummary;
import com.theatermgnt.theatermgnt.revenue.entity.MovieRevenue;
import com.theatermgnt.theatermgnt.revenue.entity.RevenueProcessingLog;
import com.theatermgnt.theatermgnt.revenue.repository.DailyRevenueSummaryRepository;
import com.theatermgnt.theatermgnt.revenue.repository.MovieRevenueRepository;
import com.theatermgnt.theatermgnt.revenue.repository.RevenueProcessingLogRepository;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RevenueAggregationService {

    private final RevenueProcessingLogRepository processingLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final BookingComboRepository bookingComboRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;
    private final MovieRevenueRepository movieRevenueRepository;
    private final com.theatermgnt.theatermgnt.payment.repository.PaymentRepository paymentRepository;

    /**
     * Process invoice refund and subtract revenue.
     * This method processes refund by subtracting revenue for the given invoice.
     *
     * @param invoiceId The invoice ID that was refunded
     */
    public void processInvoiceRefundForRevenue(String invoiceId) {
        log.info("Processing invoice refund {} for revenue subtraction", invoiceId);

        try {
            // Resolve invoice and booking
            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() -> new IllegalStateException("Invoice not found: " + invoiceId));

            Booking booking = bookingRepository
                    .findById(UUID.fromString(invoice.getBookingId()))
                    .orElseThrow(() -> new IllegalStateException("Booking not found: " + invoice.getBookingId()));

            Screening screening = booking.getScreening();
            String cinemaId = screening.getRoom().getCinema().getId();
            String movieId = screening.getMovie().getId();

            // Find original payment date to subtract revenue from the same date
            LocalDate reportDate = paymentRepository.findByInvoiceId(invoiceId).stream()
                    .filter(p -> p.getPaymentType() == com.theatermgnt.theatermgnt.payment.entity.PaymentType.BOOKING
                            && p.getStatus() == PaymentStatus.SUCCESS)
                    .findFirst()
                    .map(p -> p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : LocalDate.now())
                    .orElse(LocalDate.now());

            // Count tickets sold (screening seats booked)
            int ticketsSold = screeningSeatRepository
                    .findByBooking(booking.getId().toString())
                    .size();

            // Calculate ticket revenue (totalAmount after discount - combo)
            List<BookingCombo> combos =
                    bookingComboRepository.findByBookingId(booking.getId().toString());
            BigDecimal comboRevenue =
                    combos.stream().map(BookingCombo::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ticketRevenue = booking.getTotalAmount().subtract(comboRevenue);

            // Refund: subtract revenue (multiply by -1)
            int ticketMultiplier = -1;
            BigDecimal revenueMultiplier = BigDecimal.valueOf(-1);

            // Upsert DailyRevenueSummary
            upsertDailyRevenueSummary(
                    cinemaId,
                    reportDate,
                    ticketRevenue.multiply(revenueMultiplier),
                    comboRevenue.multiply(revenueMultiplier),
                    ticketMultiplier);

            // Upsert MovieRevenue
            upsertMovieRevenue(
                    movieId,
                    cinemaId,
                    reportDate,
                    ticketRevenue.multiply(revenueMultiplier),
                    ticketsSold * ticketMultiplier);

            log.info(
                    "Revenue refund completed for invoice {}: cinema={}, movie={}, date={}, tickets={}, ticketRev={}, comboRev={}",
                    invoiceId,
                    cinemaId,
                    movieId,
                    reportDate,
                    ticketsSold,
                    ticketRevenue,
                    comboRevenue);

        } catch (Exception e) {
            log.error("Error processing invoice refund {} for revenue", invoiceId, e);
            throw e;
        }
    }

    /**
     * Process payment event and aggregate revenue data.
     * This method is idempotent - safe to call multiple times for same payment.
     *
     * @param payment The payment that was completed (SUCCESS or REFUNDED)
     */
    public void processPaymentForRevenue(Payment payment) {
        // Check idempotency: skip if already processed
        if (processingLogRepository.existsByPaymentId(payment.getId())) {
            log.info("Payment {} already processed for revenue, skipping", payment.getId());
            return;
        }

        try {
            log.info("Processing payment {} for revenue aggregation", payment.getId());

            // Resolve invoice and booking
            Invoice invoice = invoiceRepository
                    .findById(payment.getInvoiceId())
                    .orElseThrow(() -> new IllegalStateException("Invoice not found: " + payment.getInvoiceId()));

            Booking booking = bookingRepository
                    .findById(UUID.fromString(invoice.getBookingId()))
                    .orElseThrow(() -> new IllegalStateException("Booking not found: " + invoice.getBookingId()));

            Screening screening = booking.getScreening();
            String cinemaId = screening.getRoom().getCinema().getId();
            String movieId = screening.getMovie().getId();
            // Use payment date instead of screening date for revenue reporting
            LocalDate reportDate =
                    payment.getPaymentDate() != null ? payment.getPaymentDate().toLocalDate() : LocalDate.now();

            // Count tickets sold (screening seats booked)
            int ticketsSold = screeningSeatRepository
                    .findByBooking(booking.getId().toString())
                    .size();

            // Calculate ticket revenue (totalAmount after discount - combo)
            List<BookingCombo> combos =
                    bookingComboRepository.findByBookingId(booking.getId().toString());
            BigDecimal comboRevenue =
                    combos.stream().map(BookingCombo::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ticketRevenue = booking.getTotalAmount().subtract(comboRevenue);

            // Handle SUCCESS or REFUND
            boolean isRefund = payment.getStatus() == PaymentStatus.REFUNDED;
            int ticketMultiplier = isRefund ? -1 : 1;
            BigDecimal revenueMultiplier = isRefund ? BigDecimal.valueOf(-1) : BigDecimal.ONE;

            // Upsert DailyRevenueSummary
            upsertDailyRevenueSummary(
                    cinemaId,
                    reportDate,
                    ticketRevenue.multiply(revenueMultiplier),
                    comboRevenue.multiply(revenueMultiplier),
                    ticketMultiplier);

            // Upsert MovieRevenue
            upsertMovieRevenue(
                    movieId,
                    cinemaId,
                    reportDate,
                    ticketRevenue.multiply(revenueMultiplier),
                    ticketsSold * ticketMultiplier);

            // Log successful processing
            RevenueProcessingLog processingLog = RevenueProcessingLog.builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus().name())
                    .processedAt(LocalDateTime.now())
                    .build();
            processingLogRepository.save(processingLog);

            log.info(
                    "Revenue aggregation completed for payment {}: cinema={}, movie={}, date={}, tickets={}, ticketRev={}, comboRev={}",
                    payment.getId(),
                    cinemaId,
                    movieId,
                    reportDate,
                    ticketsSold,
                    ticketRevenue,
                    comboRevenue);

        } catch (Exception e) {
            log.error("Error processing payment {} for revenue", payment.getId(), e);

            // Log error
            RevenueProcessingLog errorLog = RevenueProcessingLog.builder()
                    .paymentId(payment.getId())
                    .paymentStatus(payment.getStatus().name())
                    .processedAt(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
            processingLogRepository.save(errorLog);

            throw e; // Re-throw to allow transaction rollback if needed
        }
    }

    private void upsertDailyRevenueSummary(
            String cinemaId,
            LocalDate reportDate,
            BigDecimal ticketRevenue,
            BigDecimal comboRevenue,
            int transactionIncrement) {

        Optional<DailyRevenueSummary> existing =
                dailyRevenueSummaryRepository.findByCinemaIdAndReportDate(cinemaId, reportDate);

        if (existing.isPresent()) {
            // Update existing
            DailyRevenueSummary summary = existing.get();
            summary.setTicketRevenue(summary.getTicketRevenue().add(ticketRevenue));
            summary.setComboRevenue(summary.getComboRevenue().add(comboRevenue));
            summary.setNetRevenue(summary.getTicketRevenue().add(summary.getComboRevenue()));
            summary.setTotalTransactions(summary.getTotalTransactions() + transactionIncrement);
            dailyRevenueSummaryRepository.save(summary);
            log.debug("Updated DailyRevenueSummary for cinema={}, date={}", cinemaId, reportDate);
        } else {
            // Create new
            DailyRevenueSummary newSummary = DailyRevenueSummary.builder()
                    .cinemaId(cinemaId)
                    .reportDate(reportDate)
                    .ticketRevenue(ticketRevenue)
                    .comboRevenue(comboRevenue)
                    .netRevenue(ticketRevenue.add(comboRevenue))
                    .totalTransactions(transactionIncrement)
                    .build();
            dailyRevenueSummaryRepository.save(newSummary);
            log.debug("Created DailyRevenueSummary for cinema={}, date={}", cinemaId, reportDate);
        }
    }

    private void upsertMovieRevenue(
            String movieId, String cinemaId, LocalDate reportDate, BigDecimal ticketRevenue, int ticketsSoldIncrement) {

        Optional<MovieRevenue> existing =
                movieRevenueRepository.findByMovieIdAndCinemaIdAndReportDate(movieId, cinemaId, reportDate);

        if (existing.isPresent()) {
            // Update existing
            MovieRevenue revenue = existing.get();
            revenue.setTotalRevenue(revenue.getTotalRevenue().add(ticketRevenue));
            revenue.setTotalTicketsSold(revenue.getTotalTicketsSold() + ticketsSoldIncrement);
            movieRevenueRepository.save(revenue);
            log.debug("Updated MovieRevenue for movie={}, cinema={}, date={}", movieId, cinemaId, reportDate);
        } else {
            // Create new
            MovieRevenue newRevenue = MovieRevenue.builder()
                    .movieId(movieId)
                    .cinemaId(cinemaId)
                    .reportDate(reportDate)
                    .totalRevenue(ticketRevenue)
                    .totalTicketsSold(ticketsSoldIncrement)
                    .build();
            movieRevenueRepository.save(newRevenue);
            log.debug("Created MovieRevenue for movie={}, cinema={}, date={}", movieId, cinemaId, reportDate);
        }
    }
}
