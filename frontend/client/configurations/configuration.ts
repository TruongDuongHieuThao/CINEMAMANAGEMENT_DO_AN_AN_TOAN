export const LOCAL_API_URL = "http://localhost:8080/api/theater-mgnt";

const getEnvValue = (key: string): string | undefined => {
  const processEnv = (globalThis as typeof globalThis & {
    process?: { env?: Record<string, string | undefined> };
  }).process?.env;

  return processEnv?.[key];
};

export const CONFIG = {
  API: getEnvValue("NEXT_PUBLIC_API_URL") || LOCAL_API_URL,
};

export const API = {
  // Auth endpoints
  LOGIN: "/auth/customer/login",
  REGISTER: "/register",
  MY_INFO: "/customers/myInfo",
  
  // Customer endpoints
  UPDATE_CUSTOMER: "/customers/${customerId}",
  CUSTOMER_LOYALTY_POINTS: "/customers/${customerId}/loyalty-points",
};

