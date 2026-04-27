"use client";

import { useState, useEffect } from "react";
import { useAuthStore } from "@/stores/useAuthStore";

export const TicketBookingPage = () => {
  const { cinemaId } = useAuthStore();

  const [showtimes, setShowtimes] = useState<any[]>([]);
  const [showtimesLoading, setShowtimesLoading] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState<any>(null);

  // ✅ FIX: đưa logic vào useEffect
  useEffect(() => {
    const fetchShowtimes = async () => {
      try {
        setShowtimesLoading(true);

        // giả lập data (giữ logic của bạn)
        let data: any[] = []; // TODO: thay bằng API của bạn
        let filtered = data;

        if (cinemaId) {
          filtered = data.filter((st) => st.cinemaId === cinemaId);
        }

        const now = new Date();
        filtered = filtered.filter(
          (st) => new Date(st.startTime).getTime() > now.getTime()
        );

        // Sort
        filtered.sort(
          (a, b) =>
            new Date(a.startTime).getTime() -
            new Date(b.startTime).getTime()
        );

        setShowtimes(filtered);
      } catch (error: any) {
        console.error("Error fetching showtimes:", error);
        setShowtimes([]);
      } finally {
        setShowtimesLoading(false);
      }
    };

    fetchShowtimes();
  }, [cinemaId, selectedMovie]);

  // ✅ UI để cuối cùng
  return (
    <div className="min-h-[60vh] rounded-xl border border-dashed border-slate-300 bg-white p-8">
      <h1 className="text-2xl font-semibold text-slate-900">
        Ticket Booking
      </h1>

      <p className="mt-2 text-sm text-slate-600">
        Booking flow is temporarily simplified so the admin app can compile and run.
      </p>

      <div className="mt-4">
        {showtimesLoading ? (
          <p>Loading showtimes...</p>
        ) : (
          <pre className="text-xs">
            {JSON.stringify(showtimes, null, 2)}
          </pre>
        )}
      </div>
    </div>
  );
};