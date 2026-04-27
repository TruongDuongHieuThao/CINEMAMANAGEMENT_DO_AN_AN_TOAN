import httpClient from "@/configurations/httpClient";
import type { ApiResponse } from "@/lib/errors";
import type {
  MovieReview,
  CreateReviewRequest,
  UpdateReviewRequest,
  MovieRatingStats,
  PaginatedReviews,
} from "@/types/review";

const BASE_URL = "/reviews";

// ============ CREATE ============
export const createReview = async (
  request: CreateReviewRequest
): Promise<MovieReview> => {
  const response = await httpClient.post<ApiResponse<MovieReview>>(
    BASE_URL,
    request
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

// ============ READ ============
export const getReviewById = async (reviewId: string): Promise<MovieReview> => {
  const response = await httpClient.get<ApiResponse<MovieReview>>(
    `${BASE_URL}/${reviewId}`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getReviewsByMovieId = async (
  movieId: string
): Promise<MovieReview[]> => {
  const response = await httpClient.get<ApiResponse<MovieReview[]>>(
    `${BASE_URL}/movie/${movieId}`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getReviewsByMovieIdPaginated = async (
  movieId: string,
  page: number = 0,
  size: number = 10
): Promise<PaginatedReviews> => {
  const response = await httpClient.get<ApiResponse<PaginatedReviews>>(
    `${BASE_URL}/movie/${movieId}/paginated`,
    {
      params: { page, size },
    }
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getMostHelpfulReviews = async (
  movieId: string,
  page: number = 0,
  size: number = 5
): Promise<PaginatedReviews> => {
  const response = await httpClient.get<ApiResponse<PaginatedReviews>>(
    `${BASE_URL}/movie/${movieId}/most-helpful`,
    {
      params: { page, size },
    }
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getRecentReviews = async (
  movieId: string,
  page: number = 0,
  size: number = 5
): Promise<PaginatedReviews> => {
  const response = await httpClient.get<ApiResponse<PaginatedReviews>>(
    `${BASE_URL}/movie/${movieId}/recent`,
    {
      params: { page, size },
    }
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getReviewsByCustomerId = async (
  customerId: string
): Promise<MovieReview[]> => {
  const response = await httpClient.get<ApiResponse<MovieReview[]>>(
    `${BASE_URL}/customer/${customerId}`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getMovieRatingStats = async (
  movieId: string
): Promise<MovieRatingStats> => {
  const response = await httpClient.get<ApiResponse<MovieRatingStats>>(
    `${BASE_URL}/movie/${movieId}/stats`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

// ============ UPDATE ============
export const updateReview = async (
  reviewId: string,
  request: UpdateReviewRequest
): Promise<MovieReview> => {
  const response = await httpClient.put<ApiResponse<MovieReview>>(
    `${BASE_URL}/${reviewId}`,
    request
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const markReviewAsHelpful = async (
  reviewId: string,
  customerId: string
): Promise<MovieReview> => {
  const response = await httpClient.patch<ApiResponse<MovieReview>>(
    `${BASE_URL}/${reviewId}/helpful?customerId=${customerId}`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const markReviewAsUnhelpful = async (
  reviewId: string,
  customerId: string
): Promise<MovieReview> => {
  const response = await httpClient.patch<ApiResponse<MovieReview>>(
    `${BASE_URL}/${reviewId}/unhelpful?customerId=${customerId}`
  );
  if (!response.data.result) {
    throw new Error("No result returned from API");
  }
  return response.data.result;
};

export const getUserVotes = async (
  customerId: string,
  reviewIds: string[]
): Promise<Record<string, "HELPFUL" | "UNHELPFUL">> => {
  if (!reviewIds || reviewIds.length === 0) {
    return {};
  }

  // Build query params manually for better control
  const reviewIdsParam = reviewIds.map(id => `reviewIds=${id}`).join('&');
  const response = await httpClient.get<ApiResponse<Record<string, string>>>(
    `${BASE_URL}/votes?customerId=${customerId}&${reviewIdsParam}`
  );
  return response.data.result as Record<string, "HELPFUL" | "UNHELPFUL">;
};

// ============ DELETE ============
export const deleteReview = async (reviewId: string): Promise<void> => {
  await httpClient.delete(`${BASE_URL}/${reviewId}`);
};
