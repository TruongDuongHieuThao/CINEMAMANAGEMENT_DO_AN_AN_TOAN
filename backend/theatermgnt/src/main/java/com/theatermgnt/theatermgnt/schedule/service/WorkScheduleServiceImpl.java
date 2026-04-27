package com.theatermgnt.theatermgnt.schedule.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.ShiftType.entity.ShiftType;
import com.theatermgnt.theatermgnt.ShiftType.repository.ShiftTypeRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.schedule.dto.request.CreateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.request.UpdateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.response.WorkScheduleResponse;
import com.theatermgnt.theatermgnt.schedule.entity.WorkSchedule;
import com.theatermgnt.theatermgnt.schedule.mapper.WorkScheduleMapper;
import com.theatermgnt.theatermgnt.schedule.repository.WorkScheduleRepository;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Builder
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleServiceImpl implements WorkScheduleService {

    WorkScheduleRepository workScheduleRepository;
    ShiftTypeRepository shiftTypeRepository;
    StaffRepository staffRepository;
    WorkScheduleMapper mapper;

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public List<WorkScheduleResponse> createSchedules(String cinemaId, CreateWorkScheduleRequest req) {

        if (req.getWorkDate() == null || req.getShiftTypeId() == null || req.getUserIds() == null) {
            throw new AppException(ErrorCode.INVALID_WORK_SCHEDULE_REQUEST);
        }
        if (req.getWorkDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_WORK_DATE);
        }

        ShiftType shiftType = shiftTypeRepository
                .findById(req.getShiftTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        validateNoOverlap(req.getWorkDate(), shiftType);
        List<WorkSchedule> created = new ArrayList<>();

        for (String userId : req.getUserIds()) {

            var staff = staffRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
            if (!staff.getCinemaId().equals(cinemaId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED_CINEMA_STAFF);
            }

            // Check duplicate
            boolean exists = workScheduleRepository.existsByUserIdAndWorkDateAndShiftTypeId(
                    userId, req.getWorkDate(), req.getShiftTypeId());

            if (exists) {
                throw new AppException(ErrorCode.WORK_SCHEDULE_EXISTS);
            }

            WorkSchedule ws = new WorkSchedule();
            ws.setCinemaId(cinemaId);
            ws.setUserId(userId);
            ws.setWorkDate(req.getWorkDate());
            ws.setShiftType(shiftType);

            created.add(ws);
        }
        workScheduleRepository.saveAll(created);

        return created.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public List<WorkScheduleResponse> updateSchedules(
            String cinemaId, String shiftTypeId, LocalDate workDate, UpdateWorkScheduleRequest req) {

        if (req.getShiftTypeId() == null && req.getWorkDate() == null) {
            throw new AppException(ErrorCode.NOTHING_TO_UPDATE);
        }

        // Ngày làm chỉnh sửa không được trước ngày hiện tại
        if (req.getWorkDate() != null && req.getWorkDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_WORK_DATE);
        }

        // Tìm schedule đang tồn tại
        List<WorkSchedule> schedules =
                workScheduleRepository.findAllByCinemaIdAndShiftTypeIdAndWorkDate(cinemaId, shiftTypeId, workDate);

        if (!schedules.getFirst().getCinemaId().equals(cinemaId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CINEMA_STAFF);
        }

        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }

        ShiftType newShift = (req.getShiftTypeId() != null)
                ? shiftTypeRepository
                        .findById(req.getShiftTypeId())
                        .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND))
                : schedules.getFirst().getShiftType();

        LocalDate newWorkDate = (req.getWorkDate() != null)
                ? req.getWorkDate()
                : schedules.getFirst().getWorkDate();
        validateNoOverlap(newWorkDate, newShift);

        schedules.forEach(s -> {
            s.setWorkDate(newWorkDate);
            s.setShiftType(newShift);

            // kiểm tra trùng lịch
            boolean exists = workScheduleRepository.existsByUserIdAndWorkDateAndShiftTypeId(
                    s.getUserId(), req.getWorkDate(), req.getShiftTypeId());

            if (exists) {
                throw new AppException(ErrorCode.WORK_SCHEDULE_EXISTS);
            }
        });

        workScheduleRepository.saveAll(schedules);
        return schedules.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<WorkScheduleResponse> getSchedules(String cinemaId, LocalDate from, LocalDate to) {
        return workScheduleRepository.findByCinemaIdAndWorkDateBetween(cinemaId, from, to).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteSchedules(String cinemaId, String shiftTypeId, LocalDate date) {
        List<WorkSchedule> schedules =
                workScheduleRepository.findAllByCinemaIdAndShiftTypeIdAndWorkDate(cinemaId, shiftTypeId, date);
        if (!schedules.getFirst().getCinemaId().equals(cinemaId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CINEMA_STAFF);
        }
        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }
        workScheduleRepository.deleteAll(schedules);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteSchedule(String cinemaId, String scheduleId) {
        WorkSchedule schedule = workScheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        if (!schedule.getCinemaId().equals(cinemaId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CINEMA_STAFF);
        }

        workScheduleRepository.delete(schedule);
    }

    private void validateNoOverlap(LocalDate date, ShiftType newShift) {

        List<WorkSchedule> schedules = workScheduleRepository.findAllByWorkDate(date);

        for (WorkSchedule ws : schedules) {
            ShiftType oldShift = ws.getShiftType();
            if (!newShift.getStartTime().equals(oldShift.getStartTime())
                    && !newShift.getEndTime().equals(oldShift.getEndTime())) {
                boolean overlap = newShift.getStartTime().isBefore(oldShift.getEndTime())
                        && newShift.getEndTime().isAfter(oldShift.getStartTime());

                if (overlap) {
                    throw new AppException(ErrorCode.SHIFT_OVERLAP);
                }
            }
        }
    }
}
