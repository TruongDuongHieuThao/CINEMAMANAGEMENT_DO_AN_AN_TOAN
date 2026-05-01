import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";

export interface CustomerInfo {
  customerId: string;
  accountId: string;
  accountType: string;
  username: string;
  email: string;
  phoneNumber?: string;
  firstName: string;
  lastName: string;
  address?: string;
  gender?: string;
  dob?: string;
  loyaltyPoints?: number;
  noPassword?: boolean;
}

export interface UpdateCustomerRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  address?: string;
  dob?: string;
  gender?: string;
}

type DemoMode = "vulnerable" | "defended";

// Get customer info
export const getMyInfo = async (): Promise<CustomerInfo> => {
  try {
    const response = await httpClient.get<{ result: CustomerInfo }>(API.MY_INFO);
    
    return response.data.result;
  } catch (error) {
    console.error("Failed to get customer info:", error);
    throw error;
  }
};

// Update customer info
export const updateMyInfo = async (
  customerId: string,
  data: UpdateCustomerRequest
): Promise<CustomerInfo> => {
  try {
    const url = API.UPDATE_CUSTOMER.replace("${customerId}", customerId);
    const response = await httpClient.put<{ result: CustomerInfo }>(url, data);
    
    return response.data.result;
  } catch (error) {
    console.error("Failed to update customer info:", error);
    throw error;
  }
};

// Get customer loyalty points
export const getCustomerLoyaltyPoints = async (customerId: string): Promise<number> => {
  try {
    const url = API.CUSTOMER_LOYALTY_POINTS.replace("${customerId}", customerId);
    const response = await httpClient.get<{ result: { loyaltyPoints: number } }>(url);
    
    return response.data.result.loyaltyPoints;
  } catch (error) {
    console.error("Failed to get customer loyalty points:", error);
    throw error;
  }
};

export const updateMyEmailForCsrfDemo = async (
  email: string,
  mode: DemoMode
): Promise<CustomerInfo> => {
  const url = API.UPDATE_MY_EMAIL_DEMO;

  // Gửi dạng form-encoded để Burp Suite có thể "Generate CSRF PoC" ra HTML form
  const headers: Record<string, string> = {
    "Content-Type": "application/x-www-form-urlencoded",
  };

  if (mode === "defended") {
    const csrfToken = document.cookie
      .split("; ")
      .find((row) => row.startsWith("XSRF-TOKEN="))
      ?.split("=")[1];
    if (csrfToken) {
      headers["X-XSRF-TOKEN"] = decodeURIComponent(csrfToken);
    }
  }

  const response = await httpClient.post<{ result: CustomerInfo }>(
    url,
    new URLSearchParams({ email }),
    { headers }
  );

  return response.data.result;
};
