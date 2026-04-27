import axios from "axios";
import { CONFIG } from "@/configurations/configuration";
import { getToken, setToken } from "@/services/localStorageService";

let refreshPromise: Promise<string | null> | null = null;

export const requestTokenRefresh = async (): Promise<string | null> => {
  const currentToken = getToken();
  if (!currentToken) {
    return null;
  }

  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${CONFIG.API}/auth/refresh`, { token: currentToken })
      .then((response) => {
        const nextToken = response.data?.result?.token;
        if (nextToken) {
          setToken(nextToken);
          return nextToken;
        }
        return null;
      })
      .catch(() => null)
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
};
