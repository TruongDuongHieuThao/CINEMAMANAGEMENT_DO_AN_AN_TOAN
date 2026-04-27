package com.theatermgnt.theatermgnt.booking.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.booking.dto.request.CreateBookingRequest;
import com.theatermgnt.theatermgnt.booking.dto.request.DiscountPointRequest;
import com.theatermgnt.theatermgnt.booking.dto.response.BookingListResponse;
import com.theatermgnt.theatermgnt.booking.dto.response.BookingSummaryResponse;
import com.theatermgnt.theatermgnt.booking.dto.response.CreateBookingResponse;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;
import com.theatermgnt.theatermgnt.booking.service.BookingService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.payment.dto.request.CreateInvoiceRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceResponse;
import com.theatermgnt.theatermgnt.payment.service.InvoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final InvoiceService invoiceService;

    @PostMapping
    public ApiResponse<CreateBookingResponse> createBooking(@RequestBody @Valid CreateBookingRequest request) {
        return ApiResponse.<CreateBookingResponse>builder()
                .result(bookingService.createBooking(request))
                .build();
    }

    @GetMapping("/{bookingId}/summary")
    public ApiResponse<BookingSummaryResponse> getSummary(@PathVariable UUID bookingId) {
        return ApiResponse.<BookingSummaryResponse>builder()
                .result(bookingService.getBookingSummary(bookingId))
                .build();
    }

    @PostMapping("/{bookingId}/redeem-points")
    public ApiResponse<BookingSummaryResponse> redeemPoints(
            @PathVariable UUID bookingId, @RequestBody @Valid DiscountPointRequest discountPointRequest) {
        return ApiResponse.<BookingSummaryResponse>builder()
                .result(bookingService.redeemPoints(bookingId, discountPointRequest))
                .build();
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<String> cancelBooking(@PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ApiResponse.<String>builder()
                .result("Booking cancel successfully")
                .build();
    }

    /**
     * Create invoice for booking (before payment)
     * POST /api/theater-mgnt/bookings/{bookingId}/create-invoice
     */
    @PostMapping("/{bookingId}/create-invoice")
    public ApiResponse<InvoiceResponse> createInvoice(@PathVariable UUID bookingId) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.createInvoice(CreateInvoiceRequest.builder()
                        .bookingId(bookingId.toString())
                        .build()))
                .build();
    }

    @GetMapping
    public ApiResponse<BookingListResponse> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String customerSearch,
            @RequestParam(required = false) String emailSearch,
            @RequestParam(required = false) String movieSearch,
            @RequestParam(required = false) String cinemaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<BookingListResponse>builder()
                .result(bookingService.getBookings(
                        status, customerSearch, emailSearch, movieSearch, cinemaId, pageable))
                .build();
    }
}
