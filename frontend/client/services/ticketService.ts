import axios from "axios"
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

export const getTicketsByBooking = async (bookingId: string): Promise<TicketResponse[]> => {
  try {
    const token = typeof window !== "undefined" ? localStorage.getItem("customer_token") : null
    
    const response = await axios.get(`${API_BASE_URL}/tickets/by-booking/${bookingId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

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

export const getTicketsByCustomer = async (customerId: string): Promise<TicketResponse[]> => {
  try {
    const token = typeof window !== "undefined" ? localStorage.getItem("customer_token") : null
    
    const response = await axios.get(`${API_BASE_URL}/tickets/my-tickets/${customerId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

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

export const markTicketForTransfer = async (ticketCode: string, customerId: string): Promise<void> => {
  try {
    const token = typeof window !== "undefined" ? localStorage.getItem("customer_token") : null
    
    await axios.post(
      `${API_BASE_URL}/tickets/${ticketCode}/mark-for-transfer`,
      null,
      {
        params: { customerId },
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
  } catch (error) {
    console.error("Failed to mark ticket for transfer:", error)
    throw error
  }
}

export const cancelTicketTransfer = async (ticketCode: string, customerId: string): Promise<void> => {
  try {
    const token = typeof window !== "undefined" ? localStorage.getItem("customer_token") : null
    
    await axios.post(
      `${API_BASE_URL}/tickets/${ticketCode}/cancel-transfer`,
      null,
      {
        params: { customerId },
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
  } catch (error) {
    console.error("Failed to cancel ticket transfer:", error)
    throw error
  }
}
