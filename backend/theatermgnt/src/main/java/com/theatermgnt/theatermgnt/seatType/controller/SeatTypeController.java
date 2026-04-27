package com.theatermgnt.theatermgnt.seatType.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeCreationRequest;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeUpdateRequest;
import com.theatermgnt.theatermgnt.seatType.dto.response.SeatTypeResponse;
import com.theatermgnt.theatermgnt.seatType.service.SeatTypeService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/seatTypes")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatTypeController {

    SeatTypeService seatTypeService;

    @PostMapping
    ApiResponse<SeatTypeResponse> createSeatType(@RequestBody @Valid SeatTypeCreationRequest request) {
        return ApiResponse.<SeatTypeResponse>builder()
                .result(seatTypeService.createSeatType(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<SeatTypeResponse>> getSeatTypes() {
        return ApiResponse.<List<SeatTypeResponse>>builder()
                .result(seatTypeService.getSeatTypes())
                .build();
    }

    @GetMapping("/{seatTypeId}")
    ApiResponse<SeatTypeResponse> getSeatType(@PathVariable("seatTypeId") String seatTypeId) {
        return ApiResponse.<SeatTypeResponse>builder()
                .result(seatTypeService.getSeatType(seatTypeId))
                .build();
    }

    @DeleteMapping("/{seatTypeId}")
    ApiResponse<String> deleteSeatType(@PathVariable("seatTypeId") String seatTypeId) {
        seatTypeService.deleteSeatType(seatTypeId);
        return ApiResponse.<String>builder()
                .result("Delete seat type successfully")
                .build();
    }

    @PutMapping("/{seatTypeId}")
    ApiResponse<SeatTypeResponse> updateSeatType(
            @PathVariable String seatTypeId, @RequestBody @Valid SeatTypeUpdateRequest request) {
        return ApiResponse.<SeatTypeResponse>builder()
                .result(seatTypeService.updateSeatType(seatTypeId, request))
                .build();
    }
}
