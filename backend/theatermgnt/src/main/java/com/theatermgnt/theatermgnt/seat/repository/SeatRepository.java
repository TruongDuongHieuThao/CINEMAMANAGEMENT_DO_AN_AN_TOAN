package com.theatermgnt.theatermgnt.seat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.seat.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, String> {
    List<Seat> findByRoomId(String roomId);

    boolean existsByRowChairAndSeatNumberAndRoomId(String rowChair, Integer seatNumber, String roomId);

    long countByRoomId(String roomId);
}
