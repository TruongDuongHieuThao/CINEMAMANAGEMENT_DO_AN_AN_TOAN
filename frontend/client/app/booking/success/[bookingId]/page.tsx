'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Loader } from 'lucide-react';
import { getBookingSummary } from '@/services/bookingService';
import { getMovieById } from '@/lib/api-movie';
import { getScreeningById } from '@/lib/api-movie';
import type { Movie, Showtime, Seat, ComboItem } from '@/lib/types';
import SuccessStep from '@/components/booking/success-step';

interface BookingSummary {
  bookingId: string;
  status: string;
  seats: Array<{
    id: string;
    seatName: string;
    rowChair: string;
    seatNumber: number;
    seatTypeId: string;
  }>;
  combos: Array<{
    comboId: string;
    comboName: string;
    quantity: number;
    unitPrice: number;
    subtotal: number;
  }>;
  startTime: string;
  seatSubtotal: number;
  comboSubtotal: number;
  subTotal: number;
  totalAmount: number;
  movie: {
    id: string;
    title: string;
    posterUrl?: string;
  };
}

export default function BookingSuccessPage() {
  const params = useParams();
  const router = useRouter();
  const bookingId = params.bookingId as string;

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bookingSummary, setBookingSummary] = useState<BookingSummary | null>(null);
  const [movie, setMovie] = useState<Movie | null>(null);
  const [showtime, setShowtime] = useState<Showtime | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Fetch booking summary
        const summary = await getBookingSummary(bookingId);
        setBookingSummary(summary);

        // Fetch movie data
        if (summary.movie?.id) {
          const movieData = await getMovieById(summary.movie.id);
          setMovie({
            id: movieData.id || summary.movie.id,
            title: movieData.title || summary.movie.title,
            genre: movieData.genre || [],
            rating: movieData.rating || '',
            duration: movieData.duration || 0,
            releaseDate: movieData.releaseDate || '',
            poster: movieData.posterUrl || '',
            description: movieData.description || '',
            director: movieData.director || '',
            cast: movieData.cast || [],
          });
        }

        // Create showtime object from summary with safe pricing
        const seatCount = Array.isArray(summary.seats) ? summary.seats.length : 0;
        const unitSeatPrice = seatCount > 0 ? summary.seatSubtotal / seatCount : 0;
        setShowtime({
          id: 'current',
          movieId: summary.movie?.id || '',
          time: summary.startTime || '',
          format: 'Standard',
          price: unitSeatPrice,
          availableSeats: 0,
        });
      } catch (err) {
        console.error('Failed to fetch booking data:', err);
        setError(
          err instanceof Error
            ? err.message
            : 'Không thể tải thông tin vé. Vui lòng thử lại.'
        );
      } finally {
        setLoading(false);
      }
    };

    if (bookingId) {
      fetchData();
    }
  }, [bookingId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <Loader className="w-12 h-12 text-purple-600 mx-auto mb-4 animate-spin" />
          <p className="text-lg text-muted-foreground">Loading your ticket...</p>
        </div>
      </div>
    );
  }

  if (error || !bookingSummary || !movie || !showtime) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4 text-red-600">Lỗi</h1>
          <p className="text-muted-foreground mb-4">{error || 'Không tìm thấy thông tin vé'}</p>
          <Link href="/" className="text-purple-600 hover:text-purple-700 font-semibold flex items-center gap-2 justify-center">
            <ArrowLeft size={20} />
            Back to home
          </Link>
        </div>
      </div>
    );
  }

  // Map booking summary to Seat format
  const seatCount = Array.isArray(bookingSummary.seats) ? bookingSummary.seats.length : 0;
  const unitSeatPrice = seatCount > 0 ? bookingSummary.seatSubtotal / seatCount : 0;
  const selectedSeats: Seat[] = bookingSummary.seats.map((seat) => ({
    id: seat.seatName,
    row: seat.rowChair,
    number: seat.seatNumber,
    isAvailable: true,
    isSelected: true,
    type: 'standard',
    price: unitSeatPrice,
  }));

  // Map combos to ComboItem format
  const selectedCombos: ComboItem[] = bookingSummary.combos.map((combo) => ({
    id: combo.comboId,
    name: combo.comboName,
    price: combo.unitPrice,
    icon: '🍿',
    quantity: combo.quantity,
  }));

  return (
    <div className="min-h-screen bg-background dark:bg-slate-950 py-8 px-4">
      <div className="max-w-6xl mx-auto">

        <SuccessStep
          movie={movie}
          showtime={showtime}
          selectedSeats={selectedSeats}
          selectedCombos={selectedCombos}
          total={bookingSummary.totalAmount}
          bookingId={bookingId}
          status={bookingSummary.status}
        />
      </div>
    </div>
  );
}
