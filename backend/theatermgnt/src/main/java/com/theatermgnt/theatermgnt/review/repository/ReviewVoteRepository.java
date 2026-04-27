package com.theatermgnt.theatermgnt.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.review.entity.ReviewVote;
import com.theatermgnt.theatermgnt.review.entity.VoteType;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, String> {

    /**
     * Find a vote by customer ID and review ID
     * Using native query to avoid entity join issues
     * IMPORTANT: Check all records regardless of deleted status to handle toggle properly
     */
    @Query(
            value =
                    "SELECT * FROM review_votes WHERE customer_id = :customerId AND review_id = :reviewId ORDER BY created_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<ReviewVote> findByCustomerIdAndReviewId(
            @Param("customerId") String customerId, @Param("reviewId") String reviewId);

    /**
     * Find all votes by a customer for a list of review IDs
     * Using native query to avoid entity join issues
     */
    @Query(
            value =
                    "SELECT * FROM review_votes WHERE customer_id = :customerId AND review_id IN :reviewIds AND deleted = false",
            nativeQuery = true)
    List<ReviewVote> findByCustomerIdAndReviewIdIn(
            @Param("customerId") String customerId, @Param("reviewIds") List<String> reviewIds);

    /**
     * Count votes by review ID and vote type
     */
    @Query("SELECT COUNT(rv) FROM ReviewVote rv WHERE rv.review.id = :reviewId AND rv.voteType = :voteType")
    Long countByReviewIdAndVoteType(@Param("reviewId") String reviewId, @Param("voteType") VoteType voteType);

    /**
     * Delete vote by customer ID and review ID
     */
    @Query("DELETE FROM ReviewVote rv WHERE rv.customer.id = :customerId AND rv.review.id = :reviewId")
    void deleteByCustomerIdAndReviewId(@Param("customerId") String customerId, @Param("reviewId") String reviewId);
}
