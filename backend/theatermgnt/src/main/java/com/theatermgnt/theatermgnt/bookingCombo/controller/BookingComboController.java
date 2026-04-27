package com.theatermgnt.theatermgnt.bookingCombo.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.booking.dto.response.BookingPricingResponse;
import com.theatermgnt.theatermgnt.bookingCombo.dto.request.UpdateBookingCombosRequest;
import com.theatermgnt.theatermgnt.bookingCombo.service.BookingComboService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingComboController {
    private final BookingComboService bookingComboService;

    @PutMapping("/{bookingId}/combos")
    public ApiResponse<BookingPricingResponse> updateCombos(
            @PathVariable UUID bookingId, @RequestBody @Valid UpdateBookingCombosRequest request) {
        return ApiResponse.<BookingPricingResponse>builder()
                .result(bookingComboService.updateCombos(bookingId, request))
                .build();
    }
}
