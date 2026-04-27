"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/store";
import { clearAuthData, getToken } from "@/services/localStorageService";
import { introspectToken } from "@/services/authService";
import { requestTokenRefresh } from "@/services/tokenRefresh";

/**
 * StoreInitializer - Initialize stores on app mount
 * This component should be placed high in the component tree (e.g., in layout)
 */
export function StoreInitializer({ children }: { children: React.ReactNode }) {
  const initialized = useRef(false);
  const checkAuth = useAuthStore((state) => state.checkAuth);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    if (initialized.current) {
      return;
    }

    const initAuth = async () => {
      const token = getToken();
      if (token) {
        const isValid = await introspectToken(token);
        if (!isValid) {
          const refreshedToken = await requestTokenRefresh();
          if (!refreshedToken) {
            clearAuthData();
            logout();
          }
        }
      }

      // Initialize auth state on mount
      checkAuth();
      initialized.current = true;
    };

    void initAuth();
  }, [checkAuth, logout]);

  return <>{children}</>;
}
