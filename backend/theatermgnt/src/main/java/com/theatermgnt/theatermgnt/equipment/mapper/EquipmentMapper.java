package com.theatermgnt.theatermgnt.equipment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentResponse;
import com.theatermgnt.theatermgnt.equipment.entity.Equipment;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {
    Equipment toEquipment(EquipmentCreationRequest request);

    EquipmentResponse toEquipmentResponse(Equipment equipment);

    void updateEquipment(@MappingTarget Equipment equipment, EquipmentUpdateRequest request);
}
