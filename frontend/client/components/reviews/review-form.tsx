"use client";

import { useState } from "react";
import { RatingStars } from "./rating-stars";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { AlertCircle } from "lucide-react";
import type { CreateReviewRequest } from "@/types/review";

interface ReviewFormProps {
  movieId: string;
  customerId: string;
  screeningId?: string;
  onSubmit: (request: CreateReviewRequest) => Promise<void>;
  onCancel?: () => void;
  submitting?: boolean;
}

export function ReviewForm({
  movieId,
  customerId,
  screeningId,
  onSubmit,
  onCancel,
  submitting = false,
}: ReviewFormProps) {
  const [rating, setRating] = useState<number>(5);
  const [comment, setComment] = useState("");
  const [isSpoiler, setIsSpoiler] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (rating < 0.5 || rating > 10) {
      setError("Rating must be between 0.5 and 10");
      return;
    }

    if (comment.length > 5000) {
      setError("Comment must not exceed 5000 characters");
      return;
    }

    try {
      await onSubmit({
        customerId,
        movieId,
        screeningId,
        rating,
        comment: comment.trim() || undefined,
        isSpoiler,
      });

      // Reset form on success
      setRating(5);
      setComment("");
      setIsSpoiler(false);
    } catch (err: any) {
      setError(err.message || "Failed to submit review");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Rating */}
      <div className="space-y-2">
        <Label htmlFor="rating" className="text-base font-medium">
          Your Rating *
        </Label>
        <div className="flex items-center gap-4">
          <RatingStars
            rating={rating}
            maxRating={10}
            size="lg"
            interactive
            onRatingChange={setRating}
            showValue
          />
        </div>
        <p className="text-sm text-gray-500">
          Click on the stars to rate (0.5 - 10.0)
        </p>
      </div>

      {/* Comment */}
      <div className="space-y-2">
        <Label htmlFor="comment" className="text-base font-medium">
          Your Review (Optional)
        </Label>
        <Textarea
          id="comment"
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Share your thoughts about this movie..."
          rows={6}
          maxLength={5000}
          className="resize-none"
        />
        <div className="flex justify-between text-sm text-gray-500">
          <span>{comment.length} / 5000 characters</span>
        </div>
      </div>

      {/* Spoiler warning */}
      <div className="flex items-center space-x-2">
        <Checkbox
          id="spoiler"
          checked={isSpoiler}
          onCheckedChange={(checked) => setIsSpoiler(checked === true)}
        />
        <Label htmlFor="spoiler" className="text-sm font-normal cursor-pointer">
          This review contains spoilers
        </Label>
      </div>

      {/* Error message */}
      {error && (
        <div className="flex items-center gap-2 p-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-md">
          <AlertCircle className="h-4 w-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Actions */}
      <div className="flex gap-3">
        <Button
          type="submit"
          disabled={submitting || rating < 0.5}
          className="flex-1"
        >
          {submitting ? "Submitting..." : "Submit Review"}
        </Button>
        {onCancel && (
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={submitting}
          >
            Cancel
          </Button>
        )}
      </div>
    </form>
  );
}
