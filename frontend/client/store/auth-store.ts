import { create } from "zustand";
import { devtools, persist } from "zustand/middleware";
import { getToken } from "@/services/localStorageService";
import httpClient from "@/configurations/httpClient";

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
          // No longer check localStorage - using cookies
          set({ isAuthenticated: true });
        },

        logout: async () => {
          try {
            const token = getToken();
            if (token) {
              await httpClient.post("/auth/logout", { token });
            }
          } catch (error) {
            // Ignore logout API failures on client-side state reset
          }
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
