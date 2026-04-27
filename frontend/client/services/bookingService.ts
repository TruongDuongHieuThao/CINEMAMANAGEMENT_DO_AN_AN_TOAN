import {
  createBooking as apiCreateBooking,
  getBookingSummary as apiGetBookingSummary,
  updateBookingCombos as apiUpdateBookingCombos,
  redeemBookingPoints as apiRedeemBookingPoints,
  cancelBooking as apiCancelBooking,
} from "@/lib/api-movie"

export interface CreateBookingRequest {
  customerId: string
  screeningId: string
  screeningSeatIds: string[]
}

export interface CreateBookingResponse {
  id: string
  expiredAt: string
  subtotal: number
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

export const createBooking = async (data: CreateBookingRequest): Promise<CreateBookingResponse> => {
  try {
    const response = await apiCreateBooking(data)
    return response
  } catch (error) {
    console.error("Failed to create booking:", error)
    throw error
  }
}

export const getBookingSummary = async (bookingId: string): Promise<BookingSummaryResponse> => {
  try {
    const response = await apiGetBookingSummary(bookingId)
    return response
  } catch (error) {
    console.error("Failed to get booking summary:", error)
    throw error
  }
}

export const updateBookingCombos = async (
  bookingId: string,
  data: UpdateBookingCombosRequest
): Promise<BookingPricingResponse> => {
  try {
    const response = await apiUpdateBookingCombos(bookingId, data)
    return response
  } catch (error) {
    console.error("Failed to update booking combos:", error)
    throw error
  }
}

export const redeemBookingPoints = async (
  bookingId: string,
  data: RedeemPointsRequest
): Promise<BookingSummaryResponse> => {
  try {
    const response = await apiRedeemBookingPoints(bookingId, data)
    return response
  } catch (error) {
    console.error("Failed to redeem booking points:", error)
    throw error
  }
}

export const cancelBooking = async (bookingId: string): Promise<string> => {
  try {
    const response = await apiCancelBooking(bookingId)
    return response
  } catch (error) {
    const status = (error as any)?.response?.status
    if (status === 400 || status === 404) {
      console.warn("Cancel booking ignored:", (error as any)?.response?.data || error)
      return ""
    }
    console.error("Failed to cancel booking:", error)
    throw error
  }
}
