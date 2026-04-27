package com.theatermgnt.theatermgnt.schedule.service;

import java.time.LocalDate;
import java.util.List;

import com.theatermgnt.theatermgnt.schedule.dto.request.CreateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.request.UpdateWorkScheduleRequest;
import com.theatermgnt.theatermgnt.schedule.dto.response.WorkScheduleResponse;

public interface WorkScheduleService {

    List<WorkScheduleResponse> createSchedules(String cinemaId, CreateWorkScheduleRequest req);

    List<WorkScheduleResponse> getSchedules(String cinemaId, LocalDate from, LocalDate to);

    List<WorkScheduleResponse> updateSchedules(
            String cinemaId, String shiftTypeId, LocalDate workDate, UpdateWorkScheduleRequest request);

    void deleteSchedules(String cinemaId, String shiftTypeId, LocalDate date);

    void deleteSchedule(String cinemaId, String scheduleId);
}
