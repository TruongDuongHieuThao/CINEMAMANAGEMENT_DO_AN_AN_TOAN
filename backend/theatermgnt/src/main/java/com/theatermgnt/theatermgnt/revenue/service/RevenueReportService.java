package com.theatermgnt.theatermgnt.revenue.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.theatermgnt.theatermgnt.cinema.service.CinemaService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.revenue.dto.request.RevenueReportCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.request.RevenueReportGenerateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.response.RevenueReportResponse;
import com.theatermgnt.theatermgnt.revenue.entity.DailyRevenueSummary;
import com.theatermgnt.theatermgnt.revenue.entity.RevenueReport;
import com.theatermgnt.theatermgnt.revenue.enums.ReportType;
import com.theatermgnt.theatermgnt.revenue.repository.DailyRevenueSummaryRepository;
import com.theatermgnt.theatermgnt.revenue.repository.RevenueReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevenueReportService {

    private final RevenueReportRepository revenueReportRepository;
    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;
    private final CinemaService cinemaService;

    public RevenueReportResponse create(RevenueReportCreateRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        RevenueReport entity = RevenueReport.builder()
                .cinemaId(request.getCinemaId())
                .reportType(request.getReportType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalTicketRevenue(request.getTotalTicketRevenue())
                .totalComboRevenue(request.getTotalComboRevenue())
                .netRevenue(request.getNetRevenue())
                .generatedAt(LocalDateTime.now())
                .build();

        return toResponse(revenueReportRepository.save(entity));
    }

    public List<RevenueReportResponse> find(String cinemaId, ReportType reportType, LocalDate from, LocalDate to) {
        validateDateRangeOptional(from, to);
        return revenueReportRepository.findFiltered(cinemaId, reportType, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    public RevenueReportResponse generate(RevenueReportGenerateRequest request) {
        // For CUSTOM type, startDate and endDate must be provided
        if (request.getReportType() == ReportType.CUSTOM) {
            validateDateRange(request.getStartDate(), request.getEndDate());
        } else {
            // For other types, frontend calculates the dates, so they should always be present
            Assert.notNull(request.getStartDate(), "startDate is required");
            Assert.notNull(request.getEndDate(), "endDate is required");
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
        }

        // If cinemaId is empty, generate report for ALL cinemas
        if (request.getCinemaId() == null || request.getCinemaId().isEmpty()) {
            return generateForAllCinemas(request);
        }

        // Otherwise, generate report for specific cinema
        return generateForSpecificCinema(request);
    }

    private RevenueReportResponse generateForSpecificCinema(RevenueReportGenerateRequest request) {
        List<DailyRevenueSummary> rows = dailyRevenueSummaryRepository.findFiltered(
                request.getCinemaId(), request.getStartDate(), request.getEndDate());

        BigDecimal totalTicket =
                rows.stream().map(DailyRevenueSummary::getTicketRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCombo =
                rows.stream().map(DailyRevenueSummary::getComboRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = rows.stream().map(DailyRevenueSummary::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check if report already exists (upsert logic)
        List<RevenueReport> existing = revenueReportRepository.findByCinemaIdAndReportTypeAndStartDateAndEndDate(
                request.getCinemaId(), request.getReportType(), request.getStartDate(), request.getEndDate());

        RevenueReport entity;
        if (!existing.isEmpty()) {
            // Update existing report
            entity = existing.get(0);
            entity.setTotalTicketRevenue(totalTicket);
            entity.setTotalComboRevenue(totalCombo);
            entity.setNetRevenue(net);
            entity.setGeneratedAt(LocalDateTime.now());
        } else {
            // Create new report
            entity = RevenueReport.builder()
                    .cinemaId(request.getCinemaId())
                    .reportType(request.getReportType())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalTicketRevenue(totalTicket)
                    .totalComboRevenue(totalCombo)
                    .netRevenue(net)
                    .generatedAt(LocalDateTime.now())
                    .build();
        }

        return toResponse(revenueReportRepository.save(entity));
    }

    private RevenueReportResponse generateForAllCinemas(RevenueReportGenerateRequest request) {
        // Get all cinemas
        List<String> cinemaIds = cinemaService.getCinemas().stream()
                .map(cinema -> cinema.getId())
                .toList();

        // Generate report for each cinema (upsert logic)
        for (String cinemaId : cinemaIds) {
            List<DailyRevenueSummary> rows =
                    dailyRevenueSummaryRepository.findFiltered(cinemaId, request.getStartDate(), request.getEndDate());

            BigDecimal totalTicket =
                    rows.stream().map(DailyRevenueSummary::getTicketRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCombo =
                    rows.stream().map(DailyRevenueSummary::getComboRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net =
                    rows.stream().map(DailyRevenueSummary::getNetRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

            // Check if report already exists (upsert logic)
            List<RevenueReport> existing = revenueReportRepository.findByCinemaIdAndReportTypeAndStartDateAndEndDate(
                    cinemaId, request.getReportType(), request.getStartDate(), request.getEndDate());

            RevenueReport entity;
            if (!existing.isEmpty()) {
                // Update existing report
                entity = existing.get(0);
                entity.setTotalTicketRevenue(totalTicket);
                entity.setTotalComboRevenue(totalCombo);
                entity.setNetRevenue(net);
                entity.setGeneratedAt(LocalDateTime.now());
            } else {
                // Create new report
                entity = RevenueReport.builder()
                        .cinemaId(cinemaId)
                        .reportType(request.getReportType())
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .totalTicketRevenue(totalTicket)
                        .totalComboRevenue(totalCombo)
                        .netRevenue(net)
                        .generatedAt(LocalDateTime.now())
                        .build();
            }

            revenueReportRepository.save(entity);
        }

        // Return a placeholder response
        return RevenueReportResponse.builder()
                .cinemaId("ALL")
                .reportType(request.getReportType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        Assert.notNull(start, "startDate is required");
        Assert.notNull(end, "endDate is required");
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private void validateDateRangeOptional(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private RevenueReportResponse toResponse(RevenueReport entity) {
        return RevenueReportResponse.builder()
                .id(entity.getId())
                .cinemaId(entity.getCinemaId())
                .reportType(entity.getReportType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .totalTicketRevenue(entity.getTotalTicketRevenue())
                .totalComboRevenue(entity.getTotalComboRevenue())
                .netRevenue(entity.getNetRevenue())
                .generatedAt(entity.getGeneratedAt())
                .build();
    }
}
