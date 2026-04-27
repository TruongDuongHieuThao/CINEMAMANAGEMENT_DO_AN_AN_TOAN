import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { getToken } from "@/services/localStorageService";

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
        checkAuth: () => {
          const token = getToken();
          const authenticated = !!token;
          set({ isAuthenticated: authenticated, isChecking: false });
          return authenticated;
        },

        login: () => {
          const token = getToken();
          set({ isAuthenticated: !!token });
        },

        logout: () => {
          set({ isAuthenticated: false });
        },

        setIsChecking: (checking: boolean) => {
          set({ isChecking: checking });
        },
      }),
      {
        name: "auth-storage", // Key in localStorage
        partialize: (state) => ({ 
          // Only persist isAuthenticated, not isChecking
          isAuthenticated: state.isAuthenticated 
        }),
      }
    ),
    {
      name: "AuthStore", // Name in Redux DevTools
    }
  )
);
