import httpClient from "@/configurations/httpClient";
import type { CustomerProfile } from "@/types/CustomerType/CustomerProfile";
import { handleApiResponse } from "@/utils/apiResponse";
import type { ApiResponse } from "@/utils/apiResponse";

const BASE_URL = "/customers";

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  dob: string;
  username: string;
  password: string;
}

// Get current user info
export const getMyInfo = async (): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.get<ApiResponse<CustomerProfile>>(`${BASE_URL}/myInfo`)
  );
};

// Customer Management APIs

export const getAllCustomers = async (): Promise<CustomerProfile[]> => {
  return handleApiResponse<CustomerProfile[]>(
    httpClient.get<ApiResponse<CustomerProfile[]>>(BASE_URL)
  );
};

export const createCustomer = async (
  request: CustomerRequest
): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.post<ApiResponse<CustomerProfile>>(`${BASE_URL}`, request)
  );
};

export const getCustomerById = async (customerId: string): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.get<ApiResponse<CustomerProfile>>(`${BASE_URL}/${customerId}`)
  );
};

export const updateCustomer = async (
  customerId: string,
  request: Partial<CustomerRequest>
): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.put<ApiResponse<CustomerProfile>>(`${BASE_URL}/${customerId}`, request)
  );
};

export const deleteCustomer = async (customerId: string): Promise<void> => {
  await httpClient.delete(`${BASE_URL}/${customerId}`);
};

// Get customer loyalty points
export const getCustomerLoyaltyPoints = async (customerId: string): Promise<number> => {
  const response = await handleApiResponse<{ loyaltyPoints?: number }>(
    httpClient.get<ApiResponse<{ loyaltyPoints?: number }>>(
      `${BASE_URL}/${customerId}/loyalty-points`
    )
  );
  return response?.loyaltyPoints ?? 0;
};
