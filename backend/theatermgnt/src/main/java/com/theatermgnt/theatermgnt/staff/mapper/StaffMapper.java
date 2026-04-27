package com.theatermgnt.theatermgnt.staff.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffProfileUpdateRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;
import com.theatermgnt.theatermgnt.staff.entity.Staff;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Staff toStaff(StaffAccountCreationRequest request);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.accountType", target = "accountType")
    @Mapping(source = "id", target = "staffId")
    StaffResponse toStaffResponse(Staff staff);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    void updateStaffProfile(@MappingTarget Staff staff, StaffProfileUpdateRequest request);
}
