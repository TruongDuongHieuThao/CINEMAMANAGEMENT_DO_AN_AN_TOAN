package com.theatermgnt.theatermgnt.ShiftType.controller;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.ShiftType.dto.request.CreateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.request.UpdateShiftTypeRequest;
import com.theatermgnt.theatermgnt.ShiftType.dto.response.ShiftTypeResponse;
import com.theatermgnt.theatermgnt.ShiftType.service.ShiftTypeService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/cinemas/{cinemaId}/shift-types")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ShiftTypeController {
    ShiftTypeService shiftTypeService;

    @PostMapping
    public ApiResponse<ShiftTypeResponse> createShiftTemplate(
            @PathVariable String cinemaId, @Valid @RequestBody CreateShiftTypeRequest request) {

        return ApiResponse.<ShiftTypeResponse>builder()
                .result(shiftTypeService.create(cinemaId, request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ShiftTypeResponse>> getShiftTemplates(@PathVariable String cinemaId) {

        return ApiResponse.<List<ShiftTypeResponse>>builder()
                .result(shiftTypeService.getAll(cinemaId))
                .build();
    }

    @GetMapping("/{shiftId}")
    public ApiResponse<ShiftTypeResponse> getShiftTemplateById(
            @PathVariable String cinemaId, @PathVariable String shiftId) {

        return ApiResponse.<ShiftTypeResponse>builder()
                .result(shiftTypeService.getById(cinemaId, shiftId))
                .build();
    }

    @PutMapping("/{shiftId}")
    public ApiResponse<ShiftTypeResponse> updateShiftTemplate(
            @PathVariable String cinemaId,
            @PathVariable String shiftId,
            @RequestBody @Valid UpdateShiftTypeRequest request) {

        return ApiResponse.<ShiftTypeResponse>builder()
                .result(shiftTypeService.update(cinemaId, shiftId, request))
                .build();
    }

    @DeleteMapping("/{shiftId}")
    public ApiResponse<String> deleteShiftTemplate(@PathVariable String cinemaId, @PathVariable String shiftId) {

        shiftTypeService.delete(cinemaId, shiftId);
        return ApiResponse.<String>builder()
                .result("Delete shift type id: " + shiftId + " successfully")
                .build();
    }
}
