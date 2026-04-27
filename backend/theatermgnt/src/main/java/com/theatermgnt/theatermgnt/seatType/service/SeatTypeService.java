package com.theatermgnt.theatermgnt.seatType.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeCreationRequest;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeUpdateRequest;
import com.theatermgnt.theatermgnt.seatType.dto.response.SeatTypeResponse;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;
import com.theatermgnt.theatermgnt.seatType.mapper.SeatTypeMapper;
import com.theatermgnt.theatermgnt.seatType.repository.SeatTypeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatTypeService {

    SeatTypeRepository seatTypeRepository;

    SeatTypeMapper seatTypeMapper;

    public SeatTypeResponse createSeatType(SeatTypeCreationRequest request) {

        if (seatTypeRepository.existsByTypeName(request.getTypeName()))
            throw new AppException(ErrorCode.SEATTYPE_EXISTED);

        SeatType seatType = seatTypeMapper.toSeatType(request);

        return seatTypeMapper.toSeatTypeResponse(seatTypeRepository.save(seatType));
    }

    public List<SeatTypeResponse> getSeatTypes() {
        return seatTypeRepository.findAll().stream()
                .map(seatTypeMapper::toSeatTypeResponse)
                .toList();
    }

    public SeatTypeResponse getSeatType(String seatTypeId) {
        return seatTypeMapper.toSeatTypeResponse(seatTypeRepository
                .findById(seatTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SEATTYPE_NOT_EXISTED)));
    }

    public void deleteSeatType(String seatTypeId) {
        seatTypeRepository.deleteById(seatTypeId);
    }

    public SeatTypeResponse updateSeatType(String seatTypeId, SeatTypeUpdateRequest request) {
        SeatType seatType = seatTypeRepository
                .findById(seatTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SEATTYPE_NOT_EXISTED));

        seatTypeMapper.updateSeatType(seatType, request);

        return seatTypeMapper.toSeatTypeResponse(seatTypeRepository.save(seatType));
    }
}
