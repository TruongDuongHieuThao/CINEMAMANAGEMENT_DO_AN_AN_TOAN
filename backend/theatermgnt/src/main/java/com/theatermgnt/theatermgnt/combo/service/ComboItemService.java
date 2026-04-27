package com.theatermgnt.theatermgnt.combo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.entity.Combo;
import com.theatermgnt.theatermgnt.combo.entity.ComboItem;
import com.theatermgnt.theatermgnt.combo.mapper.ComboItemMapper;
import com.theatermgnt.theatermgnt.combo.repository.ComboItemRepository;
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
public class ComboItemService {
    ComboItemRepository comboItemRepository;
    ComboRepository comboRepository;
    ComboItemMapper comboItemMapper;

    public ComboItemResponse createComboItem(ComboItemCreationRequest request) {
        Combo combo = comboRepository
                .findById(request.getComboId())
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_EXISTED));

        if (comboItemRepository.existsByNameAndComboId(request.getName(), request.getComboId()))
            throw new AppException(ErrorCode.COMBO_ITEM_EXISTED);

        ComboItem comboItem = comboItemMapper.toComboItem(request);
        comboItem.setCombo(combo);

        return comboItemMapper.toComboItemResponse(comboItemRepository.save(comboItem));
    }

    public List<ComboItemResponse> getComboItemsByCombo(String comboId) {
        return comboItemRepository.findByComboId(comboId).stream()
                .map(comboItemMapper::toComboItemResponse)
                .toList();
    }

    public List<ComboItemResponse> getComboItems() {
        return comboItemRepository.findAll().stream()
                .map(comboItemMapper::toComboItemResponse)
                .toList();
    }

    public ComboItemResponse getComboItem(String comboItemId) {
        ComboItem comboItem = comboItemRepository
                .findById(comboItemId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_ITEM_NOT_EXISTED));
        return comboItemMapper.toComboItemResponse(comboItem);
    }

    public ComboItemResponse updateComboItem(String comboItemId, ComboItemUpdateRequest request) {
        ComboItem comboItem = comboItemRepository
                .findById(comboItemId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_ITEM_NOT_EXISTED));

        comboItemMapper.updateComboItem(comboItem, request);
        return comboItemMapper.toComboItemResponse(comboItemRepository.save(comboItem));
    }

    public void deleteComboItem(String comboItemId) {
        if (!comboItemRepository.existsById(comboItemId)) throw new AppException(ErrorCode.COMBO_ITEM_NOT_EXISTED);
        comboItemRepository.deleteById(comboItemId);
    }
}
