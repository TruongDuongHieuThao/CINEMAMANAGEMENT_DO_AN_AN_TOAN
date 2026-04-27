package com.theatermgnt.theatermgnt.movie.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.common.util.SlugUtil;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.request.UpdateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.entity.Genre;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.movie.mapper.MovieMapper;
import com.theatermgnt.theatermgnt.movie.repository.AgeRatingRepository;
import com.theatermgnt.theatermgnt.movie.repository.GenreRepository;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;
import com.theatermgnt.theatermgnt.screening.enums.ScreeningStatus;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieRepository movieRepository;
    AgeRatingRepository ageRatingRepository;
    GenreRepository genreRepository;
    MovieMapper movieMapper;
    ScreeningRepository screeningRepository;

    // ========== CREATE ==========
    public MovieResponse createMovie(CreateMovieRequest request) {
        // Validate AgeRating exists
        AgeRating ageRating = ageRatingRepository
                .findById(request.getAgeRatingId())
                .orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));

        // Validate Genres exist
        Set<Genre> genres = request.getGenreIds().stream()
                .map(id ->
                        genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED)))
                .collect(Collectors.toSet());

        // Map and set relationships
        Movie movie = movieMapper.toMovie(request);
        movie.setAgeRating(ageRating);
        movie.setGenres(genres);

        // Generate unique slug
        movie.setSlug(generateUniqueSlug(request.getTitle()));

        // Save and return response
        Movie savedMovie = movieRepository.save(movie);
        log.info("Created movie with id: {}", savedMovie.getId());
        return movieMapper.toMovieResponse(savedMovie);
    }

    // ========== READ ==========
    public List<MovieSimpleResponse> getAllMovies() {
        List<Movie> movies = movieRepository.findAllWithGenres();
        return movies.stream()
                .map(movie -> {
                    MovieSimpleResponse response = movieMapper.toMovieSimpleResponse(movie);
                    response.setNeedsArchiveWarning(shouldShowArchiveWarning(movie));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public MovieResponse getMovieById(String id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));
        return movieMapper.toMovieResponse(movie);
    }

    public MovieResponse getMovieBySlug(String slug) {
        Movie movie = movieRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));
        return movieMapper.toMovieResponse(movie);
    }

    public List<MovieSimpleResponse> getMoviesByStatus(MovieStatus status) {
        return movieRepository.findByStatus(status).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<MovieSimpleResponse> getNowShowingMovies() {
        return movieRepository.findNowShowingMovies(MovieStatus.now_showing).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<MovieSimpleResponse> getComingSoonMovies() {
        return movieRepository.findComingSoonMovies(MovieStatus.coming_soon).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<MovieSimpleResponse> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .collect(Collectors.toList());
    }

    public List<MovieSimpleResponse> getMoviesByGenre(String genreId) {
        // Validate Genre exists
        if (!genreRepository.existsById(genreId)) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED);
        }

        return movieRepository.findByGenreId(genreId).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .collect(Collectors.toList());
    }

    // ========== UPDATE ==========
    public MovieResponse updateMovie(String id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));

        if (request.getStatus() == MovieStatus.archived
                && screeningRepository.existsByMovieIdAndStatus(id, ScreeningStatus.SCHEDULED)) {
            throw new AppException(ErrorCode.MOVIE_HAS_SCHEDULED_SCREENINGS);
        }
        // Update basic fields using MapStruct
        movieMapper.updateMovieFromRequest(request, movie);

        // Update slug if title changed
        if (request.getTitle() != null && !request.getTitle().equals(movie.getTitle())) {
            movie.setSlug(generateUniqueSlug(request.getTitle()));
        }

        // Update AgeRating if provided
        if (request.getAgeRatingId() != null) {
            AgeRating ageRating = ageRatingRepository
                    .findById(request.getAgeRatingId())
                    .orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));
            movie.setAgeRating(ageRating);
        }

        // Update Genres if provided
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            Set<Genre> genres = request.getGenreIds().stream()
                    .map(genreId -> genreRepository
                            .findById(genreId)
                            .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED)))
                    .collect(Collectors.toSet());
            movie.setGenres(genres);
        }

        Movie updatedMovie = movieRepository.save(movie);
        log.info("Updated movie with id: {}", updatedMovie.getId());
        return movieMapper.toMovieResponse(updatedMovie);
    }

    public MovieResponse archiveMovie(String id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));

        if (screeningRepository.existsByMovieIdAndStatus(id, ScreeningStatus.SCHEDULED)) {
            throw new AppException(ErrorCode.MOVIE_HAS_SCHEDULED_SCREENINGS);
        }

        movie.setStatus(MovieStatus.archived);
        Movie archivedMovie = movieRepository.save(movie);
        log.info("Archived movie with id: {}", archivedMovie.getId());
        return movieMapper.toMovieResponse(archivedMovie);
    }

    private boolean shouldShowArchiveWarning(Movie movie) {
        if (movie.getStatus() != MovieStatus.now_showing) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        return !screeningRepository.existsByMovieIdAndStartTimeBetween(movie.getId(), now, sevenDaysLater);
    }

    // ========== DELETE ==========
    public void deleteMovie(String movieId) {
        Movie movie =
                movieRepository.findById(movieId).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));
        movieRepository.delete(movie);
        log.info("Deleted movie with id: {}", movieId);
    }

    // ========== SLUG GENERATION ==========
    private String generateUniqueSlug(String title) {
        String baseSlug = SlugUtil.generateSlug(title);
        String slug = baseSlug;
        int counter = 0;

        // Check if slug exists, if yes, append counter
        while (movieRepository.findBySlug(slug).isPresent()) {
            counter++;
            slug = SlugUtil.generateUniqueSlug(title, counter);
        }

        return slug;
    }
}
