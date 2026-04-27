package com.theatermgnt.theatermgnt.booking.mapper;

import org.mapstruct.Mapper;

import com.theatermgnt.theatermgnt.booking.dto.response.BookingPricingResponse;
import com.theatermgnt.theatermgnt.booking.entity.Booking;

@Mapper(componentModel = "spring")
public interface BookingPricingMapper {
    default BookingPricingResponse toPricingResponse(Booking booking) {
        BookingPricingResponse res = new BookingPricingResponse();
        res.setBookingId(booking.getId());
        res.setSubTotal(booking.getSubtotal());
        return res;
    }
}
