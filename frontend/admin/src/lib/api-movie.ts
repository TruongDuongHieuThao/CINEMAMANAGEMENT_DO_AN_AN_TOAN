import httpClient from "@/configurations/httpClient"
import type { ApiResponse } from "@/utils/apiResponse"
import type { Seat, Showtime, ComboItem } from "./types"

// Re-export types
export type { Seat, Showtime, ComboItem }

// Map screening to showtime
export function mapScreeningToShowtime(screening: any): Showtime {
  return {
    id: screening.id,
    movieId: screening.movieId,
    roomId: screening.roomId,
    cinemaId: screening.cinemaId,
    time: screening.startTime,
    format: "2D", // Default format
    price: 100000, // Default price
    duration: 120, // Default duration
  }
}

// Map seat from screening seat
export function mapScreeningSeatToSeat(seat: any, index: number): Seat | null {
  if (!seat) return null

  const seatCode = seat.seatNumber || seat.seatCode || seat.seatId || ""
  let row = "A"
  let seatNumber = (index % 12) + 1

  if (seatCode) {
    const normalized = String(seatCode).trim()
    const match = normalized.match(/^([A-Za-z]+)[-\s]?([0-9]+)$/)

    if (match) {
      row = match[1].toUpperCase()
      seatNumber = parseInt(match[2], 10)
    } else {
      const parts = normalized.split("-")
      row = parts[0] ? String(parts[0]).toUpperCase() : row
      const parsed = parseInt(parts[1], 10)
      if (!Number.isNaN(parsed)) {
        seatNumber = parsed
      }
    }
  }

  const seatTypeUpper = (seat.seatType || "").toUpperCase()
  let type: "standard" | "vip" | "couple" = "standard"
  if (seatTypeUpper === "VIP") {
    type = "vip"
  } else if (seatTypeUpper === "COUPLE") {
    type = "couple"
  }

  const isAvailable = seat.status === "AVAILABLE" 
  const price = seat.price ? Number(seat.price) : undefined

  return {
    id: seat.id || seatCode || `${row}-${seatNumber}`,
    row,
    number: seatNumber,
    type,
    isAvailable,
    price,
  }
}

// Get screening seats
export async function getScreeningSeatsByScreeningId(screeningId: string): Promise<any[]> {
  try {
    const response = await httpClient.get<ApiResponse<any[]>>(
      `/screeningSeats/screening/${screeningId}`
    )
    return response.data.result || []
  } catch (error) {
    console.error("Error fetching screening seats:", error)
    throw error
  }
}

// Get screening by ID
export async function getScreeningById(screeningId: string): Promise<any> {
  try {
    const response = await httpClient.get<ApiResponse<any>>(
      `/screenings/${screeningId}`
    )
    return response.data.result
  } catch (error) {
    console.error("Error fetching screening:", error)
    throw error
  }
}

// Get combos
export async function getCombos(): Promise<ComboItem[]> {
  try {
    const response = await httpClient.get<ApiResponse<any[]>>("/combos")
    const combos = response.data.result || []
    return combos.map((combo: any) => ({
      id: combo.id,
      name: combo.name,
      description: combo.description,
      price: combo.price,
    }))
  } catch (error) {
    console.error("Error fetching combos:", error)
    throw error
  }
}

// Get combo items by combo ID
export async function getComboItemsByComboId(comboId: string): Promise<any[]> {
  try {
    const response = await httpClient.get<ApiResponse<any[]>>(
      `/comboItems/combo/${comboId}`
    )
    return response.data.result || []
  } catch (error) {
    console.error("Error fetching combo items:", error)
    // Return empty list on error to avoid blocking booking flow
    return []
  }
}

// Map combo for display
export function mapComboForDisplay(combo: any): ComboItem {
  return {
    id: combo.id,
    name: combo.name,
    description: combo.description || "",
    price: combo.price || 0,
    imageUrl: combo.imageUrl || combo.image || "",
  }
}

// Map combo item detail
export function mapComboItemDetail(item: any): any {
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    price: item.price,
  }
}
