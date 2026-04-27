package com.theatermgnt.theatermgnt.revenue.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.revenue.entity.MovieRevenue;

public interface MovieRevenueRepository extends JpaRepository<MovieRevenue, String> {

    @Query(
            """
		SELECT m FROM MovieRevenue m
		WHERE (:cinemaId IS NULL OR :cinemaId = '' OR m.cinemaId = :cinemaId)
		AND (:movieId IS NULL OR :movieId = '' OR m.movieId = :movieId)
		AND m.reportDate >= COALESCE(:fromDate, CAST('1900-01-01' AS date))
		AND m.reportDate <= COALESCE(:toDate, CAST('2099-12-31' AS date))
		ORDER BY m.reportDate DESC
	""")
    List<MovieRevenue> findFiltered(
            @Param("cinemaId") String cinemaId,
            @Param("movieId") String movieId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    Optional<MovieRevenue> findByMovieIdAndCinemaIdAndReportDate(String movieId, String cinemaId, LocalDate reportDate);
}
