/**
 * Ticket Service - Using httpClient with JWT Session Cookies
 * 
 * Migration: Manual Bearer Token Header → httpOnly Cookie Session
 * - No longer manually reading tokens from localStorage
 * - Use httpClient which automatically sends cookies via withCredentials
 * - Backend reads JWT from cookie (not Authorization header)
 */

import httpClient from "@/configurations/httpClient"
import { CONFIG } from "@/configurations/configuration"

const API_BASE_URL = CONFIG.API

export interface TicketResponse {
  id: string
  ticketCode: string
  movieTitle: string
  startTime: string
  qrContent: string
  seatName: string
  price: number
  status: string
  expiresAt: string
  purchaseDate?: string
  createdAt?: string
}

/**
 * Get tickets by booking ID
 * Cookie automatically sent via httpClient.withCredentials
 */
export const getTicketsByBooking = async (bookingId: string): Promise<TicketResponse[]> => {
  try {
    const response = await httpClient.get(`/tickets/by-booking/${bookingId}`)

    // Handle the ApiResponse wrapper
    if (response.data?.result) {
      return response.data.result
    }
    
    return response.data
  } catch (error) {
    console.error("Failed to fetch tickets:", error)
    throw error
  }
}

/**
 * Get tickets by customer ID
 * Cookie automatically sent via httpClient.withCredentials
 */
export const getTicketsByCustomer = async (customerId: string): Promise<TicketResponse[]> => {
  try {
    const response = await httpClient.get(`/tickets/my-tickets/${customerId}`)

    // Handle the ApiResponse wrapper
    if (response.data?.result) {
      return response.data.result
    }
    
    return response.data
  } catch (error) {
    console.error("Failed to fetch customer tickets:", error)
    throw error
  }
}

/**
 * Mark ticket for transfer
 * Cookie automatically sent via httpClient.withCredentials
 */
export const markTicketForTransfer = async (ticketCode: string, customerId: string): Promise<void> => {
  try {
    await httpClient.post(
      `/tickets/${ticketCode}/mark-for-transfer`,
      null,
      {
        params: { customerId },
      }
    )
  } catch (error) {
    console.error("Failed to mark ticket for transfer:", error)
    throw error
  }
}

/**
 * Cancel ticket transfer
 * Cookie automatically sent via httpClient.withCredentials
 */
export const cancelTicketTransfer = async (ticketCode: string, customerId: string): Promise<void> => {
  try {
    await httpClient.post(
      `/tickets/${ticketCode}/cancel-transfer`,
      null,
      {
        params: { customerId },
      }
    )
  } catch (error) {
    console.error("Failed to cancel ticket transfer:", error)
    throw error
  }
}
