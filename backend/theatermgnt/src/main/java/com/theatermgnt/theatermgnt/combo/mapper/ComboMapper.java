package com.theatermgnt.theatermgnt.combo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;
import com.theatermgnt.theatermgnt.combo.entity.Combo;

@Mapper(componentModel = "spring")
public interface ComboMapper {
    Combo toCombo(ComboCreationRequest request);

    ComboResponse toComboResponse(Combo combo);

    void updateCombo(@MappingTarget Combo combo, ComboUpdateRequest request);
}
