package com.theatermgnt.theatermgnt.revenue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.revenue.service.RevenueReprocessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/revenue")
@RequiredArgsConstructor
@Slf4j
public class RevenueReprocessController {

    private final RevenueReprocessService reprocessService;

    @PostMapping("/reprocess")
    // @PreAuthorize("hasAuthority('SCOPE_REPORT_CREATE')") // Temporarily disabled for testing
    public ResponseEntity<ApiResponse<String>> reprocessAllRevenue() {
        log.info("Revenue reprocess requested");

        try {
            reprocessService.reprocessAllPayments();
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .result("Revenue reprocessing completed successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error during revenue reprocessing", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<String>builder()
                            .message("Error during reprocessing: " + e.getMessage())
                            .build());
        }
    }
}
