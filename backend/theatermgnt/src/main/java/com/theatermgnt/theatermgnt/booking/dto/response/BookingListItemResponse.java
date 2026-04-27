package com.theatermgnt.theatermgnt.booking.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.theatermgnt.theatermgnt.booking.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListItemResponse {
    private UUID id;
    private String bookingCode;
    private UUID customerId;
    private String customerName;
    private String email;
    private String phone;
    private String movieTitle;
    private String cinemaId;
    private String cinemaName;
    private String roomName;
    private String screeningTime;
    private Integer seatCount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private Instant createdAt;
    private Instant expiredAt;
}
