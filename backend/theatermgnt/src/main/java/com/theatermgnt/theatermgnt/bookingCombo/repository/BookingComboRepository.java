package com.theatermgnt.theatermgnt.bookingCombo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.bookingCombo.entity.BookingCombo;

public interface BookingComboRepository extends JpaRepository<BookingCombo, String> {
    void deleteByBookingId(String bookingId);

    @Query("""
		SELECT COALESCE(SUM(bc.subtotal), 0)
		FROM BookingCombo bc
		WHERE bc.bookingId = :bookingId
	""")
    BigDecimal sumSubtotalByBookingId(@Param("bookingId") String bookingId);

    List<BookingCombo> findByBookingId(String bookingId);
}
