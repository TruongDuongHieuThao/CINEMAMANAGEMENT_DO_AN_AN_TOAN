package com.theatermgnt.theatermgnt.authorization.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.authorization.dto.request.PermissionRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.PermissionResponse;
import com.theatermgnt.theatermgnt.authorization.service.PermissionService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/permissions")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    /// Create permission
    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }
    /// Get all permissions
    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    /// Delete permission
    @DeleteMapping("/{permissionId}")
    ApiResponse<Void> delete(@PathVariable String permissionId) {
        permissionService.delete(permissionId);
        return ApiResponse.<Void>builder().build();
    }
}
