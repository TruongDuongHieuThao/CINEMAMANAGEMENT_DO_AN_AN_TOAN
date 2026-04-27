package com.theatermgnt.theatermgnt.combo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;
import com.theatermgnt.theatermgnt.combo.entity.Combo;
import com.theatermgnt.theatermgnt.combo.mapper.ComboMapper;
import com.theatermgnt.theatermgnt.combo.repository.ComboRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboService {

    ComboRepository comboRepository;

    ComboMapper comboMapper;

    public ComboResponse createCombo(ComboCreationRequest request) {

        if (comboRepository.existsByName(request.getName())) throw new AppException(ErrorCode.COMBO_EXISTED);

        Combo combo = comboMapper.toCombo(request);

        return comboMapper.toComboResponse(comboRepository.save(combo));
    }

    public List<ComboResponse> getCombos() {
        return comboRepository.findAll().stream()
                .map(comboMapper::toComboResponse)
                .toList();
    }

    public ComboResponse getCombo(String comboId) {
        return comboMapper.toComboResponse(
                comboRepository.findById(comboId).orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_EXISTED)));
    }

    public void deleteCombo(String comboId) {
        comboRepository.deleteById(comboId);
    }

    public ComboResponse updateCombo(String comboId, ComboUpdateRequest request) {
        Combo combo =
                comboRepository.findById(comboId).orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_EXISTED));

        comboMapper.updateCombo(combo, request);

        return comboMapper.toComboResponse(comboRepository.save(combo));
    }
}
