package com.theatermgnt.theatermgnt.authorization.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.authorization.dto.request.RoleRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.RoleResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    void updateRole(RoleRequest request, @MappingTarget Role role);
}
