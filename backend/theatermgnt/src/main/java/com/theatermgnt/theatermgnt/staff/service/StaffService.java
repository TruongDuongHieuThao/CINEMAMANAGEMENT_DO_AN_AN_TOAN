package com.theatermgnt.theatermgnt.staff.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.service.AccountService;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.authorization.repository.RoleRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.constant.PredefinedRole;
import com.theatermgnt.theatermgnt.staff.dto.request.SearchStaffRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffProfileUpdateRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;
import com.theatermgnt.theatermgnt.staff.entity.Staff;
import com.theatermgnt.theatermgnt.staff.mapper.StaffMapper;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StaffService {
    StaffRepository staffRepository;
    StaffMapper staffMapper;
    RoleRepository roleRepository;
    AccountService accountService;

    /// CREATE STAFF PROFILE
    @Transactional
    //    @PreAuthorize("hasRole('ADMIN')")
    public Staff createStaffProfile(StaffAccountCreationRequest request, Account account, Set<Role> roles) {
        Staff staff = staffMapper.toStaff(request);
        staff.setAccount(account);
        staff.setRoles(roles);
        return staffRepository.save(staff);
    }

    /// GET ALL STAFF
    //    @PreAuthorize("hasRole('ADMIN')")
    public List<StaffResponse> getAll() {
        return staffRepository.findAll().stream()
                .map(staffMapper::toStaffResponse)
                .toList();
    }

    /// GET STAFF BY ID
    //    @PreAuthorize("hasRole('ADMIN')")
    public StaffResponse getStaffProfile(String staffId) {
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return staffMapper.toStaffResponse(staff);
    }

    /// GET MY INFO
    public StaffResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String accountId = context.getAuthentication().getName();

        Staff staff = staffRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return staffMapper.toStaffResponse(staff);
    }

    //    /// SEARCH STAFF BY NAME/EMAIL/PHONE
    public List<StaffResponse> searchStaff(SearchStaffRequest request) {
        List<Staff> staffs = staffRepository.findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                request.getKeyword(), request.getKeyword());
        return staffs.stream().map(staffMapper::toStaffResponse).toList();
    }

    /// UPDATE STAFF PROFILE
    public StaffResponse updateStaffProfile(String staffId, StaffProfileUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        boolean isCallerAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + PredefinedRole.ADMIN_ROLE));

        Staff staffToUpdate =
                staffRepository.findById(staffId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        staffMapper.updateStaffProfile(staffToUpdate, request);
        if (isCallerAdmin && request.getRoles() != null) {
            log.info("Updating roles for staff: {}", staffId);
            var roles = roleRepository.findAllById(request.getRoles());
            staffToUpdate.setRoles(new HashSet<>(roles));
        }
        return staffMapper.toStaffResponse(staffRepository.save(staffToUpdate));
    }
    /// DELETE STAFF
    public void deleteStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (staff.getAccount() != null) {
            accountService.deleteAccount(staff.getAccount().getId());
        }
        staffRepository.delete(staff);
    }

    /// GET STAFF BY CINEMA WITH ROLE STAFF
    public List<StaffResponse> getStaffByCinemaAndStaffRole(String cinemaId) {
        return staffRepository.findByCinemaIdAndRole(cinemaId, PredefinedRole.STAFF_ROLE).stream()
                .map(staffMapper::toStaffResponse)
                .toList();
    }

    public List<StaffResponse> getStaffRoleManagerDontManageAnyCinema() {
        return staffRepository.findByRoleAndNotManagingAnyCinema(PredefinedRole.MANAGER_ROLE).stream()
                .map(staffMapper::toStaffResponse)
                .toList();
    }
}
