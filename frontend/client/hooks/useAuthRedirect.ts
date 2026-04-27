"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { isProtectedRoute, isAuthenticated, setRedirectPath } from "@/lib/auth-utils";

export function useAuthRedirect() {
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    // Check if the current route is protected
    if (isProtectedRoute(pathname)) {
      const authenticated = isAuthenticated();
      
      if (!authenticated) {
        // Save the intended destination
        setRedirectPath(pathname);
        
        // Redirect to home (where user can login)
        router.push("/");
      }
    }
  }, [pathname, router]);
}
