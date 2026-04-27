import httpClient from "@/configurations/httpClient";
import type { Cinema, CreateCinemaRequest, UpdateCinemaRequest } from "@/types/CinemaType/cinemaType";
import { handleApiResponse, type ApiResponse } from "@/utils/apiResponse";

const BASE_URL = "/cinemas";

// Re-export types for convenience
export type { Cinema, CreateCinemaRequest, UpdateCinemaRequest };

// Get all cinemas
export const getAllCinemas = async (): Promise<Cinema[]> => {
  return handleApiResponse<Cinema[]>(
    httpClient.get<ApiResponse<Cinema[]>>(BASE_URL)
  );
};

// Get cinema by ID
export const getCinemaById = async (cinemaId: string): Promise<Cinema> => {
  return handleApiResponse<Cinema>(
    httpClient.get<ApiResponse<Cinema>>(`${BASE_URL}/${cinemaId}`)
  );
};

// Create a new cinema
export const createCinema = async (data: CreateCinemaRequest): Promise<Cinema> => {
  return handleApiResponse<Cinema>(
    httpClient.post<ApiResponse<Cinema>>(BASE_URL, data)
  );
};

// Update cinema by ID
export const updateCinema = async (
  cinemaId: string,
  data: UpdateCinemaRequest
): Promise<Cinema> => {
  return handleApiResponse<Cinema>(
    httpClient.put<ApiResponse<Cinema>>(`${BASE_URL}/${cinemaId}`, data)
  );
};

// Delete cinema by ID
export const deleteCinema = async (cinemaId: string): Promise<void> => {
  await httpClient.delete(`${BASE_URL}/${cinemaId}`);
};

// Get cinemas for buffer management (role-based)
export const getCinemasForBufferManagement = async (): Promise<Cinema[]> => {
  return handleApiResponse<Cinema[]>(
    httpClient.get<ApiResponse<Cinema[]>>(`${BASE_URL}/buffer-management`)
  );
};

// Update cinema buffer
export const updateCinemaBuffer = async (
  cinemaId: string,
  buffer: number | null
): Promise<Cinema> => {
  return handleApiResponse<Cinema>(
    httpClient.patch<ApiResponse<Cinema>>(
      `${BASE_URL}/${cinemaId}/buffer`,
      null,
      { params: { buffer } }
    )
  );
};