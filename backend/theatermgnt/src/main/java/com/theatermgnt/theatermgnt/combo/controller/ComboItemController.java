package com.theatermgnt.theatermgnt.combo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.service.ComboItemService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/comboItems")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboItemController {
    ComboItemService comboItemService;

    @PostMapping
    ApiResponse<ComboItemResponse> createComboItem(@RequestBody @Valid ComboItemCreationRequest request) {
        return ApiResponse.<ComboItemResponse>builder()
                .result(comboItemService.createComboItem(request))
                .build();
    }

    @GetMapping("/combo/{comboId}")
    ApiResponse<List<ComboItemResponse>> getComboItemsByCombo(@PathVariable String comboId) {
        return ApiResponse.<List<ComboItemResponse>>builder()
                .result(comboItemService.getComboItemsByCombo(comboId))
                .build();
    }

    @GetMapping("/{comboItemId}")
    ApiResponse<ComboItemResponse> getComboItem(@PathVariable String comboItemId) {
        return ApiResponse.<ComboItemResponse>builder()
                .result(comboItemService.getComboItem(comboItemId))
                .build();
    }

    @GetMapping
    ApiResponse<List<ComboItemResponse>> getComboItems() {
        return ApiResponse.<List<ComboItemResponse>>builder()
                .result(comboItemService.getComboItems())
                .build();
    }

    @PutMapping("/{comboItemId}")
    ApiResponse<ComboItemResponse> updateComboItem(
            @PathVariable String comboItemId, @RequestBody @Valid ComboItemUpdateRequest request) {
        return ApiResponse.<ComboItemResponse>builder()
                .result(comboItemService.updateComboItem(comboItemId, request))
                .build();
    }

    @DeleteMapping("/{comboItemId}")
    ApiResponse<String> deleteComboItem(@PathVariable String comboItemId) {
        comboItemService.deleteComboItem(comboItemId);
        return ApiResponse.<String>builder()
                .result("Delete ComboItem successfully")
                .build();
    }
}
