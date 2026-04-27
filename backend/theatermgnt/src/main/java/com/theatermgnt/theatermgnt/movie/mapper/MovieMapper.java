package com.theatermgnt.theatermgnt.movie.mapper;

import org.mapstruct.*;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.request.UpdateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.entity.Genre;
import com.theatermgnt.theatermgnt.movie.entity.Movie;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface MovieMapper {

    @Mapping(target = "ageRating", ignore = true)
    @Mapping(target = "genres", ignore = true)
    Movie toMovie(CreateMovieRequest request);

    @Mapping(target = "ageRating", ignore = true)
    @Mapping(target = "genres", ignore = true)
    void updateMovieFromRequest(UpdateMovieRequest request, @MappingTarget Movie movie);

    MovieResponse toMovieResponse(Movie movie);

    @Mapping(source = "ageRating.code", target = "ageRatingCode")
    MovieSimpleResponse toMovieSimpleResponse(Movie movie);

    MovieResponse.AgeRatingInfo toAgeRatingInfo(AgeRating ageRating);

    MovieResponse.GenreInfo toGenreInfo(Genre genre);
}
