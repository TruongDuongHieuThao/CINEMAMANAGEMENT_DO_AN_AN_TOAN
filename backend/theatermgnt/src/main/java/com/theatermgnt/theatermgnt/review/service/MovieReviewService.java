package com.theatermgnt.theatermgnt.review.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;
import com.theatermgnt.theatermgnt.review.dto.request.CreateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.request.UpdateReviewRequest;
import com.theatermgnt.theatermgnt.review.dto.response.MovieRatingStatsResponse;
import com.theatermgnt.theatermgnt.review.dto.response.ReviewResponse;
import com.theatermgnt.theatermgnt.review.entity.MovieReview;
import com.theatermgnt.theatermgnt.review.entity.ReviewVote;
import com.theatermgnt.theatermgnt.review.entity.VoteType;
import com.theatermgnt.theatermgnt.review.mapper.MovieReviewMapper;
import com.theatermgnt.theatermgnt.review.repository.MovieReviewRepository;
import com.theatermgnt.theatermgnt.review.repository.ReviewVoteRepository;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieReviewService {

    MovieReviewRepository reviewRepository;
    ReviewVoteRepository reviewVoteRepository;
    CustomerRepository customerRepository;
    MovieRepository movieRepository;
    ScreeningRepository screeningRepository;
    MovieReviewMapper reviewMapper;

    // ========== CREATE ==========
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        log.info("Creating review for movie {} by customer {}", request.getMovieId(), request.getCustomerId());

        try {
            // Validate customer exists
            Customer customer = customerRepository
                    .findById(request.getCustomerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Validate movie exists
            Movie movie = movieRepository
                    .findById(request.getMovieId())
                    .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));

            // Check if movie is currently showing
            if (movie.getStatus() != MovieStatus.now_showing) {
                throw new AppException(ErrorCode.MOVIE_NOT_SHOWING);
            }

            // Check if customer already reviewed this movie
            if (reviewRepository.existsByCustomerIdAndMovieId(request.getCustomerId(), request.getMovieId())) {
                throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
            }

            // Validate screening if provided
            Screening screening = null;
            if (request.getScreeningId() != null && !request.getScreeningId().isEmpty()) {
                screening = screeningRepository
                        .findById(request.getScreeningId())
                        .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));
            }

            // Map and set relationships
            MovieReview review = reviewMapper.toMovieReview(request);
            review.setCustomer(customer);
            review.setMovie(movie);
            review.setScreening(screening);

            // Save and return response
            MovieReview savedReview = reviewRepository.save(review);
            log.info("Successfully created review with id: {}", savedReview.getId());

            return reviewMapper.toReviewResponse(savedReview);
        } catch (AppException e) {
            log.error("AppException while creating review: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating review: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    // ========== READ ==========
    public List<ReviewResponse> getAllReviewsByMovieId(String movieId) {
        // Validate movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }

        List<MovieReview> reviews = reviewRepository.findByMovieId(movieId);
        return reviews.stream().map(reviewMapper::toReviewResponse).collect(Collectors.toList());
    }

    public Page<ReviewResponse> getReviewsByMovieIdPaginated(String movieId, int page, int size) {
        // Validate movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MovieReview> reviews = reviewRepository.findByMovieId(movieId, pageable);
        return reviews.map(reviewMapper::toReviewResponse);
    }

    public Page<ReviewResponse> getMostHelpfulReviews(String movieId, int page, int size) {
        // Validate movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MovieReview> reviews = reviewRepository.findMostHelpfulReviewsByMovieId(movieId, pageable);
        return reviews.map(reviewMapper::toReviewResponse);
    }

    public Page<ReviewResponse> getRecentReviews(String movieId, int page, int size) {
        // Validate movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<MovieReview> reviews = reviewRepository.findRecentReviewsByMovieId(movieId, pageable);
        return reviews.map(reviewMapper::toReviewResponse);
    }

    public List<ReviewResponse> getReviewsByCustomerId(String customerId) {
        // Validate customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<MovieReview> reviews = reviewRepository.findByCustomerId(customerId);
        return reviews.stream().map(reviewMapper::toReviewResponse).collect(Collectors.toList());
    }

    public ReviewResponse getReviewById(String reviewId) {
        MovieReview review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        return reviewMapper.toReviewResponse(review);
    }

    public MovieRatingStatsResponse getMovieRatingStats(String movieId) {
        // Validate movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new AppException(ErrorCode.MOVIE_NOT_EXISTED);
        }

        BigDecimal averageRating = reviewRepository.getAverageRatingByMovieId(movieId);
        Long totalReviews = reviewRepository.getReviewCountByMovieId(movieId);

        // Get rating distribution
        List<Object[]> distribution = reviewRepository.getRatingDistributionByMovieId(movieId);
        Map<BigDecimal, Long> ratingDistribution = new LinkedHashMap<>();
        for (Object[] row : distribution) {
            BigDecimal rating = (BigDecimal) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }

        // Round average rating to 1 decimal place
        if (averageRating != null) {
            averageRating = averageRating.setScale(1, RoundingMode.HALF_UP);
        }

        return MovieRatingStatsResponse.builder()
                .movieId(movieId)
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .ratingDistribution(ratingDistribution)
                .build();
    }

    /**
     * Get user's votes for a list of reviews
     * Returns a map of reviewId -> VoteType
     */
    public Map<String, VoteType> getUserVotesForReviews(String customerId, List<String> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }

        List<ReviewVote> votes = reviewVoteRepository.findByCustomerIdAndReviewIdIn(customerId, reviewIds);
        return votes.stream().collect(Collectors.toMap(vote -> vote.getReview().getId(), ReviewVote::getVoteType));
    }

    // ========== UPDATE ==========
    @Transactional
    public ReviewResponse updateReview(String reviewId, UpdateReviewRequest request) {
        MovieReview review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        // Update fields using MapStruct
        reviewMapper.updateMovieReviewFromRequest(request, review);

        MovieReview updatedReview = reviewRepository.save(review);
        log.info("Successfully updated review with id: {}", reviewId);

        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Transactional
    public ReviewResponse markReviewAsHelpful(String reviewId, String customerId) {
        // Validate review exists
        MovieReview review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        // Validate customer exists
        Customer customer =
                customerRepository.findById(customerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if user is trying to vote on their own review
        if (review.getCustomer().getId().equals(customerId)) {
            throw new AppException(ErrorCode.CANNOT_VOTE_OWN_REVIEW);
        }

        // Check if user already voted on this review
        var existingVote = reviewVoteRepository.findByCustomerIdAndReviewId(customerId, reviewId);

        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();

            // Check if vote was previously soft-deleted
            if (Boolean.TRUE.equals(vote.getDeleted())) {
                vote.setDeleted(false);
                vote.setVoteType(VoteType.HELPFUL);
                reviewVoteRepository.save(vote);
                review.setHelpfulCount(review.getHelpfulCount() + 1);
            }
            // If already voted helpful, remove the vote (toggle off)
            else if (vote.getVoteType() == VoteType.HELPFUL) {
                vote.setDeleted(true);
                reviewVoteRepository.save(vote);
                review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
            } else {
                // If voted unhelpful, switch to helpful
                vote.setVoteType(VoteType.HELPFUL);
                reviewVoteRepository.save(vote);

                // Update counts
                review.setUnhelpfulCount(Math.max(0, review.getUnhelpfulCount() - 1));
                review.setHelpfulCount(review.getHelpfulCount() + 1);
            }
        } else {
            // Create new helpful vote
            ReviewVote newVote = new ReviewVote();
            newVote.setReview(review);
            newVote.setCustomer(customer);
            newVote.setVoteType(VoteType.HELPFUL);
            newVote.setDeleted(false);
            reviewVoteRepository.save(newVote);

            // Update count
            review.setHelpfulCount(review.getHelpfulCount() + 1);
        }

        MovieReview updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewResponse(updatedReview);
    }

    @Transactional
    public ReviewResponse markReviewAsUnhelpful(String reviewId, String customerId) {
        log.info("=== markReviewAsUnhelpful START: reviewId={}, customerId={}", reviewId, customerId);

        // Validate review exists
        MovieReview review =
                reviewRepository.findById(reviewId).orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));
        log.info("✅ Review found: {}", review.getId());

        // Validate customer exists
        Customer customer =
                customerRepository.findById(customerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("✅ Customer found: {}", customer.getId());

        // Check if user is trying to vote on their own review
        if (review.getCustomer().getId().equals(customerId)) {
            throw new AppException(ErrorCode.CANNOT_VOTE_OWN_REVIEW);
        }

        // Check if user already voted on this review
        log.info("🔍 Searching for existing vote: customerId={}, reviewId={}", customerId, reviewId);
        var existingVote = reviewVoteRepository.findByCustomerIdAndReviewId(customerId, reviewId);
        log.info("🔍 Query result: isPresent={}", existingVote.isPresent());

        if (existingVote.isPresent()) {
            ReviewVote vote = existingVote.get();
            log.info(
                    "✅ Found existing vote: id={}, voteType={}, deleted={}",
                    vote.getId(),
                    vote.getVoteType(),
                    vote.getDeleted());

            // Check if vote was previously soft-deleted
            if (Boolean.TRUE.equals(vote.getDeleted())) {
                log.info("🔓 Undeleting and updating vote to UNHELPFUL");
                vote.setDeleted(false);
                vote.setVoteType(VoteType.UNHELPFUL);
                reviewVoteRepository.save(vote);
                review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
            }
            // If already voted unhelpful, remove the vote (toggle off)
            else if (vote.getVoteType() == VoteType.UNHELPFUL) {
                log.info("♻️ Soft-deleting unhelpful vote for review {} by customer {}", reviewId, customerId);
                vote.setDeleted(true);
                reviewVoteRepository.save(vote);
                review.setUnhelpfulCount(Math.max(0, review.getUnhelpfulCount() - 1));
            } else {
                // If voted helpful, switch to unhelpful
                log.info(
                        "🔄 Switching vote from helpful to unhelpful for review {} by customer {}",
                        reviewId,
                        customerId);
                vote.setVoteType(VoteType.UNHELPFUL);
                reviewVoteRepository.save(vote);

                // Update counts
                review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
                review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
            }
        } else {
            log.warn("⚠️ No existing vote found, creating new one");
            // Create new unhelpful vote
            log.info("➕ Creating new unhelpful vote for review {} by customer {}", reviewId, customerId);
            ReviewVote newVote = new ReviewVote();
            newVote.setReview(review);
            newVote.setCustomer(customer);
            newVote.setVoteType(VoteType.UNHELPFUL);
            newVote.setDeleted(false);
            reviewVoteRepository.save(newVote);

            // Update count
            review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        }

        MovieReview updatedReview = reviewRepository.save(review);
        log.info("=== markReviewAsUnhelpful END");
        return reviewMapper.toReviewResponse(updatedReview);
    }

    // ========== DELETE ==========
    @Transactional
    public void deleteReview(String reviewId) {
        try {
            MovieReview review = reviewRepository
                    .findById(reviewId)
                    .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

            // Soft delete via @SQLDelete in BaseEntity
            reviewRepository.delete(review);
            log.info("Successfully deleted review with id: {}", reviewId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while deleting review {}: {}", reviewId, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
