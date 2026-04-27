package com.theatermgnt.theatermgnt.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.booking.service.BookingService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.payment.dto.request.CreateInvoiceRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceDetailResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceStatisticsResponse;
import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.event.InvoiceRefundedEvent;
import com.theatermgnt.theatermgnt.payment.mapper.InvoiceMapper;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.revenue.service.RevenueAggregationService;
import com.theatermgnt.theatermgnt.ticket.service.TicketService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final InvoiceMapper invoiceMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RevenueAggregationService revenueAggregationService;
    private final TicketService ticketService;

    private static final int INVOICE_EXPIRY_DAYS = 7;

    // Constructor with @Lazy for BookingService to break circular dependency
    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            BookingRepository bookingRepository,
            @Lazy BookingService bookingService,
            InvoiceMapper invoiceMapper,
            RevenueAggregationService revenueAggregationService,
            ApplicationEventPublisher eventPublisher,
            TicketService ticketService) {
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.invoiceMapper = invoiceMapper;
        this.revenueAggregationService = revenueAggregationService;
        this.eventPublisher = eventPublisher;
        this.ticketService = ticketService;
    }

    @Override
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creating invoice for booking: {}", request.getBookingId());

        // Check if booking exists
        Booking booking = bookingRepository
                .findById(UUID.fromString(request.getBookingId()))
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // Check if invoice already exists for this booking
        if (invoiceRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED); // or custom error
        }

        // Create invoice
        Invoice invoice = Invoice.builder()
                .bookingId(request.getBookingId())
                .totalAmount(booking.getTotalAmount())
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully: {}", savedInvoice.getId());

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public InvoiceResponse getInvoice(String invoiceId) {
        log.info("Fetching invoice: {}", invoiceId);

        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public InvoiceResponse getInvoiceByBookingId(String bookingId) {
        log.info("Fetching invoice by booking: {}", bookingId);

        Invoice invoice = invoiceRepository
                .findByBookingId(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public InvoiceResponse updateInvoiceStatus(String invoiceId, InvoiceStatus status) {
        log.info("Updating invoice {} status to: {}", invoiceId, status);

        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        invoice.setStatus(status);

        if (status == InvoiceStatus.PAID) {
            invoice.setPaidAt(LocalDateTime.now());
        } else if (status == InvoiceStatus.REFUNDED) {
            // When invoice is refunded, update corresponding booking
            String bookingId = invoice.getBookingId();
            if (bookingId != null) {
                try {
                    bookingService.refundBooking(bookingId);
                    log.info("Booking {} refunded due to invoice refund", bookingId);

                    // Expire all active tickets for this booking
                    try {
                        UUID bookingUuid = UUID.fromString(bookingId);
                        ticketService.expireTicketsByBookingId(bookingUuid);
                        log.info("Expired tickets for refunded booking {}", bookingId);
                    } catch (Exception ticketErr) {
                        log.error("Error expiring tickets for booking {} during refund", bookingId, ticketErr);
                    }
                } catch (Exception e) {
                    log.error("Error refunding booking {} for invoice {}", bookingId, invoiceId, e);
                    // Don't fail the invoice refund if booking update fails
                }
            }

            // Process refund for revenue (subtract revenue)
            try {
                revenueAggregationService.processInvoiceRefundForRevenue(invoiceId);
                log.info("Revenue refund processed for invoice {}", invoiceId);
            } catch (Exception e) {
                log.error("Error processing revenue refund for invoice {}", invoiceId, e);
                // Don't fail the invoice refund if revenue processing fails
            }

            // Publish refund event to send email
            try {
                eventPublisher.publishEvent(new InvoiceRefundedEvent(invoiceId, invoice.getBookingId()));
                log.info("Published InvoiceRefundedEvent for invoice {}", invoiceId);
            } catch (Exception e) {
                log.error("Error publishing InvoiceRefundedEvent for invoice {}", invoiceId, e);
            }
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice status updated successfully");

        return invoiceMapper.toResponse(updatedInvoice);
    }

    @Override
    public InvoiceResponse markAsPaid(String invoiceId) {
        return updateInvoiceStatus(invoiceId, InvoiceStatus.PAID);
    }

    @Override
    public InvoiceResponse markAsFailed(String invoiceId) {
        return updateInvoiceStatus(invoiceId, InvoiceStatus.FAILED);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(int page, int size, String cinemaId) {
        log.info("Fetching all invoices - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = (cinemaId != null && !cinemaId.isBlank())
                ? invoiceRepository.findByCinema(cinemaId, pageable)
                : invoiceRepository.findAllByOrderByCreatedAtDesc(pageable);
        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, int page, int size, String cinemaId) {
        log.info("Fetching invoices by status: {} - page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = (cinemaId != null && !cinemaId.isBlank())
                ? invoiceRepository.findByCinemaAndStatus(cinemaId, status.name(), pageable)
                : invoiceRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> getInvoicesByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, String cinemaId) {
        log.info("Fetching invoices by date range: {} to {} - page: {}, size: {}", startDate, endDate, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = (cinemaId != null && !cinemaId.isBlank())
                ? invoiceRepository.findByCinemaAndDateRange(cinemaId, startDate, endDate, pageable)
                : invoiceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> searchInvoices(String search, int page, int size, String cinemaId) {
        log.info("Searching invoices: {} - page: {}, size: {}", search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = (cinemaId != null && !cinemaId.isBlank())
                ? invoiceRepository.searchInvoicesWithinCinema(search, cinemaId, pageable)
                : invoiceRepository.searchInvoices(search, pageable);
        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public Page<InvoiceResponse> searchInvoicesByStatus(
            String search, InvoiceStatus status, int page, int size, String cinemaId) {
        log.info("Searching invoices by status: {} search: {} - page: {}, size: {}", status, search, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = (cinemaId != null && !cinemaId.isBlank())
                ? invoiceRepository.searchInvoicesByStatusWithinCinema(search, status.name(), cinemaId, pageable)
                : invoiceRepository.searchInvoicesByStatus(search, status, pageable);
        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public InvoiceDetailResponse getInvoiceDetail(String invoiceId) {
        log.info("Fetching invoice detail: {}", invoiceId);

        Invoice invoice = invoiceRepository
                .findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        log.info("Invoice found: {} with bookingId: {}", invoice.getId(), invoice.getBookingId());

        try {
            UUID bookingUuid = UUID.fromString(invoice.getBookingId());
            log.info("Fetching booking summary for UUID: {}", bookingUuid);

            InvoiceDetailResponse response = InvoiceDetailResponse.builder()
                    .id(invoice.getId())
                    .bookingId(invoice.getBookingId())
                    .totalAmount(invoice.getTotalAmount())
                    .status(invoice.getStatus().name())
                    .createdAt(invoice.getCreatedAt())
                    .paidAt(invoice.getPaidAt())
                    .bookingDetails(bookingService.getBookingSummary(bookingUuid))
                    .build();

            log.info("Successfully built invoice detail response for invoice: {}", invoiceId);
            return response;
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for bookingId: {}", invoice.getBookingId(), e);
            throw new AppException(ErrorCode.INVALID_KEY);
        } catch (Exception e) {
            log.error(
                    "Error fetching booking details for invoice: {} with bookingId: {}",
                    invoiceId,
                    invoice.getBookingId(),
                    e);
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }
    }

    @Override
    public InvoiceStatisticsResponse getStatistics(String cinemaId) {
        log.info("Calculating invoice statistics for cinemaId: {}", cinemaId);

        Long totalInvoices;
        Long pendingInvoices;
        Long paidInvoices;
        Long failedInvoices;
        Long refundedInvoices;
        Double totalRevenue;
        Double pendingAmount;
        Double refundedAmount;

        if (cinemaId != null && !cinemaId.isEmpty()) {
            // Cinema-scoped statistics
            totalInvoices = invoiceRepository.countByCinema(cinemaId);
            pendingInvoices = invoiceRepository.countByCinemaAndStatus(cinemaId, InvoiceStatus.PENDING.toString());
            paidInvoices = invoiceRepository.countByCinemaAndStatus(cinemaId, InvoiceStatus.PAID.toString());
            failedInvoices = invoiceRepository.countByCinemaAndStatus(cinemaId, InvoiceStatus.FAILED.toString());
            refundedInvoices = invoiceRepository.countByCinemaAndStatus(cinemaId, InvoiceStatus.REFUNDED.toString());

            totalRevenue = invoiceRepository.sumTotalAmountByCinemaAndStatus(cinemaId, InvoiceStatus.PAID.toString());
            pendingAmount =
                    invoiceRepository.sumTotalAmountByCinemaAndStatus(cinemaId, InvoiceStatus.PENDING.toString());
            refundedAmount =
                    invoiceRepository.sumTotalAmountByCinemaAndStatus(cinemaId, InvoiceStatus.REFUNDED.toString());
        } else {
            // Global statistics
            totalInvoices = invoiceRepository.count();
            pendingInvoices = invoiceRepository.countByStatus(InvoiceStatus.PENDING);
            paidInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID);
            failedInvoices = invoiceRepository.countByStatus(InvoiceStatus.FAILED);
            refundedInvoices = invoiceRepository.countByStatus(InvoiceStatus.REFUNDED);

            totalRevenue = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.PAID);
            pendingAmount = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.PENDING);
            refundedAmount = invoiceRepository.sumTotalAmountByStatus(InvoiceStatus.REFUNDED);
        }

        return InvoiceStatisticsResponse.builder()
                .totalInvoices(totalInvoices)
                .pendingInvoices(pendingInvoices)
                .paidInvoices(paidInvoices)
                .failedInvoices(failedInvoices)
                .refundedInvoices(refundedInvoices)
                .totalRevenue(BigDecimal.valueOf(totalRevenue != null ? totalRevenue : 0))
                .pendingAmount(BigDecimal.valueOf(pendingAmount != null ? pendingAmount : 0))
                .refundedAmount(BigDecimal.valueOf(refundedAmount != null ? refundedAmount : 0))
                .build();
    }
}
