package com.theatermgnt.theatermgnt.movie.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.movie.entity.AgeRating;

public interface AgeRatingRepository extends JpaRepository<AgeRating, String> {
    Optional<AgeRating> findByCode(String code);
}
