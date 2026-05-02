/**
 * HTTP Client - Axios Instance with JWT Session Support
 * 
 * IMPORTANT: JWT tokens are now in httpOnly cookies
 * - Cookies automatically sent with each request (browser behavior)
 * - No Authorization header needed for JWT
 * - Set withCredentials: true to allow cross-site cookie sending
 * 
 * Migration: Bearer Authorization Header → httpOnly Cookie
 * @see SecurityConfig.java - BearerTokenResolver reads from cookie
 * @see AuthenticationController.java - setAuthCookies() for cookie details
 */

import axios, {
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosResponse,
} from "axios";
import { CONFIG } from "./configuration";
import { ApiError, ApiResponse } from "@/lib/errors";
import { useAuthStore } from "@/store";

const httpClient = axios.create({
  baseURL: CONFIG.API,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  // CRITICAL: Allow httpOnly cookies to be sent with cross-site requests
  // This is required for JWT session cookies to work properly
  withCredentials: true,
});

const handleAuthFailure = () => {
  if (typeof window === "undefined") {
    return;
  }
  useAuthStore.getState().logout();
};

// Response interceptor
httpClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // Check if response has standard format
    if (response.data && typeof response.data.code !== "undefined") {
      // If code !== 1000, treat as error
      if (response.data.code !== 1000) {
        const apiError = new ApiError(
          response.data.code,
          response.data.message || "Request failed",
          response.data,
        );
        return Promise.reject(apiError);
      }
    }
    return response;
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    // Handle HTTP errors (4xx, 5xx)
    if (error.response?.data) {
      const data = error.response.data;
      
      // Token expired or invalid - handled by backend via cookie refresh
      if (
        (error.response.status === 401 || data.code === 1006) &&
        originalRequest &&
        !originalRequest._retry
      ) {
        handleAuthFailure();
      }

      // If backend returns standard format even in error response
      if (typeof data.code !== "undefined") {
        const apiError = new ApiError(
          data.code,
          data.message || "Request failed",
          data,
        );
        return Promise.reject(apiError);
      }
    }

    if (error.response?.status === 401) {
      handleAuthFailure();
    }

    // Avoid noisy console errors in UI overlay; callers handle error rendering
    return Promise.reject(error);
  },
);

export default httpClient;
