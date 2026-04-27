package com.theatermgnt.theatermgnt.equipment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentCategoryResponse;
import com.theatermgnt.theatermgnt.equipment.entity.EquipmentCategory;

@Mapper(componentModel = "spring")
public interface EquipmentCategoryMapper {
    EquipmentCategory toEquipmentCategory(EquipmentCategoryCreationRequest request);

    EquipmentCategoryResponse toEquipmentCategoryResponse(EquipmentCategory equipmentCategory);

    void updateEquipmentCategory(
            @MappingTarget EquipmentCategory equipmentCategory, EquipmentCategoryUpdateRequest request);
}
