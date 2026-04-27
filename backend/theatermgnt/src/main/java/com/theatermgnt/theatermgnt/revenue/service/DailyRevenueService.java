package com.theatermgnt.theatermgnt.revenue.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.revenue.dto.request.DailyRevenueCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.response.DailyRevenueResponse;
import com.theatermgnt.theatermgnt.revenue.entity.DailyRevenueSummary;
import com.theatermgnt.theatermgnt.revenue.repository.DailyRevenueSummaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyRevenueService {

    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;

    public DailyRevenueResponse create(DailyRevenueCreateRequest request) {
        validateDate(request.getReportDate());
        DailyRevenueSummary entity = DailyRevenueSummary.builder()
                .cinemaId(request.getCinemaId())
                .reportDate(request.getReportDate())
                .ticketRevenue(request.getTicketRevenue())
                .comboRevenue(request.getComboRevenue())
                .netRevenue(request.getNetRevenue())
                .totalTransactions(request.getTotalTransactions())
                .build();
        return toResponse(dailyRevenueSummaryRepository.save(entity));
    }

    public List<DailyRevenueResponse> find(String cinemaId, LocalDate from, LocalDate to) {
        validateDateRangeOptional(from, to);
        return dailyRevenueSummaryRepository.findFiltered(cinemaId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDate(LocalDate date) {
        Assert.notNull(date, "reportDate is required");
    }

    private void validateDateRangeOptional(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private DailyRevenueResponse toResponse(DailyRevenueSummary entity) {
        return DailyRevenueResponse.builder()
                .id(entity.getId())
                .cinemaId(entity.getCinemaId())
                .reportDate(entity.getReportDate())
                .ticketRevenue(entity.getTicketRevenue())
                .comboRevenue(entity.getComboRevenue())
                .netRevenue(entity.getNetRevenue())
                .totalTransactions(entity.getTotalTransactions())
                .build();
    }
}
