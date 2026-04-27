import httpClient from "../configurations/httpClient";
import { API } from "../configurations/configuration";
import { getToken } from "./localStorageService";

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

// Get customer info
export const getMyInfo = async (): Promise<CustomerInfo> => {
  try {
    const response = await httpClient.get<{ result: CustomerInfo }>(API.MY_INFO, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
    
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
    const response = await httpClient.put<{ result: CustomerInfo }>(url, data, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
    
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
    const response = await httpClient.get<{ result: { loyaltyPoints: number } }>(url, {
      headers: {
        Authorization: `Bearer ${getToken()}`,
      },
    });
    
    return response.data.result.loyaltyPoints;
  } catch (error) {
    console.error("Failed to get customer loyalty points:", error);
    throw error;
  }
};

