package com.theatermgnt.theatermgnt.revenue.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.revenue.dto.request.DailyRevenueCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.request.MovieRevenueCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.request.RevenueReportCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.request.RevenueReportGenerateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.response.DailyRevenueResponse;
import com.theatermgnt.theatermgnt.revenue.dto.response.MovieRevenueResponse;
import com.theatermgnt.theatermgnt.revenue.dto.response.RevenueReportResponse;
import com.theatermgnt.theatermgnt.revenue.enums.ReportType;
import com.theatermgnt.theatermgnt.revenue.service.DailyRevenueService;
import com.theatermgnt.theatermgnt.revenue.service.MovieRevenueService;
import com.theatermgnt.theatermgnt.revenue.service.RevenueReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueReportService revenueReportService;
    private final DailyRevenueService dailyRevenueService;
    private final MovieRevenueService movieRevenueService;

    // ---- Revenue Reports ----
    @PostMapping("/reports")
    public ResponseEntity<RevenueReportResponse> createRevenueReport(
            @Valid @RequestBody RevenueReportCreateRequest request) {
        return ResponseEntity.ok(revenueReportService.create(request));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<RevenueReportResponse>> getRevenueReports(
            @RequestParam(required = false) String cinemaId,
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(revenueReportService.find(cinemaId, reportType, from, to));
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<RevenueReportResponse> generateRevenueReport(
            @Valid @RequestBody RevenueReportGenerateRequest request) {
        return ResponseEntity.ok(revenueReportService.generate(request));
    }

    // ---- Daily revenue summary ----
    @PostMapping("/daily")
    public ResponseEntity<DailyRevenueResponse> createDailySummary(
            @Valid @RequestBody DailyRevenueCreateRequest request) {
        return ResponseEntity.ok(dailyRevenueService.create(request));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyRevenueResponse>> getDailySummaries(
            @RequestParam(required = false) String cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dailyRevenueService.find(cinemaId, from, to));
    }

    // ---- Movie revenue ----
    @PostMapping("/movie")
    public ResponseEntity<MovieRevenueResponse> createMovieRevenue(
            @Valid @RequestBody MovieRevenueCreateRequest request) {
        return ResponseEntity.ok(movieRevenueService.create(request));
    }

    @GetMapping("/movie")
    public ResponseEntity<List<MovieRevenueResponse>> getMovieRevenue(
            @RequestParam(required = false) String cinemaId,
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(movieRevenueService.find(cinemaId, movieId, from, to));
    }
}
