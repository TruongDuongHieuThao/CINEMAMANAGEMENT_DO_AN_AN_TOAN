"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/store";

interface AuthGuardProps {
  children: React.ReactNode;
}

export function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const authState = useAuthStore((state) => state.isAuthenticated);
  const isChecking = useAuthStore((state) => state.isChecking);

  useEffect(() => {
    const checkAuth = () => {
      if (isChecking) {
        setIsAuthenticated(null);
        return;
      }

      if (!authState) {
        setIsAuthenticated(false);
        // Redirect to home page if not authenticated
        router.push("/");
      } else {
        setIsAuthenticated(true);
      }
    };

    checkAuth();
  }, [pathname, router, authState, isChecking]);

  // Show loading state while checking authentication
  if (isAuthenticated === null) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
      </div>
    );
  }

  // Show children only if authenticated
  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}
