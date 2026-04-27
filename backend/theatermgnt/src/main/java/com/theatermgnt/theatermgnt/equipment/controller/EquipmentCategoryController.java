package com.theatermgnt.theatermgnt.equipment.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCategoryUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentCategoryResponse;
import com.theatermgnt.theatermgnt.equipment.service.EquipmentCategoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/equipment-categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EquipmentCategoryController {

    EquipmentCategoryService equipmentCategoryService;

    @PostMapping
    public ResponseEntity<EquipmentCategoryResponse> createCategory(
            @Valid @RequestBody EquipmentCategoryCreationRequest request) {
        log.info("Creating new equipment category: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentCategoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentCategoryResponse>> getCategories() {
        log.info("Fetching all equipment categories");
        return ResponseEntity.ok(equipmentCategoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<EquipmentCategoryResponse> getCategory(@PathVariable String categoryId) {
        log.info("Fetching equipment category: {}", categoryId);
        return ResponseEntity.ok(equipmentCategoryService.getCategory(categoryId));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<EquipmentCategoryResponse> updateCategory(
            @PathVariable String categoryId, @Valid @RequestBody EquipmentCategoryUpdateRequest request) {
        log.info("Updating equipment category: {}", categoryId);
        return ResponseEntity.ok(equipmentCategoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String categoryId) {
        log.info("Deleting equipment category: {}", categoryId);
        equipmentCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
