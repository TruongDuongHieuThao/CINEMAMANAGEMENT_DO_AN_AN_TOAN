package com.theatermgnt.theatermgnt.movie.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;
import com.theatermgnt.theatermgnt.movie.entity.Genre;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {
    @Mapping(target = "movieCount", expression = "java(genre.getMovies() != null ? genre.getMovies().size() : 0)")
    GenreResponse toGenreResponse(Genre genre);

    List<GenreResponse> toGenreResponseList(List<Genre> genres);

    @Mapping(target = "movies", ignore = true)
    Genre toGenre(CreateGenreRequest request);
}
