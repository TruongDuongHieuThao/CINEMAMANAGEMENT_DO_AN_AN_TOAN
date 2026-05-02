/**
 * Authentication Service - JWT Session (httpOnly Cookie)
 * 
 * Migration: Bearer Token → httpOnly Cookie Session
 * - Token is now stored in httpOnly cookie by backend
 * - Browser auto-sends cookie with each request (via withCredentials: true)
 * - No localStorage storage needed
 * - Authorization header no longer used for JWT
 * 
 * @see SecurityConfig.java - BearerTokenResolver supports cookie reading
 * @see AuthenticationController.java - setAuthCookies() sets httpOnly cookie
 */

import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";

export interface LoginRequest {
  loginIdentifier: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  firstName: string;
  lastName: string;
  dob: string;
  gender: string;
  address?: string;
}

export interface AuthResponse {
  result: {
    token: string;
    authenticated: boolean;
  };
}

export interface IntrospectResponse {
  result: {
    valid: boolean;
  };
}

/**
 * Login with email and password
 * - Backend returns token in response AND sets httpOnly cookie
 * - Frontend stores auth state (managed by store)
 * - Token lifecycle managed entirely by browser cookies
 */
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await httpClient.post<AuthResponse>(API.LOGIN, data, {
    withCredentials: true, // Include cookies
  });

  // NO localStorage storage anymore - token is in httpOnly cookie
  // Backend will set the cookie automatically via Set-Cookie header

  return response.data;
};

/**
 * Register new customer
 * - Backend returns token in response AND sets httpOnly cookie
 */
export const register = async (
  data: RegisterRequest,
): Promise<AuthResponse> => {
  const response = await httpClient.post<AuthResponse>(API.REGISTER, data);

  // NO localStorage storage - cookie is set by backend

  return response.data;
};

/**
 * Introspect token validity
 */
export const introspectToken = async (token: string): Promise<boolean> => {
  try {
    const response = await httpClient.post<IntrospectResponse>(
      "/auth/introspect",
      { token },
    );
    return !!response.data.result?.valid;
  } catch (error) {
    const status = (error as any)?.response?.status;
    const code = (error as any)?.code ?? (error as any)?.response?.data?.code;
    if (status === 401 || code === 1006) {
      return false;
    }
    console.error("Token introspection failed:", error);
    return false;
  }
};
