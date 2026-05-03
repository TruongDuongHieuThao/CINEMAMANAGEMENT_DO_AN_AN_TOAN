"use client";

import { useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { isProtectedRoute, setRedirectPath } from "@/lib/auth-utils";
import { useAuthModalStore } from "@/store";
import { useAuthStore } from "@/store";

interface RouteGuardProps {
  children: React.ReactNode;
}

export function RouteGuard({ children }: RouteGuardProps) {
  const pathname = usePathname();
  const router = useRouter();
  const [authorized, setAuthorized] = useState(true); // Default true to avoid flash
  const [checked, setChecked] = useState(false);

  // Zustand store
  const openLoginModal = useAuthModalStore((state) => state.openLoginModal);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const isChecking = useAuthStore((state) => state.isChecking);

  useEffect(() => {
    // Check authentication on route change
    authCheck(pathname);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pathname, isAuthenticated, isChecking]);

  function authCheck(url: string) {
    if (isChecking) {
      setChecked(false);
      return;
    }

    setChecked(false);

    // Check if route is protected
    if (isProtectedRoute(url)) {
      const authenticated = isAuthenticated;

      if (!authenticated) {
        // Save the intended destination
        setRedirectPath(url);

        // Redirect back to home page
        router.replace("/");

        // Show login modal after a short delay
        setTimeout(() => {
          openLoginModal();
        }, 100);

        setAuthorized(false);
      } else {
        setAuthorized(true);
      }
    } else {
      setAuthorized(true);
    }

    setChecked(true);
  }

  // Don't render until checked
  if (!checked) {
    return <div className="min-h-screen" />;
  }

  return <>{children}</>;
}
