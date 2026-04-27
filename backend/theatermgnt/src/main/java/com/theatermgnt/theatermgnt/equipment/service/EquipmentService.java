package com.theatermgnt.theatermgnt.equipment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentCreationRequest;
import com.theatermgnt.theatermgnt.equipment.dto.request.EquipmentUpdateRequest;
import com.theatermgnt.theatermgnt.equipment.dto.response.EquipmentResponse;
import com.theatermgnt.theatermgnt.equipment.entity.Equipment;
import com.theatermgnt.theatermgnt.equipment.mapper.EquipmentMapper;
import com.theatermgnt.theatermgnt.equipment.repository.EquipmentRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EquipmentService {

    EquipmentRepository equipmentRepository;
    EquipmentMapper equipmentMapper;

    public EquipmentResponse createEquipment(EquipmentCreationRequest request) {
        if (request.getSerialNumber() != null && equipmentRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new AppException(ErrorCode.EQUIPMENT_EXISTED);
        }

        Equipment equipment = equipmentMapper.toEquipment(request);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());

        return equipmentMapper.toEquipmentResponse(equipmentRepository.save(equipment));
    }

    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
    }

    public List<EquipmentResponse> getEquipmentByRoom(String roomId) {
        return equipmentRepository.findByRoomId(roomId).stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
    }

    public List<EquipmentResponse> getEquipmentByCategory(String categoryId) {
        return equipmentRepository.findByCategoryId(categoryId).stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
    }

    public List<EquipmentResponse> getEquipmentByStatus(String status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
    }

    public List<EquipmentResponse> getEquipmentByRoomAndStatus(String roomId, String status) {
        return equipmentRepository.findByRoomIdAndStatus(roomId, status).stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
    }

    public EquipmentResponse getEquipment(String equipmentId) {
        return equipmentMapper.toEquipmentResponse(equipmentRepository
                .findById(equipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_EXISTED)));
    }

    public EquipmentResponse updateEquipment(String equipmentId, EquipmentUpdateRequest request) {
        Equipment equipment = equipmentRepository
                .findById(equipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.EQUIPMENT_NOT_EXISTED));

        if (request.getSerialNumber() != null
                && !request.getSerialNumber().equals(equipment.getSerialNumber())
                && equipmentRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new AppException(ErrorCode.EQUIPMENT_EXISTED);
        }

        equipmentMapper.updateEquipment(equipment, request);
        equipment.setUpdatedAt(LocalDateTime.now());

        return equipmentMapper.toEquipmentResponse(equipmentRepository.save(equipment));
    }

    public void deleteEquipment(String equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new AppException(ErrorCode.EQUIPMENT_NOT_EXISTED);
        }
        equipmentRepository.deleteById(equipmentId);
    }
}
