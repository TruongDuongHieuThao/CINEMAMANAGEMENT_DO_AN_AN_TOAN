package com.theatermgnt.theatermgnt.schedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.theatermgnt.theatermgnt.schedule.dto.response.WorkScheduleResponse;
import com.theatermgnt.theatermgnt.schedule.entity.WorkSchedule;

@Mapper(componentModel = "spring")
public interface WorkScheduleMapper {

    @Mapping(source = "shiftType.id", target = "shiftTypeId")
    @Mapping(source = "shiftType.name", target = "shiftTypeName")
    @Mapping(source = "shiftType.startTime", target = "shiftStart")
    @Mapping(source = "shiftType.endTime", target = "shiftEnd")
    WorkScheduleResponse toResponse(WorkSchedule ws);
}
