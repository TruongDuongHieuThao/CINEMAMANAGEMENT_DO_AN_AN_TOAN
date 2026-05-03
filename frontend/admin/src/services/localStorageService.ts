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

export const KEY_TOKEN = "accessToken";

/**
 * @deprecated - NO-OP function. Do not use.
 * Token is now managed by browser via httpOnly cookies.
 */
export const setToken = (_token: string): void => {
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