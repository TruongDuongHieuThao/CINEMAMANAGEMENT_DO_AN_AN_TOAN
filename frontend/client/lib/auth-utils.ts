import { getToken } from "@/services/localStorageService";

// Public routes that don't require authentication
export const publicRoutes = [
  "/",
  "/movies",
  "/authenticate",
];

// Protected routes that require authentication
export const protectedRoutes = [
  "/booking",
  "/profile",
  "/my-tickets",
  "/payment",
];

// Check if a route is protected
export const isProtectedRoute = (pathname: string): boolean => {
  return protectedRoutes.some((route) => pathname.startsWith(route));
};

// Check if a route is public
export const isPublicRoute = (pathname: string): boolean => {
  return publicRoutes.some((route) => pathname === route || pathname.startsWith(route));
};

// Check if user is authenticated
export const isAuthenticated = (): boolean => {
  const token = getToken();
  return !!token;
};

// Get redirect path after login
export const getRedirectPath = (): string => {
  if (typeof window !== "undefined") {
    return sessionStorage.getItem("redirectAfterLogin") || "/";
  }
  return "/";
};

// Set redirect path before login
export const setRedirectPath = (path: string): void => {
  if (typeof window !== "undefined") {
    sessionStorage.setItem("redirectAfterLogin", path);
  }
};

// Clear redirect path
export const clearRedirectPath = (): void => {
  if (typeof window !== "undefined") {
    sessionStorage.removeItem("redirectAfterLogin");
  }
};
