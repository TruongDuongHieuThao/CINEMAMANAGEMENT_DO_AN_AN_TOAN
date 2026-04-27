package com.theatermgnt.theatermgnt.room.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.room.dto.request.RoomCreationRequest;
import com.theatermgnt.theatermgnt.room.dto.request.RoomUpdateRequest;
import com.theatermgnt.theatermgnt.room.dto.response.RoomResponse;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.seat.mapper.SeatMapper;

@Mapper(
        componentModel = "spring",
        uses = {SeatMapper.class})
public interface RoomMapper {
    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "seats", ignore = true)
    Room toRoom(RoomCreationRequest request);

    @Mapping(target = "cinemaName", source = "cinema.name")
    @Mapping(target = "cinemaId", source = "cinema.id")
    @Mapping(target = "seats", ignore = true)
    RoomResponse toRoomResponse(Room room);

    @Mapping(target = "cinemaName", source = "cinema.name")
    @Mapping(target = "cinemaId", source = "cinema.id")
    RoomResponse toRoomResponseWithSeats(Room room);

    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateRoom(@MappingTarget Room room, RoomUpdateRequest request);
}
