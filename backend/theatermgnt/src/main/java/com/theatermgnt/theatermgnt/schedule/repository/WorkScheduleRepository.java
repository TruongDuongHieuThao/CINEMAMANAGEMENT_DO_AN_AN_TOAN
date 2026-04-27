package com.theatermgnt.theatermgnt.schedule.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.schedule.entity.WorkSchedule;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, String> {

    List<WorkSchedule> findByCinemaIdAndWorkDateBetween(String cinemaId, LocalDate from, LocalDate to);

    Optional<WorkSchedule> findByIdAndCinemaId(String id, String cinemaId);

    boolean existsByUserIdAndWorkDateAndShiftType_Id(String userId, LocalDate workDate, String shiftTypeId);

    boolean existsByUserIdAndWorkDateAndShiftTypeId(String userId, LocalDate workDate, String shiftTypeId);

    List<WorkSchedule> findAllByCinemaIdAndShiftTypeIdAndWorkDate(
            String cinemaId, String shiftTypeId, LocalDate workDate);

    List<WorkSchedule> findAllByWorkDate(LocalDate workDate);
}
