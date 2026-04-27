package com.theatermgnt.theatermgnt.ticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.theatermgnt.theatermgnt.ticket.dto.response.TicketResponse;
import com.theatermgnt.theatermgnt.ticket.entity.Ticket;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "expiresAt", target = "expiresAt")
    @Mapping(source = "booking.screening.movie.title", target = "movieTitle")
    @Mapping(source = "booking.screening.startTime", target = "startTime")
    @Mapping(source = "booking.id", target = "bookingId")
    TicketResponse toResponse(Ticket ticket);
}
