package com.theatermgnt.theatermgnt.ShiftType.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.ShiftType.entity.ShiftType;

public interface ShiftTypeRepository extends JpaRepository<ShiftType, String> {

    List<ShiftType> findByCinemaIdAndDeletedFalse(String cinemaId);

    Optional<ShiftType> findByIdAndCinemaIdAndDeletedFalse(String id, String cinemaId);

    boolean existsByCinemaIdAndNameIgnoreCaseAndDeletedFalse(String cinemaId, String name);
}
