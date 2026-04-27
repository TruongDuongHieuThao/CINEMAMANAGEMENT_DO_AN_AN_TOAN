import httpClient from "@/configurations/httpClient";
import type { ApiResponse } from "@/utils/apiResponse";

// Runtime-available status map; keeps values aligned with backend enums
export const TicketStatus = {
  ACTIVE: "ACTIVE",
  USED: "USED",
  EXPIRED: "EXPIRED",
  CANCELLED: "CANCELLED",
} as const;

export type TicketStatus = (typeof TicketStatus)[keyof typeof TicketStatus];

export interface TicketResponse {
  id: string;
  ticketCode: string;
  movieTitle: string;
  startTime: string;
  qrContent: string;
  seatName: string;
  price: number;
  status: TicketStatus;
  expiresAt: string;
  createdAt: string;
  bookingId: string;
}

export interface ComboItemResponse {
  id: string;
  comboName: string;
  name: string;
  quantity: number;
}

export interface ComboResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
}

export interface ComboCheckInResponse {
  bookingComboId: string;
  quantity: number;
  remain: number;
  combo: ComboResponse;
  comboItemResponseList: ComboItemResponse[];
}

export interface TicketCheckInViewResponse {
  ticket: TicketResponse;
  comboCheckIn: ComboCheckInResponse[];
}

export const ticketService = {
  getTicketCheckInView: async (ticketCode: string): Promise<TicketCheckInViewResponse> => {
    const response = await httpClient.get<ApiResponse<TicketCheckInViewResponse>>(
      `/tickets/check-in/${ticketCode}`
    );
    return response.data.result;
  },

  getTicketByCode: async (ticketCode: string): Promise<TicketResponse> => {
    const response = await httpClient.get<ApiResponse<TicketResponse>>(
      `/tickets/${ticketCode}`
    );
    return response.data.result;
  },

  checkInTicket: async (ticketCode: string, comboUseList?: any[]): Promise<string> => {
    const request = {
      ticketCode,
      comboUseList: comboUseList || []
    };
    const response = await httpClient.post<ApiResponse<string>>(
      `/tickets/check-in/${ticketCode}`,
      request
    );
    return response.data.result;
  },

  getTicketsByBooking: async (bookingId: string): Promise<TicketResponse[]> => {
    const response = await httpClient.get<ApiResponse<TicketResponse[]>>(
      `/tickets/by-booking/${bookingId}`
    );
    return response.data.result;
  },

  getTicketsByCustomer: async (customerId: string): Promise<TicketResponse[]> => {
    const response = await httpClient.get<TicketResponse[]>(
      `/tickets/my-tickets/${customerId}`
    );
    return response.data;
  },

  getCombosByBooking: async (bookingId: string): Promise<ComboCheckInResponse[]> => {
    const response = await httpClient.get<ApiResponse<ComboCheckInResponse[]>>(
      `/bookings/${bookingId}/combos`
    );
    return response.data.result;
  },
};
