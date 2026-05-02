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

import axios from "axios";
import { API, CONFIG } from "./configuration";
import { useNotificationStore } from "@/stores/useNotificationStore";
import { ROUTES } from "@/routes/routes";
import { useAuthStore } from "@/stores/useAuthStore";

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

/**
 * Request Interceptor
 * - Cookies automatically sent by browser (withCredentials: true)
 * - No Authorization header needed
 * - Backend reads token from cookie in SecurityConfig.BearerTokenResolver
 */
httpClient.interceptors.request.use(
  (config) => {
    // Cookies handled automatically by browser
    // No Authorization header needed anymore
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors globally
httpClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const { response, config } = error;
    
    // Handle 401 Unauthorized
    if (response?.status === 401) {
      // Check if this is a login request - if so, let the login component handle it
      const isLoginRequest = config?.url === API.LOGIN || config?.url?.endsWith(API.LOGIN);
      
      if (!isLoginRequest) {
        // This is an authenticated request that failed - session expired
        useAuthStore.getState().clearAuth();
        useNotificationStore.getState().addNotification({
          type: 'error',
          title: 'Authentication Error',
          message: 'Your session has expired. Please login again.',
          duration: 5000
        });
        
        // Redirect to login
        window.location.href = ROUTES.LOGIN;
      }
      // For login requests, just pass the error to the component
      return Promise.reject(error);
    }
    // Handle 403 Forbidden
    if (response?.status === 403) {
      useNotificationStore.getState().addNotification({
        type: 'error',
        title: 'Access Denied',
        message: 'You do not have permission to perform this action.',
        duration: 5000
      });
    }
    
    // Handle 500 Internal Server Error
    if (response?.status === 500) {
      useNotificationStore.getState().addNotification({
        type: 'error',
        title: 'Server Error',
        message: 'An unexpected error occurred. Please try again later.',
        duration: 5000
      });
    }
    
    return Promise.reject(error);
  }
);

export default httpClient;