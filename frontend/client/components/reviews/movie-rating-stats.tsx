"use client";

import { RatingStars } from "./rating-stars";
import { Progress } from "@/components/ui/progress";
import type { MovieRatingStats } from "@/types/review";

interface MovieRatingStatsProps {
  stats: MovieRatingStats | null;
  loading?: boolean;
}

export function MovieRatingStatsDisplay({
  stats,
  loading = false,
}: MovieRatingStatsProps) {
  if (loading) {
    return (
      <div className="animate-pulse space-y-4">
        <div className="flex items-center gap-4">
          <div className="h-16 w-16 bg-gray-200 rounded-full" />
          <div className="space-y-2 flex-1">
            <div className="h-6 bg-gray-200 rounded w-1/3" />
            <div className="h-4 bg-gray-200 rounded w-1/4" />
          </div>
        </div>
        <div className="space-y-2">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="h-6 bg-gray-200 rounded" />
          ))}
        </div>
      </div>
    );
  }

  if (!stats || stats.totalReviews === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <p className="text-lg font-medium">No ratings yet</p>
        <p className="text-sm">Be the first to rate this movie!</p>
      </div>
    );
  }

  // Convert rating distribution to sorted array
  const distributionArray = Object.entries(stats.ratingDistribution)
    .map(([rating, count]) => ({
      rating: Number(rating),
      count: Number(count),
    }))
    .sort((a, b) => b.rating - a.rating);

  // Group ratings into ranges for better visualization
  const ratingRanges = [
    { label: "9-10", min: 9, max: 10 },
    { label: "7-8", min: 7, max: 8.9 },
    { label: "5-6", min: 5, max: 6.9 },
    { label: "3-4", min: 3, max: 4.9 },
    { label: "1-2", min: 1, max: 2.9 },
  ];

  const groupedDistribution = ratingRanges.map((range) => {
    const count = distributionArray
      .filter((item) => item.rating >= range.min && item.rating <= range.max)
      .reduce((sum, item) => sum + item.count, 0);
    return {
      ...range,
      count,
      percentage:
        stats.totalReviews > 0 ? (count / stats.totalReviews) * 100 : 0,
    };
  });

  return (
    <div className="space-y-6">
      {/* Overall Rating */}
      <div className="flex items-center gap-6">
        <div className="text-center">
          <div className="text-5xl font-bold text-gray-900">
            {stats.averageRating.toFixed(1)}
          </div>
          <div className="text-sm text-gray-500 mt-1">out of 10</div>
        </div>

        <div className="flex-1">
          <RatingStars
            rating={stats.averageRating}
            maxRating={10}
            size="lg"
            className="mb-2"
          />
          <p className="text-sm text-gray-600">
            Based on {stats.totalReviews.toLocaleString()} review
            {stats.totalReviews !== 1 ? "s" : ""}
          </p>
        </div>
      </div>

      {/* Rating Distribution */}
      <div className="space-y-3">
        <h4 className="text-sm font-medium text-gray-700">
          Rating Distribution
        </h4>
        {groupedDistribution.map((range) => (
          <div key={range.label} className="flex items-center gap-3">
            <span className="text-sm font-medium text-gray-600 w-12">
              {range.label}
            </span>
            <Progress value={range.percentage} className="flex-1" />
            <span className="text-sm text-gray-500 w-16 text-right">
              {range.count} ({range.percentage.toFixed(0)}%)
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
