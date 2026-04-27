package com.theatermgnt.theatermgnt.booking.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("""
		SELECT b FROM Booking b
		WHERE b.status IN ('PENDING', 'CONFIRM')
		AND b.expiredAt < :now
		""")
    List<Booking> findExpiredPendingBookings(Instant now);

    @Query(
            """
		SELECT b FROM Booking b
		LEFT JOIN b.customer c
		LEFT JOIN c.account a
		LEFT JOIN b.screening s
		LEFT JOIN s.movie m
		LEFT JOIN s.room r
		LEFT JOIN r.cinema ci
		WHERE (:status IS NULL OR b.status = :status)
		AND (:customerSearch IS NULL OR c IS NOT NULL AND (
			LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:customerSearch AS string), '%'))
			OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:customerSearch AS string), '%'))
		))
		AND (:emailSearch IS NULL OR a IS NOT NULL AND LOWER(a.email) LIKE LOWER(CONCAT('%', CAST(:emailSearch AS string), '%')))
		AND (:movieSearch IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', CAST(:movieSearch AS string), '%')))
		AND (:cinemaId IS NULL OR ci.id = :cinemaId)
		""")
    Page<Booking> findBookings(
            @Param("status") BookingStatus status,
            @Param("customerSearch") String customerSearch,
            @Param("emailSearch") String emailSearch,
            @Param("movieSearch") String movieSearch,
            @Param("cinemaId") String cinemaId,
            Pageable pageable);
}
