package com.theatermgnt.theatermgnt.account.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerAccountCreationRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RegistrationController {
    RegistrationService registrationService;

    @PostMapping("/register")
    public ApiResponse<CustomerResponse> registerCustomerAccount(
            @Valid @RequestBody CustomerAccountCreationRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(registrationService.registerCustomerAccount(request))
                .build();
    }
}
