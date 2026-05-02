/**
 * @deprecated Token Refresh Service - NO LONGER NEEDED
 * 
 * JWT tokens are now stored in httpOnly cookies
 * - Backend handles cookie refresh automatically
 * - Browser sends cookies with each request (withCredentials: true)
 * - No manual token refresh needed from frontend
 * 
 * Migration: Manual token refresh → Server-side cookie management
 * @see AuthenticationController.java - Automatic cookie refresh on each request
 */

import axios from "axios";
import { CONFIG } from "@/configurations/configuration";

/**
 * @deprecated - This function is no longer used
 * Backend automatically manages cookie refresh via Set-Cookie headers
 * 
 * Kept for backward compatibility only
 */
export const requestTokenRefresh = async (): Promise<string | null> => {
  console.warn(
    "[DEPRECATED] requestTokenRefresh() is no longer needed. " +
    "JWT tokens are stored in httpOnly cookies managed by backend. " +
    "See tokenRefresh.ts for migration details."
  );
  return null;
};
