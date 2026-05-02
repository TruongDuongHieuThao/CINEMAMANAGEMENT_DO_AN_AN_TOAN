/**
 * @deprecated JWT tokens are now stored in httpOnly cookies by default
 * This service is kept for backward compatibility only.
 * DO NOT use getToken() or setToken() for production - these are NO-OPS.
 * 
 * Migration: JWT Session using httpOnly cookies (secure, auto-sent by browser)
 * - Tokens are automatically sent with each request via cookies
 * - No need for localStorage or Authorization headers
 * - Backend reads from cookies in SecurityConfig.BearerTokenResolver
 * 
 * Timeline: May 2026
 * See MIGRATION_GUIDE.md for details
 */

// Local Storage keys
const TOKEN_KEY = "customer_token";
const USER_INFO_KEY = "customer_info";

/**
 * @deprecated - NO-OP function. Do not use.
 * Token is now managed by browser via httpOnly cookies.
 */
export const setToken = (token: string): void => {
  if (typeof window !== "undefined") {
    console.warn(
      "[DEPRECATED] setToken() is a NO-OP. " +
      "JWT tokens are stored in httpOnly cookies. " +
      "See localStorageService.ts for migration details."
    );
  }
};

/**
 * @deprecated - Returns null. Do not use.
 * Token is now managed by browser via httpOnly cookies.
 */
export const getToken = (): null => {
  if (typeof window !== "undefined") {
    console.warn(
      "[DEPRECATED] getToken() returns null. " +
      "JWT tokens are stored in httpOnly cookies. " +
      "Cookies are auto-sent by browser with withCredentials: true."
    );
  }
  return null;
};

/**
 * @deprecated - NO-OP function. Do not use.
 * Token is now managed by browser via httpOnly cookies.
 */
export const removeToken = (): void => {
  if (typeof window !== "undefined") {
    console.warn(
      "[DEPRECATED] removeToken() is a NO-OP. " +
      "Use API logout endpoint to clear cookies server-side."
    );
  }
};

// User info operations
export const setUserInfo = (userInfo: any): void => {
  if (typeof window !== "undefined") {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
  }
};

export const getUserInfo = (): any | null => {
  if (typeof window !== "undefined") {
    const userInfo = localStorage.getItem(USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  }
  return null;
};

export const removeUserInfo = (): void => {
  if (typeof window !== "undefined") {
    localStorage.removeItem(USER_INFO_KEY);
  }
};

// Clear all auth data
export const clearAuthData = (): void => {
  removeToken();
  removeUserInfo();
};
