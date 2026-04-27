package com.theatermgnt.theatermgnt.revenue.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.revenue.entity.RevenueReport;
import com.theatermgnt.theatermgnt.revenue.enums.ReportType;

public interface RevenueReportRepository extends JpaRepository<RevenueReport, String> {

    @Query(
            """
		SELECT r FROM RevenueReport r
		WHERE (:cinemaId IS NULL OR :cinemaId = '' OR r.cinemaId = :cinemaId)
		AND r.reportType = COALESCE(:reportType, r.reportType)
		AND r.startDate >= COALESCE(:fromDate, CAST('1900-01-01' AS date))
		AND r.endDate <= COALESCE(:toDate, CAST('2099-12-31' AS date))
		ORDER BY r.generatedAt DESC
	""")
    List<RevenueReport> findFiltered(
            @Param("cinemaId") String cinemaId,
            @Param("reportType") ReportType reportType,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // Find existing report by unique key for upsert logic
    @Query(
            """
		SELECT r FROM RevenueReport r
		WHERE r.cinemaId = :cinemaId
		AND r.reportType = :reportType
		AND r.startDate = :startDate
		AND r.endDate = :endDate
	""")
    List<RevenueReport> findByCinemaIdAndReportTypeAndStartDateAndEndDate(
            @Param("cinemaId") String cinemaId,
            @Param("reportType") ReportType reportType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
