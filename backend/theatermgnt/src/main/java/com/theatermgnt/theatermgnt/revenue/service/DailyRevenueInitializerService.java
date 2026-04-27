package com.theatermgnt.theatermgnt.revenue.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.cinema.entity.Cinema;
import com.theatermgnt.theatermgnt.cinema.repository.CinemaRepository;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;
import com.theatermgnt.theatermgnt.revenue.entity.DailyRevenueSummary;
import com.theatermgnt.theatermgnt.revenue.entity.MovieRevenue;
import com.theatermgnt.theatermgnt.revenue.repository.DailyRevenueSummaryRepository;
import com.theatermgnt.theatermgnt.revenue.repository.MovieRevenueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyRevenueInitializerService {

    private final CinemaRepository cinemaRepository;
    private final MovieRepository movieRepository;
    private final DailyRevenueSummaryRepository dailyRevenueSummaryRepository;
    private final MovieRevenueRepository movieRevenueRepository;

    /**
     * Auto-initialize when app starts (if today's records don't exist)
     * Handles case where app is started after midnight
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeOnStartup() {
        log.info("Checking if daily revenue summaries need initialization on startup");

        LocalDate today = LocalDate.now();

        // Check if today has any DailyRevenueSummary
        // Use findAllByCinemaIdAndReportDate to handle potential duplicates
        long existingCount = cinemaRepository.findAll().stream()
                .filter(cinema -> !dailyRevenueSummaryRepository
                        .findAllByCinemaIdAndReportDate(cinema.getId(), today)
                        .isEmpty())
                .count();

        if (existingCount == 0) {
            log.info("No revenue summaries found for today, initializing...");
            try {
                initializeDailyRevenueSummaries();
            } catch (Exception e) {
                log.error("Error during startup initialization", e);
            }
        } else {
            log.info("Revenue summaries already exist for today, skipping initialization");
        }
    }

    /**
     * Run at 00:00 every day to initialize revenue summaries for today
     * Ensures dashboard always has data even if no transactions occurred
     */
    @Scheduled(cron = "0 0 0 * * *") // Midnight every day
    @Transactional
    public void initializeDailyRevenueSummaries() {
        log.info("Starting daily revenue initialization for today");

        LocalDate today = LocalDate.now();

        try {
            // Initialize DailyRevenueSummary for all cinemas if not exists
            initializeDailyRevenueSummariesForAllCinemas(today);

            // Initialize MovieRevenue for all movies and cinemas if not exists
            initializeMovieRevenuesForAllMovies(today);

            log.info("Daily revenue initialization completed successfully for {}", today);
        } catch (Exception e) {
            log.error("Error during daily revenue initialization", e);
        }
    }

    private void initializeDailyRevenueSummariesForAllCinemas(LocalDate reportDate) {
        List<Cinema> cinemas = cinemaRepository.findAll();

        for (Cinema cinema : cinemas) {
            Optional<DailyRevenueSummary> existing =
                    dailyRevenueSummaryRepository.findByCinemaIdAndReportDate(cinema.getId(), reportDate);

            if (existing.isEmpty()) {
                DailyRevenueSummary newSummary = DailyRevenueSummary.builder()
                        .cinemaId(cinema.getId())
                        .reportDate(reportDate)
                        .ticketRevenue(BigDecimal.ZERO)
                        .comboRevenue(BigDecimal.ZERO)
                        .netRevenue(BigDecimal.ZERO)
                        .totalTransactions(0)
                        .build();
                dailyRevenueSummaryRepository.save(newSummary);
                log.debug("Created DailyRevenueSummary for cinema {} on {}", cinema.getId(), reportDate);
            }
        }
    }

    private void initializeMovieRevenuesForAllMovies(LocalDate reportDate) {
        List<Cinema> cinemas = cinemaRepository.findAll();
        List<Movie> movies = movieRepository.findAll();

        for (Cinema cinema : cinemas) {
            for (Movie movie : movies) {
                Optional<MovieRevenue> existing = movieRevenueRepository.findByMovieIdAndCinemaIdAndReportDate(
                        movie.getId(), cinema.getId(), reportDate);

                if (existing.isEmpty()) {
                    MovieRevenue newRevenue = MovieRevenue.builder()
                            .movieId(movie.getId())
                            .cinemaId(cinema.getId())
                            .reportDate(reportDate)
                            .totalRevenue(BigDecimal.ZERO)
                            .totalTicketsSold(0)
                            .build();
                    movieRevenueRepository.save(newRevenue);
                    log.debug(
                            "Created MovieRevenue for movie {} in cinema {} on {}",
                            movie.getId(),
                            cinema.getId(),
                            reportDate);
                }
            }
        }

        log.info("Initialized MovieRevenue for {} cinemas x {} movies", cinemas.size(), movies.size());
    }
}
