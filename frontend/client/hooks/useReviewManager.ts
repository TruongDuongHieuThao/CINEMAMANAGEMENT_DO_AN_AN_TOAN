import { useState, useCallback } from "react";
import { useAuthStore } from "@/store/auth-store";
import * as reviewService from "@/services/reviewService";
import type {
  MovieReview,
  CreateReviewRequest,
  UpdateReviewRequest,
  MovieRatingStats,
} from "@/types/review";

export function useReviewManager(movieId?: string) {
  const [reviews, setReviews] = useState<MovieReview[]>([]);
  const [stats, setStats] = useState<MovieRatingStats | null>(null);
  const [userVotes, setUserVotes] = useState<Record<string, "HELPFUL" | "UNHELPFUL">>({});
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { isAuthenticated } = useAuthStore();

  // Load reviews for a movie
  const loadReviews = useCallback(
    async (targetMovieId?: string) => {
      const id = targetMovieId || movieId;
      if (!id) return;

      try {
        setLoading(true);
        setError(null);
        const data = await reviewService.getReviewsByMovieId(id);
        console.log("Loaded reviews:", data);
        setReviews(data);
      } catch (err: any) {
        console.error("Failed to load reviews:", err);
        setError(err?.message || "Failed to load reviews");
      } finally {
        setLoading(false);
      }
    },
    [movieId]
  );

  // Load rating statistics
  const loadStats = useCallback(
    async (targetMovieId?: string) => {
      const id = targetMovieId || movieId;
      if (!id) return;

      try {
        setLoading(true);
        setError(null);
        const data = await reviewService.getMovieRatingStats(id);
        setStats(data);
      } catch (err: any) {
        console.error("Failed to load stats:", err);
        setError(err?.message || "Failed to load statistics");
      } finally {
        setLoading(false);
      }
    },
    [movieId]
  );

  // Create a new review
  const createReview = useCallback(
    async (request: CreateReviewRequest) => {
      if (!isAuthenticated) {
        setError("Please login to submit a review");
        return null;
      }

      try {
        setSubmitting(true);
        setError(null);
        const newReview = await reviewService.createReview(request);

        // Refresh reviews and stats
        await Promise.all([loadReviews(request.movieId), loadStats(request.movieId)]);

        return newReview;
      } catch (err: any) {
        console.error("Failed to create review:", err);
        const errorMessage =
          err?.response?.data?.message || err?.message || "Failed to submit review";
        setError(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setSubmitting(false);
      }
    },
    [isAuthenticated, loadReviews, loadStats]
  );

  // Update an existing review
  const updateReview = useCallback(
    async (reviewId: string, request: UpdateReviewRequest) => {
      try {
        setSubmitting(true);
        setError(null);
        const updatedReview = await reviewService.updateReview(reviewId, request);

        // Update local state
        setReviews((prev) =>
          prev.map((r) => (r.id === reviewId ? updatedReview : r))
        );

        // Refresh stats
        if (movieId) await loadStats(movieId);

        return updatedReview;
      } catch (err: any) {
        console.error("Failed to update review:", err);
        const errorMessage =
          err?.response?.data?.message || err?.message || "Failed to update review";
        setError(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setSubmitting(false);
      }
    },
    [movieId, loadStats]
  );

  // Delete a review
  const deleteReview = useCallback(
    async (reviewId: string) => {
      try {
        setSubmitting(true);
        setError(null);
        await reviewService.deleteReview(reviewId);

        // Update local state
        setReviews((prev) => prev.filter((r) => r.id !== reviewId));

        // Refresh stats
        if (movieId) await loadStats(movieId);
      } catch (err: any) {
        console.error("Failed to delete review:", err);
        const errorMessage =
          err?.response?.data?.message || err?.message || "Failed to delete review";
        setError(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setSubmitting(false);
      }
    },
    [movieId, loadStats]
  );

  // Load user votes for reviews
  const loadUserVotes = useCallback(
    async (customerId: string, reviewIds: string[]) => {
      if (!customerId || !reviewIds || reviewIds.length === 0) return;

      try {
        const votes = await reviewService.getUserVotes(customerId, reviewIds);
        setUserVotes(votes);
      } catch (err: any) {
        console.error("Failed to load user votes:", err);
      }
    },
    []
  );

  // Mark review as helpful
  const markAsHelpful = useCallback(async (reviewId: string, customerId: string) => {
    if (!customerId) {
      throw new Error("Customer ID is required");
    }

    try {
      const updatedReview = await reviewService.markReviewAsHelpful(reviewId, customerId);

      // Update local state
      setReviews((prev) =>
        prev.map((r) => (r.id === reviewId ? updatedReview : r))
      );

      // Update user vote state - toggle off if already voted helpful
      setUserVotes((prev) => {
        if (prev[reviewId] === "HELPFUL") {
          // Remove vote (toggle off)
          const { [reviewId]: _, ...rest } = prev;
          return rest;
        } else {
          // Add or switch vote
          return {
            ...prev,
            [reviewId]: "HELPFUL",
          };
        }
      });

      return updatedReview;
    } catch (err: any) {
      console.error("Failed to mark as helpful:", err);
      throw err;
    }
  }, []);

  // Mark review as unhelpful
  const markAsUnhelpful = useCallback(async (reviewId: string, customerId: string) => {
    if (!customerId) {
      throw new Error("Customer ID is required");
    }

    try {
      const updatedReview = await reviewService.markReviewAsUnhelpful(reviewId, customerId);

      // Update local state
      setReviews((prev) =>
        prev.map((r) => (r.id === reviewId ? updatedReview : r))
      );

      // Update user vote state - toggle off if already voted unhelpful
      setUserVotes((prev) => {
        if (prev[reviewId] === "UNHELPFUL") {
          // Remove vote (toggle off)
          const { [reviewId]: _, ...rest } = prev;
          return rest;
        } else {
          // Add or switch vote
          return {
            ...prev,
            [reviewId]: "UNHELPFUL",
          };
        }
      });

      return updatedReview;
    } catch (err: any) {
      console.error("Failed to mark as unhelpful:", err);
      throw err;
    }
  }, []);

  return {
    reviews,
    stats,
    userVotes,
    loading,
    submitting,
    error,
    loadReviews,
    loadStats,
    loadUserVotes,
    createReview,
    updateReview,
    deleteReview,
    markAsHelpful,
    markAsUnhelpful,
  };
}
