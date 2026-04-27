package com.theatermgnt.theatermgnt.staff.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.staff.dto.request.SearchStaffRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffProfileUpdateRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;
import com.theatermgnt.theatermgnt.staff.service.StaffService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/staffs")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StaffController {
    StaffService staffService;
    RegistrationService registrationService;

    @PostMapping
    public ApiResponse<StaffResponse> createStaff(@RequestBody @Valid StaffAccountCreationRequest request) {
        return ApiResponse.<StaffResponse>builder()
                .result(registrationService.registerStaffAccount(request))
                .build();
    }

    @GetMapping("/{staffId}")
    public ApiResponse<StaffResponse> getStaffProfile(@PathVariable String staffId) {
        return ApiResponse.<StaffResponse>builder()
                .result(staffService.getStaffProfile(staffId))
                .build();
    }

    @GetMapping("/available-managers")
    public ApiResponse<List<StaffResponse>> getAvailableManagers() {
        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffService.getStaffRoleManagerDontManageAnyCinema())
                .build();
    }

    @GetMapping("/cinema/{cinemaId}/staff-role")
    public ApiResponse<List<StaffResponse>> getStaffByCinemaWithStaffRole(@PathVariable String cinemaId) {
        var staffs = staffService.getStaffByCinemaAndStaffRole(cinemaId);
        return ApiResponse.<List<StaffResponse>>builder().result(staffs).build();
    }

    @GetMapping
    public ApiResponse<List<StaffResponse>> getAll() {
        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffService.getAll())
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<StaffResponse> getMyInfo() {
        return ApiResponse.<StaffResponse>builder()
                .result(staffService.getMyInfo())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<StaffResponse>> searchStaff(@ModelAttribute SearchStaffRequest request) {
        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffService.searchStaff(request))
                .build();
    }

    @PutMapping("/{staffId}")
    public ApiResponse<StaffResponse> updateStaffProfile(
            @PathVariable String staffId, @Valid @RequestBody StaffProfileUpdateRequest request) {
        return ApiResponse.<StaffResponse>builder()
                .result(staffService.updateStaffProfile(staffId, request))
                .build();
    }

    @DeleteMapping("/{staffId}")
    public ApiResponse<Void> deleteStaff(@PathVariable String staffId) {
        staffService.deleteStaff(staffId);
        return ApiResponse.<Void>builder().build();
    }
}
