package com.theatermgnt.theatermgnt.ticket.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.theatermgnt.theatermgnt.ticket.entity.Ticket;
import com.theatermgnt.theatermgnt.ticket.enums.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findAllByBookingId(UUID bookingId);

    boolean existsByScreeningSeatId(String screeningSeatId);

    List<Ticket> findAllByStatusAndExpiresAtBefore(TicketStatus status, Instant time);

    boolean existsByTicketCode(String ticketCode);

    List<Ticket> findByBooking_Customer_IdOrderByCreatedAtDesc(String customerId);

    @Query(
            """
	select t from Ticket t
	join fetch t.screeningSeat ss
	join fetch ss.seat s
	join fetch s.seatType st
	where t.id in :ids
	""")
    List<Ticket> findAllForEmail(@Param("ids") List<UUID> ids);

    // Find all tickets for a booking
    List<Ticket> findByBookingIdAndStatus(UUID bookingId, TicketStatus status);

    // Find tickets by screening seat IDs and status (for transfer tickets)
    List<Ticket> findByScreeningSeatIdInAndStatus(List<String> screeningSeatIds, TicketStatus status);
}
