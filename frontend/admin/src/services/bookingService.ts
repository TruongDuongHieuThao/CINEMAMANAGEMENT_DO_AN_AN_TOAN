import httpClient from "@/configurations/httpClient"
import { useAuthStore } from "@/stores/useAuthStore"
import type { ApiResponse } from "@/utils/apiResponse"

export interface CreateBookingRequest {
  screeningId: string
  screeningSeatIds: string[]
  customerName?: string
  email?: string
  firstName?: string
  lastName?: string
  customerId?: string
}

export interface CreateBookingResponse {
  id: string
  expiredAt: string
  subtotal: number
  customerId?: string
}

export interface BookingSummaryResponse {
  bookingId: string
  status: string
  expiredAt?: string
  seats: Array<{
    id: string
    seatName: string
    rowChair: string
    seatNumber: number
    seatTypeId: string
  }>
  combos: Array<{
    comboId: string
    comboName: string
    quantity: number
    unitPrice: number
    subtotal: number
  }>
  startTime: string
  seatSubtotal: number
  comboSubtotal: number
  subTotal: number
  discountAmount?: number
  totalAmount: number
  movie: {
    id: string
    title: string
    posterUrl?: string
    ageRating?: {
      code?: string
    }
  }
}

export interface BookingListItem {
  id: string
  bookingCode: string
  customerId: string | null
  customerName: string
  email: string
  phone: string
  movieTitle: string
  cinemaId: string
  cinemaName: string
  roomName: string
  screeningTime: string
  seatCount: number
  totalAmount: number
  status: 'PENDING' | 'CONFIRM' | 'PAID' | 'EXPIRED' | 'CANCELLED' | 'REFUNDED'
  createdAt: string
  expiredAt: string | null
}

export interface BookingListResponse {
  bookings: BookingListItem[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

export interface UpdateBookingCombosRequest {
  combos: Array<{ comboId: string; quantity: number }>
}

export interface BookingPricingResponse {
  bookingId: string
  subTotal: number
}

export interface RedeemPointsRequest {
  pointsToRedeem: number
}

// Create booking
export const createBooking = async (data: CreateBookingRequest): Promise<CreateBookingResponse> => {
  try {
    const response = await httpClient.post<ApiResponse<CreateBookingResponse>>(
      "/bookings",
      data
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to create booking:", error)
    throw error
  }
}

// Get booking summary
export const getBookingSummary = async (bookingId: string): Promise<BookingSummaryResponse> => {
  try {
    const response = await httpClient.get<ApiResponse<BookingSummaryResponse>>(
      `/bookings/${bookingId}/summary`
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to get booking summary:", error)
    throw error
  }
}

// Update booking combos
export const updateBookingCombos = async (
  bookingId: string,
  data: UpdateBookingCombosRequest
): Promise<BookingPricingResponse> => {
  try {
    const response = await httpClient.put<ApiResponse<BookingPricingResponse>>(
      `/bookings/${bookingId}/combos`,
      data
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to update booking combos:", error)
    throw error
  }
}

// Redeem booking points
export const redeemBookingPoints = async (
  bookingId: string,
  data: RedeemPointsRequest
): Promise<BookingSummaryResponse> => {
  try {
    const response = await httpClient.post<ApiResponse<BookingSummaryResponse>>(
      `/bookings/${bookingId}/redeem-points`,
      data
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to redeem booking points:", error)
    throw error
  }
}

// Cancel booking
export const cancelBooking = async (bookingId: string): Promise<string> => {
  try {
    const response = await httpClient.post<ApiResponse<string>>(
      `/bookings/${bookingId}/cancel`
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to cancel booking:", error)
    throw error
  }
}

// Get list of bookings
export const getBookingList = async (
  status?: string,
  customerSearch?: string,
  emailSearch?: string,
  movieSearch?: string,
  page: number = 0,
  size: number = 10
): Promise<BookingListResponse> => {
  try {
    const cinemaId = useAuthStore.getState().cinemaId;
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    if (customerSearch) params.append('customerSearch', customerSearch)
    if (emailSearch) params.append('emailSearch', emailSearch)
    if (movieSearch) params.append('movieSearch', movieSearch)
    if (cinemaId) params.append('cinemaId', cinemaId)
    params.append('page', page.toString())
    params.append('size', size.toString())

    const response = await httpClient.get<ApiResponse<BookingListResponse>>(
      `/bookings?${params.toString()}`
    )
    return response.data.result
  } catch (error) {
    console.error("Failed to get booking list:", error)
    throw error
  }
}

