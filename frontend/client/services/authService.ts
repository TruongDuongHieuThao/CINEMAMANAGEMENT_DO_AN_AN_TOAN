import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";
import { setToken, getToken } from "./localStorageService";

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

// Login with email and password
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  try {
    const response = await httpClient.post<AuthResponse>(API.LOGIN, data);
    
    if (response.data.result?.token) {
      setToken(response.data.result.token);
    }
    return response.data;
  } catch (error) {
    console.error("Login failed:", error);
    throw error;
  }
};

// Register new customer
export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  try {
    const response = await httpClient.post<AuthResponse>(API.REGISTER, data);
    console.log("Registration Response:", response);
    
    if (response.data.result?.token) {
      setToken(response.data.result.token);
    }
    
    return response.data;
  } catch (error) {
    console.error("Registration failed:", error);
    throw error;
  }
};

// Introspect token validity
export const introspectToken = async (token: string): Promise<boolean> => {
  try {
    const response = await httpClient.post<IntrospectResponse>("/auth/introspect", { token });
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
