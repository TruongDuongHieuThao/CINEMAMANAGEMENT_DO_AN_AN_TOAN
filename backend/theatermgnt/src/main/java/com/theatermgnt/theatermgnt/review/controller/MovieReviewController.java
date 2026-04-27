package com.theatermgnt.theatermgnt.review.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.review.dto.request.CreateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.request.UpdateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.response.MovieRatingStatsResponse;
import com.theatermgnt.theatermgnt.review.dto.response.ReviewResponse;
import com.theatermgnt.theatermgnt.review.entity.VoteType;
import com.theatermgnt.theatermgnt.review.service.MovieReviewService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieReviewController {

    MovieReviewService reviewService;

    // ========== CREATE ==========

    @PostMapping
    ApiResponse<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ApiResponse.<ReviewResponse>builder().result(response).build();
    }

    // ========== READ ==========

    @GetMapping("/{reviewId}")
    ApiResponse<ReviewResponse> getReviewById(@PathVariable("reviewId") String reviewId) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.getReviewById(reviewId))
                .build();
    }

    @GetMapping("/movie/{movieId}")
    ApiResponse<List<ReviewResponse>> getReviewsByMovieId(@PathVariable("movieId") String movieId) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getAllReviewsByMovieId(movieId))
                .build();
    }

    @GetMapping("/movie/{movieId}/paginated")
    ApiResponse<Page<ReviewResponse>> getReviewsByMovieIdPaginated(
            @PathVariable("movieId") String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getReviewsByMovieIdPaginated(movieId, page, size))
                .build();
    }

    @GetMapping("/movie/{movieId}/most-helpful")
    ApiResponse<Page<ReviewResponse>> getMostHelpfulReviews(
            @PathVariable("movieId") String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getMostHelpfulReviews(movieId, page, size))
                .build();
    }

    @GetMapping("/movie/{movieId}/recent")
    ApiResponse<Page<ReviewResponse>> getRecentReviews(
            @PathVariable("movieId") String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(reviewService.getRecentReviews(movieId, page, size))
                .build();
    }

    @GetMapping("/customer/{customerId}")
    ApiResponse<List<ReviewResponse>> getReviewsByCustomerId(@PathVariable("customerId") String customerId) {
        return ApiResponse.<List<ReviewResponse>>builder()
                .result(reviewService.getReviewsByCustomerId(customerId))
                .build();
    }

    @GetMapping("/movie/{movieId}/stats")
    ApiResponse<MovieRatingStatsResponse> getMovieRatingStats(@PathVariable("movieId") String movieId) {
        return ApiResponse.<MovieRatingStatsResponse>builder()
                .result(reviewService.getMovieRatingStats(movieId))
                .build();
    }

    // ========== UPDATE ==========

    @PutMapping("/{reviewId}")
    ApiResponse<ReviewResponse> updateReview(
            @PathVariable("reviewId") String reviewId, @Valid @RequestBody UpdateReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(reviewId, request))
                .build();
    }

    @PatchMapping("/{reviewId}/helpful")
    ApiResponse<ReviewResponse> markReviewAsHelpful(
            @PathVariable("reviewId") String reviewId,
            @RequestParam(value = "customerId", required = true) String customerId) {
        log.info("Received helpful request: reviewId={}, customerId={}", reviewId, customerId);
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.markReviewAsHelpful(reviewId, customerId))
                .build();
    }

    @PatchMapping("/{reviewId}/unhelpful")
    ApiResponse<ReviewResponse> markReviewAsUnhelpful(
            @PathVariable("reviewId") String reviewId,
            @RequestParam(value = "customerId", required = true) String customerId) {
        log.info("Received unhelpful request: reviewId={}, customerId={}", reviewId, customerId);
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.markReviewAsUnhelpful(reviewId, customerId))
                .build();
    }

    @GetMapping("/votes")
    ApiResponse<Map<String, String>> getUserVotes(
            @RequestParam("customerId") String customerId, @RequestParam("reviewIds") List<String> reviewIds) {
        Map<String, VoteType> votes = reviewService.getUserVotesForReviews(customerId, reviewIds);
        // Convert VoteType enum to String for JSON response
        Map<String, String> voteStrings = votes.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue().name()));
        return ApiResponse.<Map<String, String>>builder().result(voteStrings).build();
    }

    // ========== DELETE ==========

    @DeleteMapping("/{reviewId}")
    ApiResponse<String> deleteReview(@PathVariable("reviewId") String reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.<String>builder()
                .result("Review deleted successfully")
                .build();
    }
}
