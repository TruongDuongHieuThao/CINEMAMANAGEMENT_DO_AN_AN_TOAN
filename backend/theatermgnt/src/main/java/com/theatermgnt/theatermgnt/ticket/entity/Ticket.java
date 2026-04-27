package com.theatermgnt.theatermgnt.ticket.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import com.theatermgnt.theatermgnt.ticket.enums.TicketStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {@UniqueConstraint(columnNames = "ticket_code")},
        indexes = {
            @Index(name = "idx_ticket_booking", columnList = "booking_id"),
            @Index(name = "idx_ticket_status", columnList = "status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_seat_id", nullable = false, unique = true)
    ScreeningSeat screeningSeat;

    @Column(name = "seat_name", length = 10)
    String seatName;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 50)
    String ticketCode;

    @Column(name = "qr_content", nullable = false, columnDefinition = "TEXT")
    String qrContent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "used_at")
    Instant usedAt;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;
}
