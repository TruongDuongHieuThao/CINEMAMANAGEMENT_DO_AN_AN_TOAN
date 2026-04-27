/**
 * Store Types
 * 
 * This file exports TypeScript types for all stores.
 * Use these types for advanced use cases like custom hooks or middleware.
 */

import { useAuthStore } from "./auth-store";
import { useAuthModalStore } from "./auth-modal-store";

// Extract store types from Zustand stores
export type AuthStoreState = ReturnType<typeof useAuthStore.getState>;
export type AuthModalStoreState = ReturnType<typeof useAuthModalStore.getState>;

// Individual state/action interfaces (if needed for documentation or utilities)
export interface AuthState {
  isAuthenticated: boolean;
  isChecking: boolean;
}

export interface AuthActions {
  checkAuth: () => boolean;
  login: () => void;
  logout: () => void;
  setIsChecking: (checking: boolean) => void;
}

export interface AuthModalState {
  showAuthModal: "login" | "register" | null;
}

export interface AuthModalActions {
  setShowAuthModal: (mode: "login" | "register" | null) => void;
  openLoginModal: () => void;
  openRegisterModal: () => void;
  closeModal: () => void;
}

// Combined types
export type AuthStore = AuthState & AuthActions;
export type AuthModalStore = AuthModalState & AuthModalActions;
