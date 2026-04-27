package com.theatermgnt.theatermgnt.authentication.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import com.theatermgnt.theatermgnt.authentication.dto.request.AuthenticationRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.IntrospectRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.LogoutRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.RefreshTokenRequest;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.dto.response.IntrospectResponse;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.authentication.service.AuthenticationService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/admin/login")
    ApiResponse<AuthenticationResponse> adminLogin(@RequestBody AuthenticationRequest request) {
        log.info("Admin login attempt for: {}", request.getLoginIdentifier());
        var result = authenticationService.authenticate(request, AccountType.INTERNAL);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/customer/login")
    ApiResponse<AuthenticationResponse> customerLogin(@RequestBody AuthenticationRequest request) {
        log.info("Customer login attempt for: {}", request.getLoginIdentifier());
        var result = authenticationService.authenticate(request, AccountType.CUSTOMER);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
}
