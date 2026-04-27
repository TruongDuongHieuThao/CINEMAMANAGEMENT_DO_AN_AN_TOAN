package com.theatermgnt.theatermgnt.booking.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingListResponse {
    private List<BookingListItemResponse> bookings;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
