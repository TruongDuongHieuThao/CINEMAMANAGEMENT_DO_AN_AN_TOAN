package com.theatermgnt.theatermgnt.combo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.entity.ComboItem;

@Mapper(componentModel = "spring")
public interface ComboItemMapper {
    ComboItem toComboItem(ComboItemCreationRequest request);

    @Mapping(target = "comboName", source = "combo.name")
    ComboItemResponse toComboItemResponse(ComboItem comboItem);

    void updateComboItem(@MappingTarget ComboItem comboItem, ComboItemUpdateRequest request);
}
