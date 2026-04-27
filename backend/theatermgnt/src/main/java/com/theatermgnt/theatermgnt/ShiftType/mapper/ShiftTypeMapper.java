package com.theatermgnt.theatermgnt.ShiftType.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.ShiftType.dto.request.CreateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.request.UpdateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.response.ShiftTypeResponse;
import com.theatermgnt.theatermgnt.ShiftType.entity.ShiftType;

@Mapper(componentModel = "spring")
public interface ShiftTypeMapper {

    @Mapping(target = "cinemaId", ignore = true)
    ShiftType toEntity(CreateShiftTypeRequest request);

    ShiftTypeResponse toResponse(ShiftType entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget ShiftType entity, UpdateShiftTypeRequest request);
}
