package com.theatermgnt.theatermgnt.schedule.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.schedule.dto.request.CreateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.request.UpdateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.response.WorkScheduleResponse;
import com.theatermgnt.theatermgnt.schedule.service.WorkScheduleService;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("cinemas/{cinemaId}/schedules")
@RequiredArgsConstructor
@Builder
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleController {

    WorkScheduleService scheduleService;

    @GetMapping
    public ApiResponse<List<WorkScheduleResponse>> getSchedules(
            @PathVariable String cinemaId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {

        return ApiResponse.<List<WorkScheduleResponse>>builder()
                .result(scheduleService.getSchedules(cinemaId, from, to))
                .build();
    }

    @PutMapping("/shifts/{shiftTypeId}/date/{workDate}")
    public ApiResponse<List<WorkScheduleResponse>> updateShiftInstance(
            @PathVariable String cinemaId,
            @PathVariable String shiftTypeId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workDate,
            @Valid @RequestBody UpdateWorkScheduleRequest request) {
        var result = scheduleService.updateSchedules(cinemaId, shiftTypeId, workDate, request);

        return ApiResponse.<List<WorkScheduleResponse>>builder().result(result).build();
    }

    @PostMapping
    public ApiResponse<List<WorkScheduleResponse>> createSchedules(
            @PathVariable String cinemaId, @Valid @RequestBody CreateWorkScheduleRequest request) {

        return ApiResponse.<List<WorkScheduleResponse>>builder()
                .result(scheduleService.createSchedules(cinemaId, request))
                .build();
    }

    @DeleteMapping("/shifts/{shiftTypeId}/date/{workDate}")
    public ApiResponse<String> deleteWorkSchedules(
            @PathVariable String cinemaId,
            @PathVariable String shiftTypeId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workDate) {
        scheduleService.deleteSchedules(cinemaId, shiftTypeId, workDate);
        return ApiResponse.<String>builder()
                .result("Deleted entire shift instance")
                .build();
    }

    @DeleteMapping("/{scheduleId}")
    public ApiResponse<String> deleteSchedule(@PathVariable String cinemaId, @PathVariable String scheduleId) {

        scheduleService.deleteSchedule(cinemaId, scheduleId);

        return ApiResponse.<String>builder()
                .result("Deleted schedule: " + scheduleId)
                .build();
    }
}
