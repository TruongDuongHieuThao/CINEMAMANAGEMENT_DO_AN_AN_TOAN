package com.theatermgnt.theatermgnt.priceConfig.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.priceConfig.dto.request.PriceConfigCreationRequest;
import com.theatermgnt.theatermgnt.priceConfig.dto.request.PriceConfigUpdateRequest;
import com.theatermgnt.theatermgnt.priceConfig.dto.response.PriceConfigResponse;
import com.theatermgnt.theatermgnt.priceConfig.entity.PriceConfig;
import com.theatermgnt.theatermgnt.priceConfig.mapper.PriceConfigMapper;
import com.theatermgnt.theatermgnt.priceConfig.repository.PriceConfigRepository;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;
import com.theatermgnt.theatermgnt.seatType.repository.SeatTypeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceConfigService {
    PriceConfigRepository priceConfigRepository;
    SeatTypeRepository seatTypeRepository;
    PriceConfigMapper priceConfigMapper;

    public PriceConfigResponse createPriceConfig(PriceConfigCreationRequest request) {
        SeatType seatType = seatTypeRepository
                .findById(request.getSeatTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SEATTYPE_NOT_EXISTED));

        PriceConfig priceConfig = priceConfigMapper.toPriceConfig(request);
        priceConfig.setSeatType(seatType);

        return priceConfigMapper.toPriceConfigResponse(priceConfigRepository.save(priceConfig));
    }

    public List<PriceConfigResponse> getPriceConfigsBySeatType(String seatTypeId) {
        return priceConfigRepository.findBySeatTypeId(seatTypeId).stream()
                .map(priceConfigMapper::toPriceConfigResponse)
                .toList();
    }

    public List<PriceConfigResponse> getPriceConfigs() {
        return priceConfigRepository.findAll().stream()
                .map(priceConfigMapper::toPriceConfigResponse)
                .toList();
    }

    public PriceConfigResponse getPriceConfig(String priceConfigId) {
        PriceConfig priceConfig = priceConfigRepository
                .findById(priceConfigId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICECONFIG_NOT_EXISTED));
        return priceConfigMapper.toPriceConfigResponse(priceConfig);
    }

    public PriceConfigResponse updatePriceConfig(String priceConfigId, PriceConfigUpdateRequest request) {
        PriceConfig priceConfig = priceConfigRepository
                .findById(priceConfigId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICECONFIG_NOT_EXISTED));

        priceConfigMapper.updatePriceConfig(priceConfig, request);
        return priceConfigMapper.toPriceConfigResponse(priceConfigRepository.save(priceConfig));
    }

    public void deletePriceConfig(String priceConfigId) {
        if (!priceConfigRepository.existsById(priceConfigId)) throw new AppException(ErrorCode.PRICECONFIG_NOT_EXISTED);
        priceConfigRepository.deleteById(priceConfigId);
    }
}
