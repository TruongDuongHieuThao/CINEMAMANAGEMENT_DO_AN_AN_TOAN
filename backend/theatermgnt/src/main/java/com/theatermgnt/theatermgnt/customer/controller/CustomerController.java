package com.theatermgnt.theatermgnt.customer.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerProfileUpdateRequest;
import com.theatermgnt.theatermgnt.customer.dto.request.StaffCreateCustomerAccountRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerLoyaltyPointsResponse;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/customers")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerController {
    CustomerService customerService;
    RegistrationService registrationService;

    @PostMapping
    public ApiResponse<CustomerResponse> staffCreateCustomerAccount(
            @Valid @RequestBody StaffCreateCustomerAccountRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(registrationService.StaffCreateCustomerAccount(request))
                .build();
    }

    @GetMapping("/{customerId}")
    ApiResponse<CustomerResponse> getCustomerProfile(@PathVariable String customerId) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerProfileById(customerId))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<CustomerResponse> getMyInfo() {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getMyInfo())
                .build();
    }

    @GetMapping
    ApiResponse<List<CustomerResponse>> getAllCustomers() {
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAll())
                .build();
    }

    @PutMapping("/{customerId}")
    ApiResponse<CustomerResponse> updateCustomerProfile(
            @PathVariable String customerId, @Valid @RequestBody CustomerProfileUpdateRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomerProfile(customerId, request))
                .build();
    }

    @GetMapping("/{customerId}/loyalty-points")
    ApiResponse<CustomerLoyaltyPointsResponse> getLoyaltyPoints(@PathVariable String customerId) {
        return ApiResponse.<CustomerLoyaltyPointsResponse>builder()
                .result(customerService.getLoyaltyPoints(customerId))
                .build();
    }

    @DeleteMapping("/{customerId}")
    ApiResponse<Void> deleteCustomerProfile(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ApiResponse.<Void>builder().build();
    }
}
