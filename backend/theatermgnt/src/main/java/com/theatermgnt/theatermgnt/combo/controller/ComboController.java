package com.theatermgnt.theatermgnt.combo.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;
import com.theatermgnt.theatermgnt.combo.service.ComboService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/combos")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboController {
    ComboService comboService;

    @PostMapping
    ApiResponse<ComboResponse> createCombo(@RequestBody @Valid ComboCreationRequest request) {
        return ApiResponse.<ComboResponse>builder()
                .result(comboService.createCombo(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<ComboResponse>> getCombos() {
        return ApiResponse.<List<ComboResponse>>builder()
                .result(comboService.getCombos())
                .build();
    }

    @GetMapping("/{comboId}")
    ApiResponse<ComboResponse> getCombo(@PathVariable("comboId") String comboId) {
        return ApiResponse.<ComboResponse>builder()
                .result(comboService.getCombo(comboId))
                .build();
    }

    @DeleteMapping("/{comboId}")
    ApiResponse<String> deleteCombo(@PathVariable("comboId") String comboId) {
        comboService.deleteCombo(comboId);
        return ApiResponse.<String>builder().result("Delete combo successfully").build();
    }

    @PutMapping("/{comboId}")
    ApiResponse<ComboResponse> updateUser(
            @PathVariable String comboId, @RequestBody @Valid ComboUpdateRequest request) {
        return ApiResponse.<ComboResponse>builder()
                .result(comboService.updateCombo(comboId, request))
                .build();
    }
}
