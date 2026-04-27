package com.theatermgnt.theatermgnt.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.theatermgnt.theatermgnt.booking.dto.response.CreateBookingResponse;
import com.theatermgnt.theatermgnt.booking.entity.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "customer.id", target = "customerId")
    CreateBookingResponse toCreateBookingResponse(Booking booking);
}
