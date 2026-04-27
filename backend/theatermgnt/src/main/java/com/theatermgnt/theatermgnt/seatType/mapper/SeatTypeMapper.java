package com.theatermgnt.theatermgnt.seatType.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeCreationRequest;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeUpdateRequest;
import com.theatermgnt.theatermgnt.seatType.dto.response.SeatTypeResponse;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;

@Mapper(componentModel = "spring")
public interface SeatTypeMapper {
    SeatType toSeatType(SeatTypeCreationRequest request);

    SeatTypeResponse toSeatTypeResponse(SeatType seatType);

    void updateSeatType(@MappingTarget SeatType seatType, SeatTypeUpdateRequest request);
}
