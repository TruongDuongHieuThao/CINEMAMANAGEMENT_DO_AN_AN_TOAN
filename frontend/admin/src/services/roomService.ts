import httpClient from "@/configurations/httpClient";
import type { CreateRoomRequest, Room, UpdateRoomRequest } from "@/types/RoomType/room";
import {  handleApiResponse } from "@/utils/apiResponse";
import type { ApiResponse } from "@/utils/apiResponse";

// Re-export types for convenience
export type { Room, CreateRoomRequest, UpdateRoomRequest };



// Get all rooms
export const getAllRooms = async () : Promise<Room[]> => {
  return handleApiResponse<Room[]>(
    httpClient.get<ApiResponse<Room[]>>("/rooms")
  )
}

// Create a new room
export const createRoom = async (roomData: CreateRoomRequest): Promise<Room> => {
  return handleApiResponse<Room>(
    httpClient.post<ApiResponse<Room>>("/rooms", roomData)
  );
};

// Get a room by id
export const getRoomById = async (id: string) : Promise<Room> => {
  return handleApiResponse<Room>(
    httpClient.get<ApiResponse<Room[]>>(`/rooms/${id}`)
  )
}

// Update a room by id
export const updateRoom = async (id: string, roomData: UpdateRoomRequest): Promise<Room> => {
  return handleApiResponse<Room>(
    httpClient.put<ApiResponse<Room>>(`/rooms/${id}`, roomData)
  );
}

export const getRoomsByCinema = async (cinemaId: string) => {
  return await httpClient.get(`/rooms/cinema/${cinemaId}`);
};

export const deleteRoom = async (roomId: string) => {
  return await httpClient.delete(`/rooms/${roomId}`);
};