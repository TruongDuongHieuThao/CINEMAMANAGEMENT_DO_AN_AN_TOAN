package com.theatermgnt.theatermgnt.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaCreationRequest;
import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaUpdateRequest;
import com.theatermgnt.theatermgnt.cinema.dto.response.CinemaResponse;
import com.theatermgnt.theatermgnt.cinema.entity.Cinema;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    @Mapping(target = "manager", ignore = true)
    Cinema toCinemas(CinemaCreationRequest request);

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(getManagerFullName(cinema))")
    @Mapping(target = "managerDob", source = "manager.dob")
    @Mapping(target = "buffer", source = "buffer")
    CinemaResponse toCinemaResponse(Cinema cinema);

    @Mapping(target = "manager", ignore = true)
    void updateCinema(@MappingTarget Cinema cinema, CinemaUpdateRequest request);

    default String getManagerFullName(Cinema cinema) {
        if (cinema.getManager() == null) {
            return null;
        }
        return cinema.getManager().getFirstName() + " " + cinema.getManager().getLastName();
    }
}
