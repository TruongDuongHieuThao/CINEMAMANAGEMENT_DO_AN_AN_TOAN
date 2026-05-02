/**
 * Authentication Service - JWT Session (httpOnly Cookie)
 * 
 * Migration: Bearer Token → httpOnly Cookie Session
 * - Token is now stored in httpOnly cookie by backend
 * - Browser auto-sends cookie with each request (via withCredentials: true)
 * - No localStorage storage needed
 * - Authorization header no longer used for JWT
 * 
 * @see SecurityConfig.java - BearerTokenResolver supports cookie reading
 * @see AuthenticationController.java - setAuthCookies() sets httpOnly cookie
 */

import httpClient from "../configurations/httpClient";
import {
  API
} from "../configurations/configuration";
import { useAuthStore } from "@/stores";
import { extractCinemaIdFromToken, extractPermissionsFromToken, extractUserIdFromToken } from "@/utils/jwtUtils";

/**
 * Login with email/username and password
 * - Backend returns token in response AND sets httpOnly cookie
 * - Frontend stores user metadata in Zustand (userId, permissions, cinemaId)
 * - Token lifecycle managed entirely by browser cookies
 */
export const login = async (loginIdentifier: string, password: string) => {
  try {
    const response = await httpClient.post(API.LOGIN, {
      loginIdentifier: loginIdentifier,
      password: password,
    });

    const token = response.data?.result?.token;

    if (token) {
      // Extract userId and permissions from token (backend will validate signature)
      const userId = extractUserIdFromToken(token);
      const permissions = extractPermissionsFromToken(token);
      const cinemaId = extractCinemaIdFromToken(token);

      if (!userId) {
        throw new Error('Invalid token: missing user ID');
      }

      // Update auth store - NO token stored here anymore
      // Token is in httpOnly cookie, managed by browser
      useAuthStore.getState().setAuth(userId, cinemaId, permissions);
    }

    return response;
  } catch (error) {
    console.error('Login failed:', error);
    throw error;
  }
};

/**
 * Logout and clear session
 * - Calls backend logout endpoint to blacklist token
 * - Backend clears httpOnly cookie
 * - Frontend clears Zustand store
 */
export const logOut = () => {
  try {
    // Clear Zustand store (auth state)
    useAuthStore.getState().clearAuth();
    // Cookie is cleared by logout API response (backend set Set-Cookie with maxAge=0)
  } catch (error) {
    console.error('Logout error:', error);
    throw error;
  }
};

/**
 * Check if user is authenticated
 * - Returns true if user has userId in store (cookie exists on browser)
 * - Does NOT check localStorage
 */
export const isAuthenticated = (): boolean => {
  const isAuth = useAuthStore.getState().isAuthenticated;
  return !!isAuth;
};

export const forgotPassword = async (loginIdentifier: string) => {
  const response = await httpClient.post(API.FORGOT_PASSWORD, {
    loginIdentifier: loginIdentifier,
  });
  return response.data;
};

export const resetPassword = async (
  loginIdentifier: string,
  otpCode: string,
  newPassword: string
) => {
  const response = await httpClient.post(API.RESET_PASSWORD, {
    loginIdentifier: loginIdentifier,
    otpCode: otpCode,
    newPassword: newPassword,
  });
  return response.data;
};