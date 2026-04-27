package com.theatermgnt.theatermgnt.seatType.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.seatType.entity.SeatType;

public interface SeatTypeRepository extends JpaRepository<SeatType, String> {
    boolean existsByTypeName(String typeName);
}
