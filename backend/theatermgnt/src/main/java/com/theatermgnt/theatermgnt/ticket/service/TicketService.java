package com.theatermgnt.theatermgnt.ticket.service;

import java.util.List;
import java.util.UUID;

import com.theatermgnt.theatermgnt.ticket.dto.request.TicketCheckInRequest;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketCheckInResponse;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketCheckInViewResponse;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketResponse;
import com.theatermgnt.theatermgnt.ticket.entity.Ticket;

public interface TicketService {
    List<TicketResponse> getTicketsByBooking(UUID bookingId);

    TicketCheckInResponse checkInByQr(String qrContent);

    List<Ticket> createTickets(UUID bookingId);

    Ticket getTicketByCode(String ticketCode);

    TicketCheckInViewResponse getTicketCheckInViewByCode(String ticketCode);

    List<Ticket> getTicketsByCustomerId(String customerId);

    void expireTickets();

    void checkInTicket(TicketCheckInRequest request);

    void expireTicketsByBookingId(UUID bookingId);

    // Pass ticket methods
    void markTicketForTransfer(String ticketCode, String customerId);

    void cancelTicketTransfer(String ticketCode, String customerId);
}
