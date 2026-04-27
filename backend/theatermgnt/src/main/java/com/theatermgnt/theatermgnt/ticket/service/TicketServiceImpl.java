package com.theatermgnt.theatermgnt.ticket.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.bookingCombo.dto.response.ComboCheckInResponse;
import com.theatermgnt.theatermgnt.bookingCombo.repository.BookingComboRepository;
import com.theatermgnt.theatermgnt.bookingCombo.service.BookingComboService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;
import com.theatermgnt.theatermgnt.screeningSeat.service.ScreeningSeatService;
import com.theatermgnt.theatermgnt.ticket.dto.request.TicketCheckInRequest;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketCheckInResponse;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketCheckInViewResponse;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketResponse;
import com.theatermgnt.theatermgnt.ticket.entity.Ticket;
import com.theatermgnt.theatermgnt.ticket.enums.TicketStatus;
import com.theatermgnt.theatermgnt.ticket.event.TicketCreatedEvent;
import com.theatermgnt.theatermgnt.ticket.mapper.TicketMapper;
import com.theatermgnt.theatermgnt.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final BookingRepository bookingRepository;
    private final BookingComboRepository bookingComboRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final ScreeningSeatService screeningSeatService;
    private final BookingComboService bookingComboService;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final QrGenerator qrGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<TicketResponse> getTicketsByBooking(UUID bookingId) {
        return ticketRepository.findAllByBookingId(bookingId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    public TicketCheckInResponse checkInByQr(String qrContent) {
        String ticketCode = extractTicketCode(qrContent);

        Ticket ticket = ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            return new TicketCheckInResponse(
                    ticketCode, ticket.getStatus(), ticket.getUsedAt(), "Ticket is not active");
        }

        if (Instant.now().isAfter(ticket.getExpiresAt())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            return new TicketCheckInResponse(ticketCode, TicketStatus.EXPIRED, null, "Ticket expired");
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(Instant.now());

        return new TicketCheckInResponse(ticketCode, TicketStatus.USED, ticket.getUsedAt(), "Check-in successful");
    }

    private String extractTicketCode(String qrContent) {
        return qrContent.split("\\|")[0];
    }

    @Override
    public List<Ticket> createTickets(UUID bookingId) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_EXISTED));

        // chỉ tạo vé khi đã thanh toán
        if (booking.getStatus() != BookingStatus.PAID) {
            throw new IllegalStateException("Booking not paid");
        }

        List<ScreeningSeat> seats = screeningSeatRepository.findByBooking(bookingId.toString());

        if (seats.isEmpty()) {
            throw new AppException(ErrorCode.SCREENING_SEAT_NOT_EXISTED);
        }

        Instant expiresAt = booking.getScreening()
                .getEndTime()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toInstant();

        List<Ticket> tickets = seats.stream()
                .map(seat -> {
                    String ticketCode = generateUniqueCode();
                    String qrContent = qrGenerator.generateQrContent(ticketCode);
                    BigDecimal ticketPrice =
                            screeningSeatService.getScreeningSeat(seat.getId()).getPrice();

                    return Ticket.builder()
                            .booking(booking)
                            .screeningSeat(seat)
                            .seatName(seat.getSeat().getRowChair()
                                    + seat.getSeat().getSeatNumber())
                            .price(ticketPrice)
                            .ticketCode(ticketCode)
                            .qrContent(qrContent)
                            .status(TicketStatus.ACTIVE)
                            .expiresAt(expiresAt)
                            .build();
                })
                .toList();
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
        if (booking.getCustomer() != null) {
            eventPublisher.publishEvent(TicketCreatedEvent.builder()
                    .accountId(
                            UUID.fromString(booking.getCustomer().getAccount().getId()))
                    .bookingId(booking.getId())
                    .ticketIds(savedTickets.stream().map(Ticket::getId).toList())
                    .build());
        }
        return savedTickets;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = ticketCodeGenerator.generate();
        } while (ticketRepository.existsByTicketCode(code));
        return code;
    }

    @Override
    public Ticket getTicketByCode(String ticketCode) {
        return ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));
    }

    @Override
    public TicketCheckInViewResponse getTicketCheckInViewByCode(String ticketCode) {
        Ticket ticket = ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));
        List<ComboCheckInResponse> comboResponse =
                bookingComboService.getCombos(ticket.getBooking().getId());
        TicketResponse ticketResponse = ticketMapper.toResponse(ticket);
        return new TicketCheckInViewResponse(ticketResponse, comboResponse);
    }

    @Override
    public List<Ticket> getTicketsByCustomerId(String customerId) {
        return ticketRepository.findByBooking_Customer_IdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public void expireTickets() {

        Instant now = Instant.now();

        List<Ticket> expiredTickets = ticketRepository.findAllByStatusAndExpiresAtBefore(TicketStatus.ACTIVE, now);

        if (expiredTickets.isEmpty()) {
            return;
        }

        expiredTickets.forEach(ticket -> ticket.setStatus(TicketStatus.EXPIRED));

        ticketRepository.saveAll(expiredTickets);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('STAFF')")
    public void checkInTicket(TicketCheckInRequest request) {
        Ticket ticket = ticketRepository
                .findByTicketCode(request.getTicketCode())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new AppException(ErrorCode.TICKET_NOT_ACTIVE);
        }

        if (Instant.now().isAfter(ticket.getExpiresAt())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            throw new AppException(ErrorCode.TICKET_EXPIRED);
        }
        for (var comboUse : request.getComboUseList()) {
            var bookingCombo = bookingComboRepository
                    .findById(comboUse.getComboId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_COMBO_NOT_EXISTED));
            if (bookingCombo.getRemain() < comboUse.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_COMBO_QUANTITY);
            }
            bookingCombo.setRemain(bookingCombo.getRemain() - comboUse.getQuantity());
            bookingComboRepository.save(bookingCombo);
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(Instant.now());
        ticketRepository.save(ticket);
    }

    @Override
    public void expireTicketsByBookingId(UUID bookingId) {
        log.info("Expiring all active tickets for booking: {}", bookingId);

        List<Ticket> activeTickets = ticketRepository.findByBookingIdAndStatus(bookingId, TicketStatus.ACTIVE);

        if (activeTickets.isEmpty()) {
            log.info("No active tickets found for booking: {}", bookingId);
            return;
        }

        activeTickets.forEach(ticket -> {
            ticket.setStatus(TicketStatus.EXPIRED);
            log.debug("Expiring ticket: {} for booking: {}", ticket.getTicketCode(), bookingId);
        });

        ticketRepository.saveAll(activeTickets);
        log.info("Expired {} tickets for booking: {}", activeTickets.size(), bookingId);
    }

    @Override
    public void markTicketForTransfer(String ticketCode, String customerId) {
        Ticket ticket = ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));

        // Verify ownership
        if (!ticket.getBooking().getCustomer().getId().equals(customerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if ticket is active
        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new AppException(ErrorCode.TICKET_NOT_ACTIVE);
        }

        // Check if screening is at least 1 hour away
        Instant now = Instant.now();
        Instant screeningTime = ticket.getBooking()
                .getScreening()
                .getStartTime()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant();
        long hoursUntilScreening = (screeningTime.toEpochMilli() - now.toEpochMilli()) / (1000 * 60 * 60);

        if (hoursUntilScreening < 1) {
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }

        ticket.setStatus(TicketStatus.FOR_TRANSFER);
        ticketRepository.save(ticket);
        log.info("Ticket {} marked for transfer by customer {}", ticketCode, customerId);
    }

    @Override
    public void cancelTicketTransfer(String ticketCode, String customerId) {
        Ticket ticket = ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_EXISTED));

        // Verify ownership
        if (!ticket.getBooking().getCustomer().getId().equals(customerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if ticket is marked for transfer
        if (ticket.getStatus() != TicketStatus.FOR_TRANSFER) {
            throw new AppException(ErrorCode.TICKET_NOT_ACTIVE);
        }

        ticket.setStatus(TicketStatus.ACTIVE);
        ticketRepository.save(ticket);
        log.info("Ticket {} transfer cancelled by customer {}", ticketCode, customerId);
    }
}
