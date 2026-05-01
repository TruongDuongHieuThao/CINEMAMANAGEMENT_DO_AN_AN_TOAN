import axios, {
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosResponse,
} from "axios";
import { CONFIG } from "./configuration";
import { ApiError, ApiResponse } from "@/lib/errors";
import { getToken } from "@/services/localStorageService";
import { requestTokenRefresh } from "@/services/tokenRefresh";
import { useAuthStore } from "@/store";

const httpClient = axios.create({
  baseURL: CONFIG.API,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Always send cookies
});

const PUBLIC_API_PATHS = [
  "/auth",
  "/register",
  "/movies",
  "/genres",
  "/screenings",
  "/cinemas",
  "/reviews",
  "/payment",
];

const isPublicRequest = (url?: string) => {
  if (!url) {
    return false;
  }
  const path = url.startsWith("http") ? new URL(url).pathname : url;
  return PUBLIC_API_PATHS.some((publicPath) => path.startsWith(publicPath));
};

// Request interceptor
httpClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add fallback Authorization header for APIs still expecting Bearer token
    if (typeof window !== "undefined" && !isPublicRequest(config.url)) {
      const token = getToken();
      if (token && config.headers && !config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      // For CSRF protection, add X-XSRF-TOKEN header if needed
      const csrfToken = document.cookie
        .split("; ")
        .find((row) => row.startsWith("XSRF-TOKEN="))
        ?.split("=")[1];
      if (csrfToken && config.headers) {
        config.headers["X-XSRF-TOKEN"] = csrfToken;
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

const handleAuthFailure = () => {
  if (typeof window === "undefined") {
    return;
  }
  // No longer clear localStorage - cookies will be cleared by logout API
  useAuthStore.getState().logout();
};

const isRefreshRequest = (url?: string) => {
  if (!url) return false;
  const path = url.startsWith("http") ? new URL(url).pathname : url;
  return path.startsWith("/auth/refresh");
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
      if (
        (error.response.status === 401 || data.code === 1006) &&
        originalRequest &&
        !originalRequest._retry &&
        !isRefreshRequest(originalRequest.url) &&
        !isPublicRequest(originalRequest.url)
      ) {
        // Disable token refresh for now - using cookies
        // originalRequest._retry = true;
        // const refreshedToken = await requestTokenRefresh();
        // if (refreshedToken && originalRequest.headers) {
        //   originalRequest.headers.Authorization = `Bearer ${refreshedToken}`;
        //   return httpClient(originalRequest);
        // }
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
