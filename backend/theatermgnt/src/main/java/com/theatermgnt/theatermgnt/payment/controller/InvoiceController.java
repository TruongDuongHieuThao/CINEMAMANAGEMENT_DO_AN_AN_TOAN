package com.theatermgnt.theatermgnt.payment.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceDetailResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceStatisticsResponse;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.service.InvoiceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<Page<InvoiceResponse>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cinemaId) {
        log.info("Getting all invoices - page: {}, size: {}", page, size);
        return ApiResponse.<Page<InvoiceResponse>>builder()
                .result(invoiceService.getAllInvoices(page, size, cinemaId))
                .build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<Page<InvoiceResponse>> searchInvoices(
            @RequestParam String query,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String cinemaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching invoices: {} with status: {} - page: {}, size: {}", query, status, page, size);

        Page<InvoiceResponse> result;
        if (status != null) {
            result = invoiceService.searchInvoicesByStatus(query, status, page, size, cinemaId);
        } else {
            result = invoiceService.searchInvoices(query, page, size, cinemaId);
        }

        return ApiResponse.<Page<InvoiceResponse>>builder().result(result).build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<Page<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status,
            @RequestParam(required = false) String cinemaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting invoices by status: {} - page: {}, size: {}", status, page, size);
        return ApiResponse.<Page<InvoiceResponse>>builder()
                .result(invoiceService.getInvoicesByStatus(status, page, size, cinemaId))
                .build();
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<Page<InvoiceResponse>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String cinemaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting invoices by date range: {} to {} - page: {}, size: {}", startDate, endDate, page, size);
        return ApiResponse.<Page<InvoiceResponse>>builder()
                .result(invoiceService.getInvoicesByDateRange(startDate, endDate, page, size, cinemaId))
                .build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<InvoiceStatisticsResponse> getStatistics(@RequestParam(required = false) String cinemaId) {
        log.info("Getting invoice statistics for cinemaId: {}", cinemaId);
        return ApiResponse.<InvoiceStatisticsResponse>builder()
                .result(invoiceService.getStatistics(cinemaId))
                .build();
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable String invoiceId) {
        log.info("Getting invoice: {}", invoiceId);
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoice(invoiceId))
                .build();
    }

    @GetMapping("/{invoiceId}/detail")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<InvoiceDetailResponse> getInvoiceDetail(@PathVariable String invoiceId) {
        log.info("Getting invoice detail: {}", invoiceId);
        return ApiResponse.<InvoiceDetailResponse>builder()
                .result(invoiceService.getInvoiceDetail(invoiceId))
                .build();
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAuthority('INVOICE_READ')")
    public ApiResponse<InvoiceResponse> getInvoiceByBookingId(@PathVariable String bookingId) {
        log.info("Getting invoice by booking: {}", bookingId);
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceByBookingId(bookingId))
                .build();
    }

    @PatchMapping("/{invoiceId}/status")
    @PreAuthorize("hasAuthority('INVOICE_UPDATE')")
    public ApiResponse<InvoiceResponse> updateInvoiceStatus(
            @PathVariable String invoiceId, @RequestParam InvoiceStatus status) {
        log.info("Updating invoice {} status to: {}", invoiceId, status);
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.updateInvoiceStatus(invoiceId, status))
                .build();
    }
}
