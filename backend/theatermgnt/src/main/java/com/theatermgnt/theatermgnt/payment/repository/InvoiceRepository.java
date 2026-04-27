package com.theatermgnt.theatermgnt.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByBookingId(String bookingId);

    List<Invoice> findByStatus(InvoiceStatus status);

    Page<Invoice> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Invoice> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            InvoiceStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query(
            "SELECT i FROM Invoice i WHERE (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.bookingId) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY i.createdAt DESC")
    Page<Invoice> searchInvoices(@Param("search") String search, Pageable pageable);

    @Query(
            "SELECT i FROM Invoice i WHERE (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.bookingId) LIKE LOWER(CONCAT('%', :search, '%'))) AND i.status = :status ORDER BY i.createdAt DESC")
    Page<Invoice> searchInvoicesByStatus(
            @Param("search") String search, @Param("status") InvoiceStatus status, Pageable pageable);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = :status")
    Double sumTotalAmountByStatus(@Param("status") InvoiceStatus status);

    // Cinema-scoped queries (native, join bookings -> screenings -> rooms)
    @Query(
            value = "SELECT i.* FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId \n"
                    + "ORDER BY i.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId",
            nativeQuery = true)
    Page<Invoice> findByCinema(@Param("cinemaId") String cinemaId, Pageable pageable);

    @Query(
            value = "SELECT i.* FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.status = :status \n"
                    + "ORDER BY i.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.status = :status",
            nativeQuery = true)
    Page<Invoice> findByCinemaAndStatus(
            @Param("cinemaId") String cinemaId, @Param("status") String status, Pageable pageable);

    @Query(
            value = "SELECT i.* FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.created_at BETWEEN :startDate AND :endDate \n"
                    + "ORDER BY i.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.created_at BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Page<Invoice> findByCinemaAndDateRange(
            @Param("cinemaId") String cinemaId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query(
            value = "SELECT i.* FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.booking_id) LIKE LOWER(CONCAT('%', :search, '%'))) \n"
                    + "ORDER BY i.created_at DESC",
            countQuery =
                    "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                            + "JOIN screenings s ON s.id = b.screening_id \n"
                            + "JOIN rooms r ON r.id = s.room_id \n"
                            + "WHERE r.cinema_id = :cinemaId AND (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.booking_id) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true)
    Page<Invoice> searchInvoicesWithinCinema(
            @Param("search") String search, @Param("cinemaId") String cinemaId, Pageable pageable);

    @Query(
            value = "SELECT i.* FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.status = :status AND (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.booking_id) LIKE LOWER(CONCAT('%', :search, '%'))) \n"
                    + "ORDER BY i.created_at DESC",
            countQuery =
                    "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                            + "JOIN screenings s ON s.id = b.screening_id \n"
                            + "JOIN rooms r ON r.id = s.room_id \n"
                            + "WHERE r.cinema_id = :cinemaId AND i.status = :status AND (LOWER(i.id) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.booking_id) LIKE LOWER(CONCAT('%', :search, '%')))",
            nativeQuery = true)
    Page<Invoice> searchInvoicesByStatusWithinCinema(
            @Param("search") String search,
            @Param("status") String status,
            @Param("cinemaId") String cinemaId,
            Pageable pageable);

    // Cinema-scoped statistics methods
    @Query(
            value = "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId",
            nativeQuery = true)
    Long countByCinema(@Param("cinemaId") String cinemaId);

    @Query(
            value = "SELECT COUNT(*) FROM invoices i \n" + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.status = :status",
            nativeQuery = true)
    Long countByCinemaAndStatus(@Param("cinemaId") String cinemaId, @Param("status") String status);

    @Query(
            value = "SELECT COALESCE(SUM(i.total_amount), 0) FROM invoices i \n"
                    + "JOIN bookings b ON b.id::TEXT = i.booking_id \n"
                    + "JOIN screenings s ON s.id = b.screening_id \n"
                    + "JOIN rooms r ON r.id = s.room_id \n"
                    + "WHERE r.cinema_id = :cinemaId AND i.status = :status",
            nativeQuery = true)
    Double sumTotalAmountByCinemaAndStatus(@Param("cinemaId") String cinemaId, @Param("status") String status);
}
