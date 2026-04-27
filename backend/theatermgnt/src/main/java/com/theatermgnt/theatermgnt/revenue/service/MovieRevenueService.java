package com.theatermgnt.theatermgnt.revenue.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.revenue.dto.request.MovieRevenueCreateRequest;
import com.theatermgnt.theatermgnt.revenue.dto.response.MovieRevenueResponse;
import com.theatermgnt.theatermgnt.revenue.entity.MovieRevenue;
import com.theatermgnt.theatermgnt.revenue.repository.MovieRevenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieRevenueService {

    private final MovieRevenueRepository movieRevenueRepository;

    public MovieRevenueResponse create(MovieRevenueCreateRequest request) {
        validateDate(request.getReportDate());
        MovieRevenue entity = MovieRevenue.builder()
                .movieId(request.getMovieId())
                .cinemaId(request.getCinemaId())
                .reportDate(request.getReportDate())
                .totalTicketsSold(request.getTotalTicketsSold())
                .totalRevenue(request.getTotalRevenue())
                .build();
        return toResponse(movieRevenueRepository.save(entity));
    }

    public List<MovieRevenueResponse> find(String cinemaId, String movieId, LocalDate from, LocalDate to) {
        validateDateRangeOptional(from, to);
        return movieRevenueRepository.findFiltered(cinemaId, movieId, from, to).stream()
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

    private MovieRevenueResponse toResponse(MovieRevenue entity) {
        return MovieRevenueResponse.builder()
                .id(entity.getId())
                .movieId(entity.getMovieId())
                .cinemaId(entity.getCinemaId())
                .reportDate(entity.getReportDate())
                .totalTicketsSold(entity.getTotalTicketsSold())
                .totalRevenue(entity.getTotalRevenue())
                .build();
    }
}
