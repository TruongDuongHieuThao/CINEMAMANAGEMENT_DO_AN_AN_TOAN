package com.theatermgnt.theatermgnt.screening.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.room.repository.RoomRepository;
import com.theatermgnt.theatermgnt.screening.dto.request.ScreeningCreationRequest;
import com.theatermgnt.theatermgnt.screening.dto.request.ScreeningUpdateRequest;
import com.theatermgnt.theatermgnt.screening.dto.response.ScreeningDetailResponse;
import com.theatermgnt.theatermgnt.screening.dto.response.ScreeningResponse;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.enums.ScreeningStatus;
import com.theatermgnt.theatermgnt.screening.mapper.ScreeningMapper;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;
import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatCreationRequest;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;
import com.theatermgnt.theatermgnt.screeningSeat.service.ScreeningSeatService;
import com.theatermgnt.theatermgnt.seat.entity.Seat;
import com.theatermgnt.theatermgnt.seat.repository.SeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreeningService {
    RoomRepository roomRepository;
    MovieRepository movieRepository;
    ScreeningRepository screeningRepository;
    ScreeningMapper screeningMapper;
    SeatRepository seatRepository;
    ScreeningSeatService screeningSeatService;
    ScreeningSeatRepository screeningSeatRepository;

    private void validateScreeningTime(LocalDateTime start, LocalDateTime end) {
        if (!start.isAfter(LocalDateTime.now())) throw new AppException(ErrorCode.SCREENING_TIME_INVALID);

        if (!end.isAfter(start)) throw new AppException(ErrorCode.SCREENING_TIME_INVALID);
    }

    private void validateOverlap(String roomId, LocalDateTime start, LocalDateTime end, String currentId) {
        boolean overlap = screeningRepository.isTimeOverlap(roomId, start, end, currentId);
        if (overlap) throw new AppException(ErrorCode.SCREENING_TIME_OVERLAP);
    }

    @Transactional
    public ScreeningResponse createScreening(ScreeningCreationRequest request) {

        Room room = roomRepository
                .findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_EXISTED));

        Movie movie = movieRepository
                .findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));
        //        // code smell
        //
        //        if (!request.getStartTime().isAfter(LocalDateTime.now()))
        //            throw new AppException(ErrorCode.SCREENING_TIME_INVALID);
        //
        //        if (!request.getEndTime().isAfter(request.getStartTime()))
        //            throw new AppException(ErrorCode.SCREENING_TIME_INVALID);
        //
        //        boolean overlap = screeningRepository.isTimeOverlap(
        //                request.getRoomId(), request.getStartTime(), request.getEndTime(), null);
        //
        //        if (overlap) throw new AppException(ErrorCode.SCREENING_TIME_OVERLAP);

        validateScreeningTime(request.getStartTime(), request.getEndTime());
        validateOverlap(request.getRoomId(), request.getStartTime(), request.getEndTime(), null);

        Screening screening = screeningMapper.toScreening(request);
        screening.setRoom(room);
        screening.setMovie(movie);
        screening.setStatus(ScreeningStatus.SCHEDULED);
        Screening saved = screeningRepository.save(screening);

        createScreeningSeatsForRoom(saved, room);

        return screeningMapper.toScreeningResponse(saved);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void createScreeningSeatsForRoom(Screening screening, Room room) {

        List<Seat> seats = seatRepository.findByRoomId(room.getId());
        if (seats.isEmpty()) return;

        for (Seat seat : seats) {
            ScreeningSeatCreationRequest dto = ScreeningSeatCreationRequest.builder()
                    .screeningId(screening.getId())
                    .seatId(seat.getId())
                    .build();

            screeningSeatService.createScreeningSeat(dto);
        }
    }

    public List<ScreeningResponse> getScreeningsByRoomId(String roomId) {
        return screeningRepository.findByRoomId(roomId).stream()
                .map(screeningMapper::toScreeningResponse)
                .toList();
    }

    public List<ScreeningResponse> getScreeningsByMovieId(String movieId) {
        return screeningRepository.findByMovieId(movieId).stream()
                .map(screeningMapper::toScreeningResponse)
                .toList();
    }

    public List<ScreeningResponse> getScreenings() {
        return screeningRepository.findAll().stream()
                .map(screeningMapper::toScreeningResponse)
                .toList();
    }

    public ScreeningResponse getScreening(String screeningId) {
        Screening screening = screeningRepository
                .findById(screeningId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));
        return screeningMapper.toScreeningResponse(screening);
    }

    public ScreeningDetailResponse getScreeningDetail(String screeningId) {
        Screening screening = screeningRepository
                .findById(screeningId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));

        // Get total seats from room
        Integer totalSeats = screening.getRoom().getTotalSeats();

        // Count booked seats (seats with status SOLD)
        Integer bookedSeats = screeningSeatRepository.countBookedSeats(screeningId);

        // Calculate available seats
        Integer availableSeats = totalSeats - bookedSeats;

        // Use mapper to convert
        return screeningMapper.toScreeningDetailResponse(screening, totalSeats, bookedSeats, availableSeats);
    }

    public ScreeningResponse updateScreening(String screeningId, ScreeningUpdateRequest request) {

        Screening screening = screeningRepository
                .findById(screeningId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));

        if (screening.getStatus() != ScreeningStatus.SCHEDULED)
            throw new AppException(ErrorCode.SCREENING_CANNOT_UPDATE);

        // code smell

        if (!request.getStartTime().isAfter(LocalDateTime.now()))
            throw new AppException(ErrorCode.SCREENING_TIME_INVALID);

        if (!request.getEndTime().isAfter(request.getStartTime()))
            throw new AppException(ErrorCode.SCREENING_TIME_INVALID);

        boolean overlap = screeningRepository.isTimeOverlap(
                screening.getRoom().getId(), request.getStartTime(), request.getEndTime(), screeningId);

        if (overlap) throw new AppException(ErrorCode.SCREENING_TIME_OVERLAP);
        // code smell

        //        validateScreeningTime(request.getStartTime(), request.getEndTime());
        //        validateOverlap(request.getRoomId(), request.getStartTime(), request.getEndTime(), null);

        screeningMapper.updateScreening(screening, request);

        return screeningMapper.toScreeningResponse(screeningRepository.save(screening));
    }

    @Transactional
    public void deleteScreening(String screeningId) {

        Screening screening = screeningRepository
                .findById(screeningId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));

        if (screeningSeatRepository.existsSoldSeat(screeningId)) {
            throw new AppException(ErrorCode.SCREENING_SEAT_CANNOT_DELETE);
        }

        screeningSeatRepository.softDeleteByScreeningId(screeningId);

        screeningRepository.delete(screening);
    }
}
