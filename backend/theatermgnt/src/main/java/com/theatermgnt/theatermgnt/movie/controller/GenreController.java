package com.theatermgnt.theatermgnt.movie.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;
import com.theatermgnt.theatermgnt.movie.service.GenreService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {

    GenreService genreService;

    @PostMapping
    ApiResponse<GenreResponse> createGenre(@Valid @RequestBody CreateGenreRequest request) {
        return ApiResponse.<GenreResponse>builder()
                .result(genreService.createGenre(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<GenreResponse>> getAllGenres() {
        return ApiResponse.<List<GenreResponse>>builder()
                .result(genreService.getAllGenres())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<GenreResponse> getGenreById(@PathVariable("id") String id) {
        return ApiResponse.<GenreResponse>builder()
                .result(genreService.getGenreById(id))
                .build();
    }

    @GetMapping("/name/{name}")
    ApiResponse<GenreResponse> getGenreByName(@PathVariable("name") String name) {
        return ApiResponse.<GenreResponse>builder()
                .result(genreService.getGenreByName(name))
                .build();
    }
}
