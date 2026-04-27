package com.theatermgnt.theatermgnt.authorization.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.authorization.dto.request.PermissionRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.PermissionResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.mapper.PermissionMapper;
import com.theatermgnt.theatermgnt.authorization.repository.PermissionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    ///  CREATE A PERMISSION
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    ///  GET ALL PERMISSIONS
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }
    /// DELETE A PERMISSION
    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}
