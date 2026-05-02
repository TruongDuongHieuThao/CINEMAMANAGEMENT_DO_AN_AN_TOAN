/**
 * Auth Store - Zustand + LocalStorage (User Metadata Only)
 * 
 * IMPORTANT: Does NOT store JWT token anymore
 * - Token is stored in httpOnly cookie by browser (auto-sent with requests)
 * - Store only keeps user metadata: userId, cinemaId, permissions
 * - Metadata persisted to localStorage for session restoration
 * - Backend validates token signature on each request
 * 
 * Migration: Bearer Token (localStorage) → httpOnly Session Cookie
 * @see SecurityConfig.java - Cookie reading via BearerTokenResolver
 * @see AuthenticationController.java - setAuthCookies() on login
 */

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  // State - NO token field anymore (stored in httpOnly cookie)
  userId: string | null;
  cinemaId: string | null;
  permissions: string[]; // List of permission codes from JWT scope
  isAuthenticated: boolean;
  
  // Actions
  setAuth: (userId: string, cinemaId: string | null, permissions: string[]) => void;
  clearAuth: () => void;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      userId: null,
      cinemaId: null,
      permissions: [],
      isAuthenticated: false,
      
      // Actions
      setAuth: (userId, cinemaId, permissions) => {
        set({
          userId,
          cinemaId,
          permissions,
          isAuthenticated: true,
        });
      },
      
      clearAuth: () => {
        set({
          userId: null,
          cinemaId: null,
          permissions: [],
          isAuthenticated: false,
        });
      },

      hasPermission: (permission: string) => {
        return get().permissions.includes(permission);
      },

      hasAnyPermission: (permissionsToCheck: string[]) => {
        return permissionsToCheck.some(permission => get().permissions.includes(permission));
      },

      hasAllPermissions: (permissionsToCheck: string[]) => {
        return permissionsToCheck.every(permission => get().permissions.includes(permission));
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      // Persist only user metadata (NOT token)
      partialize: (state) => ({
        userId: state.userId,
        cinemaId: state.cinemaId,
        permissions: state.permissions,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Selectors for optimized re-renders
export const selectIsAuthenticated = (state: AuthState) => state.isAuthenticated;
export const selectToken = (state: AuthState) => state.token;
export const selectUserId = (state: AuthState) => state.userId;
export const selectCinemaId = (state: AuthState) => state.cinemaId;
export const selectPermissions = (state: AuthState) => state.permissions;
