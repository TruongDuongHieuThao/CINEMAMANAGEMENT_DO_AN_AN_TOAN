"use client";

import { useEffect, useState, useRef } from "react";
import { useReviewManager } from "@/hooks/useReviewManager";
import { useAuthStore } from "@/store/auth-store";
import { getUserInfo } from "@/services/localStorageService";
import { getMyInfo } from "@/services/customerService";
import {
  RatingStars,
  ReviewForm,
  ReviewList,
  MovieRatingStatsDisplay,
} from "@/components/reviews";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { MessageSquarePlus } from "lucide-react";

interface ReviewsSectionProps {
  movieId: string;
  customerId?: string;
  movieStatus?: string;
}

export function ReviewsSection({
  movieId,
  customerId: customerIdProp,
  movieStatus,
}: ReviewsSectionProps) {
  const { isAuthenticated } = useAuthStore();
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [customerId, setCustomerId] = useState<string | undefined>(
    customerIdProp,
  );
  const [loadingCustomerId, setLoadingCustomerId] = useState(false);
  const hasFetchedRef = useRef(false);

  // Sync customerId with customerIdProp when it changes
  useEffect(() => {
    if (customerIdProp && customerIdProp !== customerId) {
      setCustomerId(customerIdProp);
      hasFetchedRef.current = true;
    }
  }, [customerIdProp]);

  // Fetch customer ID from API when user is authenticated
  useEffect(() => {
    if (isAuthenticated && !customerId && !hasFetchedRef.current) {
      hasFetchedRef.current = true;

      const fetchCustomerId = async () => {
        setLoadingCustomerId(true);
        try {
          // Method 1: Try to get from localStorage first
          const userInfo = getUserInfo();
          let userId = userInfo?.id || userInfo?.customerId || userInfo?.userId;

          // Method 2: If not found, fetch from API
          if (!userId) {
            const customerInfo = await getMyInfo();
            userId = customerInfo.customerId;
          }

          if (userId) {
            setCustomerId(userId);
          } else {
            console.warn("Could not get customer ID");
          }
        } catch (error) {
          console.error("Failed to fetch customer ID:", error);
        } finally {
          setLoadingCustomerId(false);
        }
      };

      fetchCustomerId();
    }
  }, [isAuthenticated, customerId]);

  const {
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
    markAsHelpful,
    markAsUnhelpful,
  } = useReviewManager(movieId);

  // Load data on mount
  useEffect(() => {
    loadReviews();
    loadStats();
  }, [loadReviews, loadStats]);

  // Load user votes when reviews change and customerId is available
  useEffect(() => {
    if (customerId && reviews.length > 0) {
      const reviewIds = reviews.map((r) => r.id);
      loadUserVotes(customerId, reviewIds);
    }
  }, [customerId, reviews, loadUserVotes]);

  const handleSubmitReview = async (request: any) => {
    await createReview(request);
    setShowReviewForm(false);
  };

  // Check if movie is currently showing (allow reviews)
  // Backend returns 'now_showing' (lowercase with underscore)
  const isMovieShowing = movieStatus === "now_showing";
  const canWriteReview = isAuthenticated && customerId && isMovieShowing;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Reviews & Ratings</h2>
        {canWriteReview && !showReviewForm ? (
          <Button onClick={() => setShowReviewForm(true)} className="gap-2">
            <MessageSquarePlus className="h-4 w-4" />
            Write a Review
          </Button>
        ) : !isAuthenticated ? (
          <p className="text-sm text-gray-500">
            Please login to write a review
          </p>
        ) : !isMovieShowing ? (
          <p className="text-sm text-gray-500">
            Reviews are only available for movies currently showing
          </p>
        ) : loadingCustomerId ? (
          <p className="text-sm text-gray-500">Loading...</p>
        ) : null}
      </div>

      {/* Error display */}
      {error && (
        <div className="p-4 text-sm text-red-700 bg-red-50 border border-red-200 rounded-md">
          {error}
        </div>
      )}

      {/* Review Form */}
      {showReviewForm && canWriteReview && (
        <Card>
          <CardHeader>
            <CardTitle>Write Your Review</CardTitle>
          </CardHeader>
          <CardContent>
            <ReviewForm
              movieId={movieId}
              customerId={customerId}
              onSubmit={handleSubmitReview}
              onCancel={() => setShowReviewForm(false)}
              submitting={submitting}
            />
          </CardContent>
        </Card>
      )}

      {/* Stats & Reviews Tabs */}
      <Tabs defaultValue="reviews" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="reviews">
            All Reviews ({stats?.totalReviews || 0})
          </TabsTrigger>
          <TabsTrigger value="stats">Rating Statistics</TabsTrigger>
        </TabsList>

        <TabsContent value="reviews" className="mt-6">
          <ReviewList
            reviews={reviews}
            onHelpful={markAsHelpful}
            onUnhelpful={markAsUnhelpful}
            loading={loading}
            customerId={customerId}
            userVotes={userVotes}
          />
        </TabsContent>

        <TabsContent value="stats" className="mt-6">
          <Card>
            <CardContent className="pt-6">
              <MovieRatingStatsDisplay stats={stats} loading={loading} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
