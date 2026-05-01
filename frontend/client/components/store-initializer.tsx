"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/store";
import httpClient from "@/configurations/httpClient";
import { getToken } from "@/services/localStorageService";

/**
 * StoreInitializer - Initialize stores on app mount
 * This component should be placed high in the component tree (e.g., in layout)
 */
export function StoreInitializer({ children }: { children: React.ReactNode }) {
  const initialized = useRef(false);
  const login = useAuthStore((state) => state.login);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    if (initialized.current) {
      return;
    }

    const initAuth = async () => {
      const token = getToken();
      if (!token) {
        logout();
        initialized.current = true;
        return;
      }

      try {
        const response = await httpClient.post("/auth/introspect", { token });
        if (response.data?.result?.valid) {
          login();
        } else {
          logout();
        }
      } catch (error) {
        logout();
      }

      initialized.current = true;
    };

    void initAuth();
  }, [login, logout]);

  return <>{children}</>;
}
