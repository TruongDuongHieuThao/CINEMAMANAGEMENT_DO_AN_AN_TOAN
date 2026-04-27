package com.theatermgnt.theatermgnt.ShiftType.service;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.ShiftType.dto.request.CreateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.request.UpdateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.response.ShiftTypeResponse;
import com.theatermgnt.theatermgnt.ShiftType.entity.ShiftType;
import com.theatermgnt.theatermgnt.ShiftType.mapper.ShiftTypeMapper;
import com.theatermgnt.theatermgnt.ShiftType.repository.ShiftTypeRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ShiftTypeServiceImpl implements ShiftTypeService {
    ShiftTypeRepository repository;
    ShiftTypeMapper mapper;

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public ShiftTypeResponse create(String cinemaId, CreateShiftTypeRequest request) {

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (repository.existsByCinemaIdAndNameIgnoreCaseAndDeletedFalse(cinemaId, request.getName())) {
            throw new AppException(ErrorCode.SHIFT_TYPE_EXISTS);
        }

        ShiftType entity = mapper.toEntity(request);
        entity.setCinemaId(cinemaId);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public List<ShiftTypeResponse> getAll(String cinemaId) {
        return repository.findByCinemaIdAndDeletedFalse(cinemaId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ShiftTypeResponse getById(String cinemaId, String shiftId) {
        ShiftType entity = repository
                .findByIdAndCinemaIdAndDeletedFalse(shiftId, cinemaId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_TYPE_NOT_FOUND));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public ShiftTypeResponse update(String cinemaId, String shiftId, UpdateShiftTypeRequest request) {

        ShiftType entity = repository
                .findByIdAndCinemaIdAndDeletedFalse(shiftId, cinemaId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_TYPE_NOT_FOUND));
        if (request.getName() != null) {
            repository.existsByCinemaIdAndNameIgnoreCaseAndDeletedFalse(cinemaId, request.getName());
        }

        // Validate time range when needed
        if (request.getStartTime() != null || request.getEndTime() != null) {
            LocalTime start = request.getStartTime() != null ? request.getStartTime() : entity.getStartTime();
            LocalTime end = request.getEndTime() != null ? request.getEndTime() : entity.getEndTime();
            validateTimeRange(start, end);
        }
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public void delete(String cinemaId, String shiftId) {
        ShiftType entity = repository
                .findByIdAndCinemaIdAndDeletedFalse(shiftId, cinemaId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_TYPE_NOT_FOUND));

        // Soft delete
        entity.setDeleted(true);
        repository.save(entity);
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new AppException(ErrorCode.INVALID_SHIFT_TIME_RANGE);
        }
    }
}
