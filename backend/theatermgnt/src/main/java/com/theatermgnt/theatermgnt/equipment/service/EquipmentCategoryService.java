package com.theatermgnt.theatermgnt.equipment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentCategoryResponse;
import com.theatermgnt.theatermgnt.equipment.entity.EquipmentCategory;
import com.theatermgnt.theatermgnt.equipment.mapper.EquipmentCategoryMapper;
import com.theatermgnt.theatermgnt.equipment.repository.EquipmentCategoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EquipmentCategoryService {

    EquipmentCategoryRepository equipmentCategoryRepository;
    EquipmentCategoryMapper equipmentCategoryMapper;

    public EquipmentCategoryResponse createCategory(EquipmentCategoryCreationRequest request) {
        if (equipmentCategoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.EQUIPMENT_CATEGORY_EXISTED);
        }

        EquipmentCategory category = equipmentCategoryMapper.toEquipmentCategory(request);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return equipmentCategoryMapper.toEquipmentCategoryResponse(equipmentCategoryRepository.save(category));
    }

    public List<EquipmentCategoryResponse> getCategories() {
        return equipmentCategoryRepository.findAll().stream()
                .map(equipmentCategoryMapper::toEquipmentCategoryResponse)
                .toList();
    }

    public EquipmentCategoryResponse getCategory(String categoryId) {
        return equipmentCategoryMapper.toEquipmentCategoryResponse(equipmentCategoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_CATEGORY_NOT_EXISTED)));
    }

    public EquipmentCategoryResponse updateCategory(String categoryId, EquipmentCategoryUpdateRequest request) {
        EquipmentCategory category = equipmentCategoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_CATEGORY_NOT_EXISTED));

        equipmentCategoryMapper.updateEquipmentCategory(category, request);
        category.setUpdatedAt(LocalDateTime.now());

        return equipmentCategoryMapper.toEquipmentCategoryResponse(equipmentCategoryRepository.save(category));
    }

    public void deleteCategory(String categoryId) {
        if (!equipmentCategoryRepository.existsById(categoryId)) {
            throw new AppException(ErrorCode.EQUIPMENT_CATEGORY_NOT_EXISTED);
        }
        equipmentCategoryRepository.deleteById(categoryId);
    }
}
