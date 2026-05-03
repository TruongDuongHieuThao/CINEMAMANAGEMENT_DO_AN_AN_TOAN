"use client";

import { useEffect, useRef } from "react";
import { useAuthStore } from "@/store";
import { getMyInfo } from "@/services/customerService";
import { removeUserInfo, setUserInfo } from "@/services/localStorageService";

/**
 * StoreInitializer - Initialize stores on app mount
 * This component should be placed high in the component tree (e.g., in layout)
 */
export function StoreInitializer({ children }: { children: React.ReactNode }) {
  const initialized = useRef(false);
  const login = useAuthStore((state) => state.login);
  const setIsChecking = useAuthStore((state) => state.setIsChecking);
  const setAuthState = useAuthStore.setState;

  useEffect(() => {
    if (initialized.current) {
      return;
    }

    const initAuth = async () => {
      try {
        setIsChecking(true);

        const userInfo = await getMyInfo();
        if (userInfo) {
          setUserInfo(userInfo);
          login();
        } else {
          removeUserInfo();
          setAuthState({ isAuthenticated: false, isChecking: false });
        }
      } catch (error) {
        removeUserInfo();
        setAuthState({ isAuthenticated: false, isChecking: false });
      } finally {
        setIsChecking(false);
        initialized.current = true;
      }
    };

    void initAuth();
  }, [login, setAuthState, setIsChecking]);

  return <>{children}</>;
}
