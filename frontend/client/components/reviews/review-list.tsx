"use client";

import { formatDistanceToNow } from "date-fns";
import { RatingStars } from "./rating-stars";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { ThumbsUp, ThumbsDown, AlertTriangle } from "lucide-react";
import type { MovieReview } from "@/types/review";

interface ReviewListProps {
  reviews: MovieReview[];
  onHelpful?: (reviewId: string, customerId: string) => void;
  onUnhelpful?: (reviewId: string, customerId: string) => void;
  loading?: boolean;
  emptyMessage?: string;
  customerId?: string;
  userVotes?: Record<string, "HELPFUL" | "UNHELPFUL">;
}

export function ReviewList({
  reviews,
  onHelpful,
  onUnhelpful,
  loading = false,
  emptyMessage = "No reviews yet. Be the first to review!",
  customerId,
  userVotes = {},
}: ReviewListProps) {
  if (loading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="animate-pulse border rounded-lg p-6 space-y-3"
          >
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 bg-gray-200 rounded-full" />
              <div className="space-y-2 flex-1">
                <div className="h-4 bg-gray-200 rounded w-1/4" />
                <div className="h-3 bg-gray-200 rounded w-1/6" />
              </div>
            </div>
            <div className="h-4 bg-gray-200 rounded w-3/4" />
            <div className="h-4 bg-gray-200 rounded w-full" />
          </div>
        ))}
      </div>
    );
  }

  if (reviews.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p>{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {reviews.map((review) => (
        <ReviewCard
          key={review.id}
          review={review}
          onHelpful={onHelpful}
          onUnhelpful={onUnhelpful}
          customerId={customerId}
          userVote={userVotes[review.id]}
        />
      ))}
    </div>
  );
}

interface ReviewCardProps {
  review: MovieReview;
  onHelpful?: (reviewId: string, customerId: string) => void;
  onUnhelpful?: (reviewId: string, customerId: string) => void;
  customerId?: string;
  userVote?: "HELPFUL" | "UNHELPFUL";
}

function ReviewCard({
  review,
  onHelpful,
  onUnhelpful,
  customerId,
  userVote,
}: ReviewCardProps) {
  // Handle deleted customer case
  const customer = review.customer || {
    id: "deleted",
    firstName: "Deleted",
    lastName: "User",
    avatarUrl: null,
  };

  const customerName = `${customer.firstName ?? ""} ${
    customer.lastName ?? ""
  }`.trim();
  const initials = `${customer.firstName ? customer.firstName[0] : ""}${
    customer.lastName ? customer.lastName[0] : ""
  }`.toUpperCase();
  const reviewDate = formatDistanceToNow(new Date(review.createdAt), {
    addSuffix: true,
  });

  const hasVotedHelpful = userVote === "HELPFUL";
  const hasVotedUnhelpful = userVote === "UNHELPFUL";
  const isOwnReview = customerId === customer.id;
  const canVote = !!customerId && !isOwnReview;

  const handleHelpful = () => {
    if (canVote && customerId) {
      onHelpful?.(review.id, customerId);
    }
  };

  const handleUnhelpful = () => {
    if (canVote && customerId) {
      onUnhelpful?.(review.id, customerId);
    }
  };

  return (
    <div className="border rounded-lg p-6 hover:shadow-md transition-shadow">
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <Avatar>
            <AvatarImage
              src={customer.avatarUrl ?? undefined}
              alt={customerName}
            />
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
          <div>
            <p className="font-medium text-gray-900">{customerName}</p>
            <p className="text-sm text-gray-500">{reviewDate}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <RatingStars
            rating={review.rating}
            maxRating={10}
            size="sm"
            showValue
          />
          {review.isSpoiler && (
            <Badge variant="destructive" className="flex items-center gap-1">
              <AlertTriangle className="h-3 w-3" />
              Spoiler
            </Badge>
          )}
        </div>
      </div>

      {/* Comment */}
      {review.comment && (
        <div className="mb-4">
          <p className="text-gray-700 whitespace-pre-wrap">{review.comment}</p>
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center gap-4 pt-4 border-t">
        <Button
          variant={hasVotedHelpful ? "default" : "ghost"}
          size="sm"
          onClick={handleHelpful}
          disabled={!canVote}
          className={`flex items-center gap-2 ${
            !canVote
              ? "text-gray-400 cursor-not-allowed hover:text-gray-400 hover:bg-transparent"
              : hasVotedHelpful
                ? "bg-green-600 text-white hover:bg-green-700"
                : "text-gray-600 hover:text-green-600"
          }`}
        >
          <ThumbsUp className="h-4 w-4" />
          <span>Helpful ({review.helpfulCount})</span>
        </Button>

        <Button
          variant={hasVotedUnhelpful ? "default" : "ghost"}
          size="sm"
          onClick={handleUnhelpful}
          disabled={!canVote}
          className={`flex items-center gap-2 ${
            !canVote
              ? "text-gray-400 cursor-not-allowed hover:text-gray-400 hover:bg-transparent"
              : hasVotedUnhelpful
                ? "bg-red-600 text-white hover:bg-red-700"
                : "text-gray-600 hover:text-red-600"
          }`}
        >
          <ThumbsDown className="h-4 w-4" />
          <span>Not Helpful ({review.unhelpfulCount})</span>
        </Button>
      </div>
    </div>
  );
}
