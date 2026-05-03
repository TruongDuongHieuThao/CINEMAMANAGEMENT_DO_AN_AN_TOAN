/**
 * Auth Store - JWT Session (httpOnly Cookie)
 * 
 * IMPORTANT: Does NOT store JWT token anymore
 * - Token is stored in httpOnly cookie by browser (auto-sent with requests)
 * - Store only manages authentication state (isAuthenticated boolean)
 * - Backend validates token signature on each request via cookie
 * 
 * Migration: Bearer Token (localStorage) → httpOnly Session Cookie
 * @see SecurityConfig.java - Cookie reading via BearerTokenResolver
 * @see AuthenticationController.java - setAuthCookies() on login
 */

import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import httpClient from "@/configurations/httpClient";
import { removeUserInfo } from "@/services/localStorageService";

interface AuthState {
  isAuthenticated: boolean;
  isChecking: boolean;
}

interface AuthActions {
  checkAuth: () => boolean;
  login: () => void;
  logout: () => void;
  setIsChecking: (checking: boolean) => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial state
        isAuthenticated: false,
        isChecking: true,

        // Actions
        /**
         * Check if user is authenticated
         * Since JWT is in httpOnly cookie, we can only check our store state
         * Backend will validate actual token on each request
         */
        checkAuth: () => {
          const authenticated = get().isAuthenticated;
          set({ isChecking: false });
          return authenticated;
        },

        /**
         * Set authenticated state (called after successful login)
         * Backend automatically sets httpOnly cookie with JWT
         */
        login: () => {
          set({ isAuthenticated: true });
        },

        /**
         * Logout and clear session
         * - Calls backend logout endpoint to blacklist token
         * - Backend clears httpOnly cookie
         * - Frontend clears auth state
         */
        logout: async () => {
          try {
            // Call logout endpoint - backend will blacklist token and clear cookie
            await httpClient.post("/auth/logout", {});
          } catch (error) {
            // Ignore logout API failures - proceed with client-side state reset
            console.error("Logout error:", error);
          }
          removeUserInfo();
          set({ isAuthenticated: false });
        },

        setIsChecking: (checking: boolean) => {
          set({ isChecking: checking });
        },
      }),
      {
        name: "AuthStore", // Name in Redux DevTools
      },
    ),
  ),
);
