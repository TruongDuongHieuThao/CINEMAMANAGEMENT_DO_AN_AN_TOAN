package com.theatermgnt.theatermgnt.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByCinemaId(String cinemaId);

    boolean existsByNameAndCinemaId(String name, String cinemaId);

    boolean existsByNameAndCinemaIdAndIdNot(String name, String cinemaId, String id);
}
