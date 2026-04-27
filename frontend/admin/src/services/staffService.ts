import httpClient from "@/configurations/httpClient";
import type { StaffProfile } from "@/types/StaffType/StaffProfile";
import { handleApiResponse } from "@/utils/apiResponse";
import type { ApiResponse } from "@/utils/apiResponse";

const BASE_URL = "/staffs";

export interface StaffRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  jobTitle: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  dob: string;
  username: string;
  password?: string;
  cinemaId?: string;
  roles?: string[];
}

// export const getMyInfo = async () => {
//   return await httpClient.get("/my-info");
// };
export const getMyInfo = async () => {
  return handleApiResponse<StaffProfile>(
    httpClient.get<ApiResponse<StaffProfile>>(`${BASE_URL}/myInfo`))
}
export const updateMyInfo = async (
  staffId: string,
  data: {
    firstName?: string;
    lastName?: string;
    jobTitle?: string;
    cinemaId?: string;
    address?: string;
    gender?: string;
    dob?: string;
    role?: Array<string>;
    avatarUrl?: string;
  }
) => {
  return handleApiResponse<StaffProfile>(
    httpClient.put<ApiResponse<StaffProfile>>(`/staffs/${staffId}`, data)
  );
};

// Staff Management APIs

export const getAllStaffs = async (): Promise<StaffProfile[]> => {
  return handleApiResponse<StaffProfile[]>(
    httpClient.get<ApiResponse<StaffProfile[]>>(BASE_URL)
  );
};

export const getStaffById = async (staffId: string): Promise<StaffProfile> => {
  return handleApiResponse<StaffProfile>(
    httpClient.get<ApiResponse<StaffProfile>>(`${BASE_URL}/${staffId}`)
  );
};

export const createStaff = async (
  request: StaffRequest
): Promise<StaffProfile> => {
  return handleApiResponse<StaffProfile>(
    httpClient.post<ApiResponse<StaffProfile>>(BASE_URL, request)
  );
};

export const updateStaff = async (
  staffId: string,
  request: Partial<StaffRequest>
): Promise<StaffProfile> => {
  return handleApiResponse<StaffProfile>(
    httpClient.put<ApiResponse<StaffProfile>>(`${BASE_URL}/${staffId}`, request)
  );
};

export const deleteStaff = async (staffId: string): Promise<void> => {
  await httpClient.delete(`${BASE_URL}/${staffId}`);
};

export const searchStaffs = async (keyword: string): Promise<StaffProfile[]> => {
  return handleApiResponse<StaffProfile[]>(
    httpClient.get<ApiResponse<StaffProfile[]>>(`${BASE_URL}/search`, {
      params: { keyword },
    })
  );
};

export const getStaffsByCinemaWithRoleStaff = async (cinemaId: string) => {
  return handleApiResponse<StaffProfile[]>(
    httpClient.get<ApiResponse<StaffProfile[]>>(
      `/staffs/cinema/${cinemaId}/staff-role`
    )
  );
};

export const getAvailableManagers = async (): Promise<StaffProfile[]> => {
  return handleApiResponse<StaffProfile[]>(
    httpClient.get<ApiResponse<StaffProfile[]>>(`${BASE_URL}/available-managers`)
  );
};