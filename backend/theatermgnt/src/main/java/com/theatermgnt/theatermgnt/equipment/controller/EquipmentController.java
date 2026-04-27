package com.theatermgnt.theatermgnt.equipment.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentResponse;
import com.theatermgnt.theatermgnt.equipment.service.EquipmentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/equipments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EquipmentController {

    EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<EquipmentResponse> createEquipment(@Valid @RequestBody EquipmentCreationRequest request) {
        log.info("Creating new equipment: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(request));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment() {
        log.info("Fetching all equipment");
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @GetMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> getEquipment(@PathVariable String equipmentId) {
        log.info("Fetching equipment: {}", equipmentId);
        return ResponseEntity.ok(equipmentService.getEquipment(equipmentId));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentByRoom(@PathVariable String roomId) {
        log.info("Fetching equipment for room: {}", roomId);
        return ResponseEntity.ok(equipmentService.getEquipmentByRoom(roomId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentByCategory(@PathVariable String categoryId) {
        log.info("Fetching equipment for category: {}", categoryId);
        return ResponseEntity.ok(equipmentService.getEquipmentByCategory(categoryId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentByStatus(@PathVariable String status) {
        log.info("Fetching equipment with status: {}", status);
        return ResponseEntity.ok(equipmentService.getEquipmentByStatus(status));
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<EquipmentResponse>> getEquipmentByRoomAndStatus(
            @PathVariable String roomId, @PathVariable String status) {
        log.info("Fetching equipment for room: {} with status: {}", roomId, status);
        return ResponseEntity.ok(equipmentService.getEquipmentByRoomAndStatus(roomId, status));
    }

    @PutMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable String equipmentId, @Valid @RequestBody EquipmentUpdateRequest request) {
        log.info("Updating equipment: {}", equipmentId);
        return ResponseEntity.ok(equipmentService.updateEquipment(equipmentId, request));
    }

    @DeleteMapping("/{equipmentId}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable String equipmentId) {
        log.info("Deleting equipment: {}", equipmentId);
        equipmentService.deleteEquipment(equipmentId);
        return ResponseEntity.noContent().build();
    }
}
