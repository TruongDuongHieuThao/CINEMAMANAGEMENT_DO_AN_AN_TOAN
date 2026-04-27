package com.theatermgnt.theatermgnt.review.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.review.entity.MovieReview;

@Repository
public interface MovieReviewRepository extends JpaRepository<MovieReview, String> {

    // Find all reviews for a specific movie
    List<MovieReview> findByMovieId(String movieId);

    // Find all reviews for a specific movie with pagination
    Page<MovieReview> findByMovieId(String movieId, Pageable pageable);

    // Find all reviews by a specific customer
    List<MovieReview> findByCustomerId(String customerId);

    // Find review by customer and movie
    Optional<MovieReview> findByCustomerIdAndMovieId(String customerId, String movieId);

    // Check if customer already reviewed a movie
    boolean existsByCustomerIdAndMovieId(String customerId, String movieId);

    // Find reviews for a specific screening
    List<MovieReview> findByScreeningId(String screeningId);

    // Get average rating for a movie
    @Query("SELECT AVG(r.rating) FROM MovieReview r WHERE r.movie.id = :movieId")
    BigDecimal getAverageRatingByMovieId(@Param("movieId") String movieId);

    // Get total review count for a movie
    @Query("SELECT COUNT(r) FROM MovieReview r WHERE r.movie.id = :movieId")
    Long getReviewCountByMovieId(@Param("movieId") String movieId);

    // Get reviews by rating range
    @Query(
            "SELECT r FROM MovieReview r WHERE r.movie.id = :movieId AND r.rating >= :minRating AND r.rating <= :maxRating")
    List<MovieReview> findByMovieIdAndRatingBetween(
            @Param("movieId") String movieId,
            @Param("minRating") BigDecimal minRating,
            @Param("maxRating") BigDecimal maxRating);

    // Get most helpful reviews for a movie
    @Query("SELECT r FROM MovieReview r WHERE r.movie.id = :movieId ORDER BY r.helpfulCount DESC")
    Page<MovieReview> findMostHelpfulReviewsByMovieId(@Param("movieId") String movieId, Pageable pageable);

    // Get recent reviews for a movie
    @Query("SELECT r FROM MovieReview r WHERE r.movie.id = :movieId ORDER BY r.createdAt DESC")
    Page<MovieReview> findRecentReviewsByMovieId(@Param("movieId") String movieId, Pageable pageable);

    // Get rating distribution for a movie
    @Query(
            """
		SELECT r.rating as rating, COUNT(r) as count
		FROM MovieReview r
		WHERE r.movie.id = :movieId
		GROUP BY r.rating
		ORDER BY r.rating DESC
	""")
    List<Object[]> getRatingDistributionByMovieId(@Param("movieId") String movieId);
}
