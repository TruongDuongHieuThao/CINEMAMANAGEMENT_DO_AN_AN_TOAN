package com.theatermgnt.theatermgnt.room.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.cinema.entity.Cinema;
import com.theatermgnt.theatermgnt.cinema.repository.CinemaRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.room.dto.request.RoomCreationRequest;
import com.theatermgnt.theatermgnt.room.dto.request.RoomUpdateRequest;
import com.theatermgnt.theatermgnt.room.dto.response.RoomResponse;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.room.mapper.RoomMapper;
import com.theatermgnt.theatermgnt.room.repository.RoomRepository;
import com.theatermgnt.theatermgnt.seat.service.SeatService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {
    RoomRepository roomRepository;
    CinemaRepository cinemasRepository;
    RoomMapper roomMapper;
    SeatService seatService;

    @Transactional
    public RoomResponse createRoom(RoomCreationRequest request) {
        Cinema cinema = cinemasRepository
                .findById(request.getCinemaId())
                .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_EXISTED));

        if (roomRepository.existsByNameAndCinemaId(request.getName(), request.getCinemaId()))
            throw new AppException(ErrorCode.ROOM_EXISTED);

        // Create room and save room
        Room room = roomMapper.toRoom(request);
        room.setCinema(cinema);
        Room savedRoom = roomRepository.save(room);

        if (request.getSeats() != null) {
            seatService.syncSeats(room, request.getSeats());
        }

        return roomMapper.toRoomResponse(savedRoom);
    }

    @Transactional
    public RoomResponse updateRoom(String roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_EXISTED));

        // Validation: Check if the room has screening from now to future
        //        boolean isRoomBusy = screeningRepository.existsByRoomIdAndStartTimeAfter(roomId, LocalDateTime.now());

        // Update room
        roomMapper.updateRoom(room, request);

        if (request.getName() != null
                && roomRepository.existsByNameAndCinemaIdAndIdNot(
                        request.getName(), room.getCinema().getId(), roomId)) {
            throw new AppException(ErrorCode.ROOM_EXISTED);
        }
        if (request.getSeats() != null) {
            seatService.syncSeats(room, request.getSeats());
        }

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toRoomResponseWithSeats(savedRoom);
    }

    public List<RoomResponse> getRoomsByCinema(String cinemaId) {
        return roomRepository.findByCinemaId(cinemaId).stream()
                .map(roomMapper::toRoomResponse)
                .toList();
    }

    public List<RoomResponse> getRooms() {
        return roomRepository.findAll().stream().map(roomMapper::toRoomResponse).toList();
    }

    public RoomResponse getRoom(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_EXISTED));
        return roomMapper.toRoomResponseWithSeats(room);
    }

    public void deleteRoom(String roomId) {
        if (!roomRepository.existsById(roomId)) throw new AppException(ErrorCode.ROOM_NOT_EXISTED);
        roomRepository.deleteById(roomId);
    }
}
