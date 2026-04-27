package com.theatermgnt.theatermgnt.room.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.room.dto.request.RoomCreationRequest;
import com.theatermgnt.theatermgnt.room.dto.request.RoomUpdateRequest;
import com.theatermgnt.theatermgnt.room.dto.response.RoomResponse;
import com.theatermgnt.theatermgnt.room.service.RoomService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomController {
    RoomService roomService;

    @PostMapping
    ApiResponse<RoomResponse> createRoom(@RequestBody @Valid RoomCreationRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.createRoom(request))
                .build();
    }

    @GetMapping("/cinema/{cinemaId}")
    ApiResponse<List<RoomResponse>> getRoomsByCinema(@PathVariable String cinemaId) {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomService.getRoomsByCinema(cinemaId))
                .build();
    }

    @GetMapping("/{roomId}")
    ApiResponse<RoomResponse> getRoom(@PathVariable String roomId) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.getRoom(roomId))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoomResponse>> getRooms() {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomService.getRooms())
                .build();
    }

    @PutMapping("/{roomId}")
    ApiResponse<RoomResponse> updateRoom(@PathVariable String roomId, @RequestBody @Valid RoomUpdateRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.updateRoom(roomId, request))
                .build();
    }

    @DeleteMapping("/{roomId}")
    ApiResponse<String> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ApiResponse.<String>builder().result("Delete room successfully").build();
    }
}
