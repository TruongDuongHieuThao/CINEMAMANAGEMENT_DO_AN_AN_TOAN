package com.theatermgnt.theatermgnt.revenue.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.revenue.entity.DailyRevenueSummary;

public interface DailyRevenueSummaryRepository extends JpaRepository<DailyRevenueSummary, String> {

    @Query(
            """
		SELECT d FROM DailyRevenueSummary d
		WHERE (:cinemaId IS NULL OR :cinemaId = '' OR d.cinemaId = :cinemaId)
		AND d.reportDate >= COALESCE(:fromDate, CAST('1900-01-01' AS date))
		AND d.reportDate <= COALESCE(:toDate, CAST('2099-12-31' AS date))
		ORDER BY d.reportDate DESC
	""")
    List<DailyRevenueSummary> findFiltered(
            @Param("cinemaId") String cinemaId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    Optional<DailyRevenueSummary> findByCinemaIdAndReportDate(String cinemaId, LocalDate reportDate);

    // Handle duplicates by returning list
    List<DailyRevenueSummary> findAllByCinemaIdAndReportDate(String cinemaId, LocalDate reportDate);
}
