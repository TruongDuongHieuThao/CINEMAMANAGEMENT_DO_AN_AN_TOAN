package com.theatermgnt.theatermgnt.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.cinema.entity.Cinema;

public interface CinemaRepository extends JpaRepository<Cinema, String> {
    boolean existsByName(String name);
}
