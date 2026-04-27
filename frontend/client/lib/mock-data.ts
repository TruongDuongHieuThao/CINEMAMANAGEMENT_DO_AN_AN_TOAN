import type { Movie, Facility, Testimonial, FAQItem, MembershipTier, Showtime, ComboItem, Seat } from "./types"

export const nowShowingMovies: Movie[] = [
  {
    id: "1",
    title: "The Quantum Paradox",
    genre: ["Sci-Fi", "Thriller"],
    rating: "PG-13",
    duration: 148,
    releaseDate: "2024-10-15",
    poster: "/sci-fi-movie-poster-quantum.jpg",
    description: "A mind-bending journey through parallel dimensions.",
    director: "Christopher Nolan",
    cast: ["Leonardo DiCaprio", "Marion Cotillard"],
    trailerUrl: "https://www.youtube.com/embed/dQw4w9WgXcQ",
  },
  {
    id: "2",
    title: "Echoes of Tomorrow",
    genre: ["Drama", "Romance"],
    rating: "PG",
    duration: 132,
    releaseDate: "2024-10-20",
    poster: "/romantic-drama-movie-poster.jpg",
    description: "A touching story about love across time.",
    director: "Greta Gerwig",
    cast: ["Timothée Chalamet", "Zendaya"],
  },
  {
    id: "3",
    title: "Shadow Protocol",
    genre: ["Action", "Spy"],
    rating: "R",
    duration: 156,
    releaseDate: "2024-10-18",
    poster: "/action-spy-thriller-movie-poster.jpg",
    description: "High-stakes espionage in a digital age.",
    director: "Denis Villeneuve",
    cast: ["Tom Cruise", "Rebecca Ferguson"],
  },
  {
    id: "4",
    title: "Laughter & Chaos",
    genre: ["Comedy"],
    rating: "PG-13",
    duration: 104,
    releaseDate: "2024-10-22",
    poster: "/comedy-movie-poster-funny.jpg",
    description: "A hilarious adventure that will keep you laughing.",
    director: "Taika Waititi",
    cast: ["Ryan Reynolds", "Awkwafina"],
  },
  {
    id: "5",
    title: "The Last Guardian",
    genre: ["Fantasy", "Adventure"],
    rating: "PG",
    duration: 142,
    releaseDate: "2024-10-25",
    poster: "/fantasy-adventure-movie-poster.png",
    description: "An epic quest to save the realm.",
    director: "Peter Jackson",
    cast: ["Cate Blanchett", "Viggo Mortensen"],
  },
]

export const comingSoonMovies: Movie[] = [
  {
    id: "6",
    title: "Cosmic Collision",
    genre: ["Sci-Fi", "Action"],
    rating: "PG-13",
    duration: 160,
    releaseDate: "2024-11-15",
    poster: "/cosmic-space-sci-fi-movie-poster.jpg",
    description: "When two worlds collide, heroes must rise.",
    director: "James Cameron",
    cast: ["Zoe Saldana", "Sam Worthington"],
  },
  {
    id: "7",
    title: "Midnight Heist",
    genre: ["Crime", "Thriller"],
    rating: "R",
    duration: 138,
    releaseDate: "2024-11-20",
    poster: "/heist-crime-thriller-movie-poster.jpg",
    description: "The greatest heist ever planned.",
    director: "David Fincher",
    cast: ["Ryan Gosling", "Charlize Theron"],
  },
  {
    id: "8",
    title: "Whispers in the Wind",
    genre: ["Drama", "Mystery"],
    rating: "PG",
    duration: 125,
    releaseDate: "2024-11-25",
    poster: "/mystery-drama-movie-poster.jpg",
    description: "Secrets that could change everything.",
    director: "Yorgos Lanthimos",
    cast: ["Emma Stone", "Mark Ruffalo"],
  },
]

export const facilities: Facility[] = [
  {
    id: "1",
    name: "Premium Seating",
    description: "Luxury recliners with maximum comfort and legroom",
    icon: "🪑",
  },
  {
    id: "2",
    name: "4K Projection",
    description: "Crystal clear 4K resolution for immersive viewing",
    icon: "📽️",
  },
  {
    id: "3",
    name: "Dolby Atmos",
    description: "Immersive 3D surround sound experience",
    icon: "🔊",
  },
  {
    id: "4",
    name: "Gourmet Snacks",
    description: "Premium food and beverage selection",
    icon: "🍿",
  },
  {
    id: "5",
    name: "VIP Lounge",
    description: "Exclusive lounge for premium members",
    icon: "✨",
  },
  {
    id: "6",
    name: "Wheelchair Access",
    description: "Fully accessible facilities for all guests",
    icon: "♿",
  },
]

export const testimonials: Testimonial[] = [
  {
    id: "1",
    name: "Sarah Johnson",
    role: "Weekend Regular",
    content: "Cifastar is consistently clean and comfortable. The sound system is top-tier, and the seats are great.",
    rating: 5,
    avatar: "/diverse-woman-avatar.png",
  },
  {
    id: "2",
    name: "Michael Chen",
    role: "Family Visitor",
    content: "Staff are friendly and quick to help. We love the spacious rooms and clear screens at every branch.",
    rating: 5,
    avatar: "/man-avatar.png",
  },
  {
    id: "3",
    name: "Emma Williams",
    role: "Student Moviegoer",
    content: "Easy booking, good snacks, and reliable showtimes. Cifastar has become our go-to cinema chain.",
    rating: 5,
    avatar: "/professional-woman-avatar.png",
  },
]

export const faqs: FAQItem[] = [
  {
    id: "1",
    question: "How do I book tickets online?",
    answer:
      "Choose your movie, pick a showtime, select seats, and complete payment. Your tickets appear in My Tickets and via email confirmation.",
  },
  {
    id: "2",
    question: "Can I cancel or modify my booking?",
    answer:
      "You can cancel a booking up to 2 hours before showtime. Open My Tickets, select the booking, and choose Cancel.",
  },
  {
    id: "3",
    question: "What payment methods do you accept?",
    answer: "Cifastar supports cash payments at the counter.",
  },
  {
    id: "4",
    question: "How do loyalty points work?",
    answer:
      "Earn points after each successful booking and redeem them at checkout for ticket discounts.",
  },
  {
    id: "5",
    question: "Do you offer group discounts?",
    answer: "For group bookings, please contact Cifastar support to receive the best available rates.",
  },
  {
    id: "6",
    question: "What is your refund policy?",
    answer:
      "Refunds apply to cancellations made at least 2 hours before showtime. No refunds for no-shows.",
  },
]

export const membershipTiers: MembershipTier[] = [
  {
    id: "1",
    name: "Silver",
    price: 9.99,
    benefits: ["10% discount on tickets", "5% discount on snacks", "Early booking access", "Monthly newsletter"],
  },
  {
    id: "2",
    name: "Gold",
    price: 19.99,
    benefits: [
      "20% discount on tickets",
      "15% discount on snacks",
      "Priority booking",
      "Free large popcorn monthly",
      "Exclusive events",
    ],
    featured: true,
  },
  {
    id: "3",
    name: "Platinum",
    price: 29.99,
    benefits: [
      "30% discount on tickets",
      "25% discount on snacks",
      "VIP lounge access",
      "Free premium snacks monthly",
      "Exclusive premiere events",
      "Personal concierge",
    ],
  },
]

export const showtimes: Record<string, Showtime[]> = {
  "1": [
    { id: "st1", movieId: "1", time: "10:00 AM", format: "2D", price: 50000, availableSeats: 45 },
    { id: "st2", movieId: "1", time: "1:30 PM", format: "3D", price: 60000, availableSeats: 32 },
    { id: "st3", movieId: "1", time: "5:00 PM", format: "IMAX", price: 75000, availableSeats: 28 },
    { id: "st4", movieId: "1", time: "8:30 PM", format: "2D", price: 50000, availableSeats: 15 },
  ],
  "2": [
    { id: "st5", movieId: "2", time: "11:00 AM", format: "2D", price: 50000, availableSeats: 50 },
    { id: "st6", movieId: "2", time: "2:00 PM", format: "2D", price: 50000, availableSeats: 38 },
    { id: "st7", movieId: "2", time: "6:00 PM", format: "3D", price: 60000, availableSeats: 22 },
    { id: "st8", movieId: "2", time: "9:00 PM", format: "2D", price: 50000, availableSeats: 10 },
  ],
  "3": [
    { id: "st9", movieId: "3", time: "10:30 AM", format: "2D", price: 50000, availableSeats: 48 },
    { id: "st10", movieId: "3", time: "1:00 PM", format: "IMAX", price: 75000, availableSeats: 25 },
    { id: "st11", movieId: "3", time: "4:30 PM", format: "2D", price: 50000, availableSeats: 35 },
    { id: "st12", movieId: "3", time: "7:30 PM", format: "3D", price: 60000, availableSeats: 18 },
  ],
  "4": [
    { id: "st13", movieId: "4", time: "9:00 AM", format: "2D", price: 50000, availableSeats: 52 },
    { id: "st14", movieId: "4", time: "12:00 PM", format: "2D", price: 50000, availableSeats: 40 },
    { id: "st15", movieId: "4", time: "3:30 PM", format: "2D", price: 50000, availableSeats: 30 },
    { id: "st16", movieId: "4", time: "6:30 PM", format: "3D", price: 60000, availableSeats: 20 },
  ],
  "5": [
    { id: "st17", movieId: "5", time: "10:00 AM", format: "2D", price: 50000, availableSeats: 46 },
    { id: "st18", movieId: "5", time: "1:30 PM", format: "IMAX", price: 75000, availableSeats: 24 },
    { id: "st19", movieId: "5", time: "5:00 PM", format: "2D", price: 50000, availableSeats: 33 },
    { id: "st20", movieId: "5", time: "8:00 PM", format: "3D", price: 60000, availableSeats: 16 },
  ],
}

export const combos: ComboItem[] = [
  { id: "c1", name: "Small Popcorn", price: 50000, icon: "🍿" },
  { id: "c2", name: "Large Popcorn", price: 80000, icon: "🍿" },
  { id: "c3", name: "Small Drink", price: 40000, icon: "🥤" },
  { id: "c4", name: "Large Drink", price: 60000, icon: "🥤" },
  { id: "c5", name: "Candy Pack", price: 70000, icon: "🍬" },
  { id: "c6", name: "Combo Deal (Popcorn + Drink)", price: 100000, icon: "🎬" },
]

export const generateSeats = (): Seat[] => {
  const seats: Seat[] = []
  const rows = ["A", "B", "C", "D", "E", "F", "G", "H"]
  const seatsPerRow = 12

  rows.forEach((row) => {
    for (let i = 1; i <= seatsPerRow; i++) {
      seats.push({
        id: `${row}${i}`,
        row,
        number: i,
        isAvailable: Math.random() > 0.2,
        isSelected: false,
        type: "standard",
        price: 0,
      })
    }
  })

  return seats
}
