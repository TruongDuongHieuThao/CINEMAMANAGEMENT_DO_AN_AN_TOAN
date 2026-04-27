/**
 * Standard API Response from Backend
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  result?: T;
}

/**
 * Custom API Error class
 * Contains error code and message from backend
 */
export class ApiError extends Error {
  code: number;
  originalError?: any;

  constructor(code: number, message: string, originalError?: any) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.originalError = originalError;

    // Maintains proper stack trace for where our error was thrown (only available on V8)
    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, ApiError);
    }
  }

  /**
   * Check if error is successful (code = 1000)
   */
  isSuccess(): boolean {
    return this.code === 1000;
  }

  /**
   * Get user-friendly error message
   */
  getUserMessage(): string {
    return this.message || "An unexpected error occurred";
  }
}

/**
 * Parse backend response and throw ApiError if code !== 1000
 */
export function handleApiResponse<T>(response: ApiResponse<T>): T {
  if (response.code !== 1000) {
    throw new ApiError(
      response.code,
      response.message || "Request failed",
      response
    );
  }

  return response.result as T;
}

/**
 * Check if error is ApiError instance
 */
export function isApiError(error: any): error is ApiError {
  return error instanceof ApiError;
}

/**
 * Extract error message from any error type
 */
export function getErrorMessage(error: any): string {
  if (isApiError(error)) {
    return error.getUserMessage();
  }

  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  if (error?.message) {
    return error.message;
  }

  return "An unexpected error occurred";
}

/**
 * Extract error code from any error type
 */
export function getErrorCode(error: any): number | null {
  if (isApiError(error)) {
    return error.code;
  }

  if (error?.response?.data?.code) {
    return error.response.data.code;
  }

  return null;
}
