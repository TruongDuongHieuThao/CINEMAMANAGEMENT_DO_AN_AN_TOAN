// Local Storage keys
const TOKEN_KEY = "customer_token";
const USER_INFO_KEY = "customer_info";

// Token operations
export const setToken = (token: string): void => {
  if (typeof window !== "undefined") {
    localStorage.setItem(TOKEN_KEY, token);
  }
};

export const getToken = (): string | null => {
  if (typeof window !== "undefined") {
    return localStorage.getItem(TOKEN_KEY);
  }
  return null;
};

export const removeToken = (): void => {
  if (typeof window !== "undefined") {
    localStorage.removeItem(TOKEN_KEY);
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
