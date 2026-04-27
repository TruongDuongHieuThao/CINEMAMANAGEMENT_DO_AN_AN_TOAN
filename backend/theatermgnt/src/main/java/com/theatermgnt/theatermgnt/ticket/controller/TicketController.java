package com.theatermgnt.theatermgnt.ticket.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.ticket.dto.request.TicketCheckInRequest;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketCheckInViewResponse;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketResponse;
import com.theatermgnt.theatermgnt.ticket.entity.Ticket;
import com.theatermgnt.theatermgnt.ticket.mapper.TicketMapper;
import com.theatermgnt.theatermgnt.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    @GetMapping("/by-booking/{bookingId}")
    public ApiResponse<List<TicketResponse>> getTicketsByBooking(@PathVariable UUID bookingId) {

        return ApiResponse.<List<TicketResponse>>builder()
                .result(ticketService.getTicketsByBooking(bookingId))
                .build();
    }

    @GetMapping("/{ticketCode}")
    public ApiResponse<TicketResponse> getTicketByCode(@PathVariable String ticketCode) {

        Ticket ticket = ticketService.getTicketByCode(ticketCode);

        return ApiResponse.<TicketResponse>builder()
                .result(ticketMapper.toResponse(ticket))
                .build();
    }

    @GetMapping("/check-in/{ticketCode}")
    public ApiResponse<TicketCheckInViewResponse> getTicketForCheckIn(@PathVariable String ticketCode) {
        return ApiResponse.<TicketCheckInViewResponse>builder()
                .result(ticketService.getTicketCheckInViewByCode(ticketCode))
                .build();
    }

    @GetMapping("/my-tickets/{customerId}")
    public List<TicketResponse> getTicketsByCustomer(@PathVariable String customerId) {
        List<Ticket> tickets = ticketService.getTicketsByCustomerId(customerId);
        return tickets.stream().map(ticketMapper::toResponse).toList();
    }

    @PostMapping("/check-in/{ticketCode}")
    public ApiResponse<String> checkInTicket(
            @PathVariable String ticketCode, @RequestBody(required = false) TicketCheckInRequest request) {
        if (request == null) {
            request = TicketCheckInRequest.builder().ticketCode(ticketCode).build();
        } else {
            request.setTicketCode(ticketCode);
        }
        ticketService.checkInTicket(request);
        return ApiResponse.<String>builder()
                .result("Ticket checked in successfully")
                .build();
    }

    @PostMapping("/{ticketCode}/mark-for-transfer")
    public ApiResponse<String> markTicketForTransfer(@PathVariable String ticketCode, @RequestParam String customerId) {
        ticketService.markTicketForTransfer(ticketCode, customerId);
        return ApiResponse.<String>builder()
                .result("Ticket marked for transfer successfully")
                .build();
    }

    @PostMapping("/{ticketCode}/cancel-transfer")
    public ApiResponse<String> cancelTicketTransfer(@PathVariable String ticketCode, @RequestParam String customerId) {
        ticketService.cancelTicketTransfer(ticketCode, customerId);
        return ApiResponse.<String>builder()
                .result("Ticket transfer cancelled successfully")
                .build();
    }
}
