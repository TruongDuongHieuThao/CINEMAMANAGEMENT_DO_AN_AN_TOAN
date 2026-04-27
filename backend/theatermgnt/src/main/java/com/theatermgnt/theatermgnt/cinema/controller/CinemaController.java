package com.theatermgnt.theatermgnt.cinema.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaCreationRequest;
import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaUpdateRequest;
import com.theatermgnt.theatermgnt.cinema.dto.response.CinemaResponse;
import com.theatermgnt.theatermgnt.cinema.service.CinemaService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cinemas")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CinemaController {
    CinemaService cinemaService;

    @PostMapping
    ApiResponse<CinemaResponse> createCinema(@RequestBody @Valid CinemaCreationRequest request) {
        return ApiResponse.<CinemaResponse>builder()
                .result(cinemaService.createCinema(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<CinemaResponse>> getCinemas() {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(cinemaService.getCinemas())
                .build();
    }

    @GetMapping("/{cinemaId}")
    ApiResponse<CinemaResponse> getCinema(@PathVariable("cinemaId") String cinemaId) {
        return ApiResponse.<CinemaResponse>builder()
                .result(cinemaService.getCinema(cinemaId))
                .build();
    }

    @DeleteMapping("/{cinemaId}")
    ApiResponse<String> deleteCinema(@PathVariable("cinemaId") String cinemaId) {
        cinemaService.deleteCinema(cinemaId);
        return ApiResponse.<String>builder()
                .result("Delete cinema successfully")
                .build();
    }

    @PutMapping("/{cinemaId}")
    ApiResponse<CinemaResponse> updateUser(
            @PathVariable String cinemaId, @RequestBody @Valid CinemaUpdateRequest request) {
        return ApiResponse.<CinemaResponse>builder()
                .result(cinemaService.updateCinema(cinemaId, request))
                .build();
    }

    @GetMapping("/buffer-management")
    ApiResponse<List<CinemaResponse>> getCinemasForBufferManagement() {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(cinemaService.getCinemasForBufferManagement())
                .build();
    }

    @PatchMapping("/{cinemaId}/buffer")
    ApiResponse<CinemaResponse> updateCinemaBuffer(@PathVariable String cinemaId, @RequestParam Integer buffer) {
        return ApiResponse.<CinemaResponse>builder()
                .result(cinemaService.updateCinemaBuffer(cinemaId, buffer))
                .build();
    }
}
