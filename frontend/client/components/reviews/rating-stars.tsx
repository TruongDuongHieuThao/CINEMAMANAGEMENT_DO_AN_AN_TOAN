"use client";

import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface RatingStarsProps {
  rating: number;
  maxRating?: number;
  size?: "sm" | "md" | "lg";
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
  showValue?: boolean;
  className?: string;
}

export function RatingStars({
  rating,
  maxRating = 10,
  size = "md",
  interactive = false,
  onRatingChange,
  showValue = false,
  className,
}: RatingStarsProps) {
  const totalStars = 5;
  const normalizedRating = (rating / maxRating) * totalStars;

  const sizeClasses = {
    sm: "h-4 w-4",
    md: "h-5 w-5",
    lg: "h-6 w-6",
  };

  const handleStarClick = (starIndex: number) => {
    if (!interactive || !onRatingChange) return;
    // Convert 5-star system back to maxRating scale
    const newRating = ((starIndex + 1) / totalStars) * maxRating;
    onRatingChange(Number(newRating.toFixed(1)));
  };

  const renderStar = (index: number) => {
    const fillPercentage = Math.min(
      Math.max((normalizedRating - index) * 100, 0),
      100,
    );

    return (
      <button
        key={index}
        type="button"
        onClick={() => handleStarClick(index)}
        disabled={!interactive}
        className={cn(
          "relative inline-block",
          interactive && "cursor-pointer hover:scale-110 transition-transform",
          !interactive && "cursor-default",
        )}
      >
        {/* Background star (empty) */}
        <Star
          className={cn(sizeClasses[size], "text-gray-300")}
          fill="currentColor"
        />

        {/* Foreground star (filled) */}
        <div
          className="absolute inset-0 overflow-hidden"
          style={{ width: `${fillPercentage}%` }}
        >
          <Star
            className={cn(sizeClasses[size], "text-yellow-400")}
            fill="currentColor"
          />
        </div>
      </button>
    );
  };

  return (
    <div className={cn("flex items-center gap-1", className)}>
      <div className="flex items-center">
        {Array.from({ length: totalStars }, (_, i) => renderStar(i))}
      </div>

      {showValue && (
        <span className="ml-2 text-sm font-medium text-gray-700">
          {rating.toFixed(1)}/{maxRating}
        </span>
      )}
    </div>
  );
}
