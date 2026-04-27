package com.theatermgnt.theatermgnt.payment.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.theatermgnt.theatermgnt.payment.dto.request.CreateInvoiceRequest;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceDetailResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceResponse;
import com.theatermgnt.theatermgnt.payment.dto.response.InvoiceStatisticsResponse;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoice(String invoiceId);

    InvoiceDetailResponse getInvoiceDetail(String invoiceId);

    InvoiceResponse getInvoiceByBookingId(String bookingId);

    InvoiceResponse updateInvoiceStatus(String invoiceId, InvoiceStatus status);

    InvoiceResponse markAsPaid(String invoiceId);

    InvoiceResponse markAsFailed(String invoiceId);

    Page<InvoiceResponse> getAllInvoices(int page, int size, String cinemaId);

    Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, int page, int size, String cinemaId);

    Page<InvoiceResponse> getInvoicesByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, String cinemaId);

    Page<InvoiceResponse> searchInvoices(String search, int page, int size, String cinemaId);

    Page<InvoiceResponse> searchInvoicesByStatus(
            String search, InvoiceStatus status, int page, int size, String cinemaId);

    InvoiceStatisticsResponse getStatistics(String cinemaId);
}
