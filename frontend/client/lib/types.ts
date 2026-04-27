export interface Movie {
  id: string
  title: string
  genre: string[]
  rating: string
  duration: number
  releaseDate: string
  poster: string
  description: string
  trailerUrl?: string
  director: string
  cast: string[]
}

export interface Showtime {
  id: string
  movieId: string
  time: string
  date?: string
  format: string
  price: number
  availableSeats: number
  roomId?: string
  cinemaId?: string
  startDateTime?: string
  roomName?: string
  cinemaName?: string
  status?: string
}

export interface Seat {
  id: string
  row: string
  number: number
  isAvailable: boolean
  isSelected: boolean
  type: "standard" | "vip" | "couple"
  price: number
  // Transfer information
  isForTransfer?: boolean
  transferTicketId?: string
  sellerName?: string
  sellerEmail?: string
  sellerPhone?: string
}

export interface ComboItemDetail {
  id: string
  comboName?: string
  name: string
  quantity: number
}

export interface ComboItem {
  id: string
  name: string
  price: number
  icon: string
  description?: string
  imageUrl?: string
  items?: ComboItemDetail[]
  quantity?: number
  deleted?: boolean
}

export interface BookingState {
  movieId: string
  showtimeId: string
  selectedSeats: Seat[]
  selectedCombos: ComboItem[]
  totalPrice: number
  discountCode?: string
  discountAmount?: number
}

export interface Facility {
  id: string
  name: string
  description: string
  icon: string
}

export interface Testimonial {
  id: string
  name: string
  role: string
  content: string
  rating: number
  avatar: string
}

export interface FAQItem {
  id: string
  question: string
  answer: string
}

export interface MembershipTier {
  id: string
  name: string
  price: number
  benefits: string[]
  featured?: boolean
}
