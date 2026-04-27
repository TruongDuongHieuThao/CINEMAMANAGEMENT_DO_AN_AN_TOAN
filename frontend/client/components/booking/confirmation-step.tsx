"use client";

import { useMemo, useState } from "react";
import type { Movie, Showtime, Seat, ComboItem } from "@/lib/types";
import type { BookingSummaryResponse } from "@/services/bookingService";

interface ConfirmationStepProps {
  movie: Movie;
  showtime: Showtime;
  selectedSeats: Seat[];
  selectedCombos: ComboItem[];
  subtotal: number;
  pointsUsed?: number;
  pointsDiscount?: number;
  total: number;
  customerPoints?: number;
  onApplyPoints?: (pointsToUse: number) => void;
  bookingSummary: BookingSummaryResponse | null;
  isLoadingSummary: boolean;
}

export default function ConfirmationStep({
  movie,
  showtime,
  selectedSeats,
  selectedCombos,
  subtotal,
  pointsUsed = 0,
  pointsDiscount = 0,
  total,
  customerPoints = 0,
  onApplyPoints,
  bookingSummary,
  isLoadingSummary,
}: ConfirmationStepProps) {
  const [pointsInput, setPointsInput] = useState(pointsUsed);

  const resolvedSubtotal = useMemo(() => {
    const serverSubtotal = bookingSummary?.subTotal;
    return serverSubtotal !== undefined ? Number(serverSubtotal) : subtotal;
  }, [bookingSummary, subtotal]);

  const resolvedTotalBeforePoints = useMemo(() => {
    // Use subtotal for calculating base (before points), not totalAmount which may include other discounts
    const serverSubtotal = bookingSummary?.subTotal;
    return serverSubtotal !== undefined ? Number(serverSubtotal) : subtotal;
  }, [bookingSummary, subtotal]);

  const resolvedTotal = useMemo(() => {
    // Always calculate client-side in confirmation step for real-time updates when user changes points
    return Math.max(0, resolvedTotalBeforePoints - pointsDiscount);
  }, [resolvedTotalBeforePoints, pointsDiscount]);

  const formattedStartTime = useMemo(() => {
    if (bookingSummary?.startTime) {
      const date = new Date(bookingSummary.startTime);
      return `${date.toLocaleDateString()} ${date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
    }
    return `${showtime.time} • ${showtime.format}`;
  }, [bookingSummary?.startTime, showtime.format, showtime.time]);

  const maxPointsCanUse = useMemo(() => {
    const fiftyPercentCap = Math.floor(resolvedTotalBeforePoints * 0.5);
    // 1 point = 1000 VND, so divide VND amounts by 1000 to get max points
    const maxPointsBySubtotal = Math.floor(resolvedSubtotal / 1000);
    const maxPointsByFiftyCap = Math.floor(fiftyPercentCap / 1000);
    return Math.min(customerPoints, maxPointsBySubtotal, maxPointsByFiftyCap);
  }, [customerPoints, resolvedSubtotal, resolvedTotalBeforePoints]);

  const summarySeats = bookingSummary?.seats?.length
    ? bookingSummary.seats.map(
        (seat) => seat.seatName || `${seat.rowChair}${seat.seatNumber}`,
      )
    : selectedSeats.map((seat) => seat.id);

  const summaryCombos = bookingSummary?.combos?.length
    ? bookingSummary.combos.map((combo) => ({
        id: combo.comboId,
        name:
          combo.comboName ||
          selectedCombos.find((c) => c.id === combo.comboId)?.name ||
          combo.comboId,
        quantity: combo.quantity,
        price: Number(combo.unitPrice ?? 0),
        subtotal: Number(combo.subtotal ?? 0),
      }))
    : selectedCombos.map((combo) => ({
        id: combo.id,
        name: combo.name,
        quantity: combo.quantity || 1,
        price: combo.price,
        subtotal: combo.price * (combo.quantity || 1),
      }));

  return (
    <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
      <h2 className="text-2xl font-bold mb-8">Confirm Your Booking</h2>

      {isLoadingSummary && (
        <div className="mb-6 p-4 rounded-lg border border-border dark:border-slate-800 bg-muted/60 dark:bg-slate-800 flex items-center justify-between">
          <span className="text-sm text-muted-foreground">
            Đang tải thông tin đơn đặt...
          </span>
          <div className="inline-block w-5 h-5 border-2 border-purple-600 border-t-transparent rounded-full animate-spin" />
        </div>
      )}

      {/* Movie Info */}
      <div className="mb-8 pb-8 border-b border-border dark:border-slate-800">
        <h3 className="font-bold mb-4">Movie Details</h3>
        <div className="space-y-3">
          <div>
            <p className="text-sm text-muted-foreground">Movie</p>
            <p className="font-semibold">
              {bookingSummary?.movie?.title || movie.title}
            </p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Time</p>
            <p className="font-semibold">{formattedStartTime}</p>
          </div>
        </div>
      </div>

      {bookingSummary?.movie?.ageRating && (
        <div className="mb-8 pb-8 border-b border-border dark:border-slate-800">
          <div className="space-y-3">
            <div>
              <p className="text-sm text-muted-foreground">Age Rating</p>
              <p className="font-semibold">
                {bookingSummary.movie.ageRating.code}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Seats */}
      <div className="mb-8 pb-8 border-b border-border dark:border-slate-800">
        <h3 className="font-bold mb-4">
          Selected Seats ({summarySeats.length})
        </h3>
        <div className="flex flex-wrap gap-2">
          {summarySeats.map((seat) => (
            <span
              key={seat}
              className="px-3 py-1 rounded-full bg-purple-500/20 text-purple-600 dark:text-purple-300 text-sm font-semibold"
            >
              {seat}
            </span>
          ))}
        </div>
      </div>

      {/* Combos */}
      {summaryCombos.length > 0 && (
        <div className="mb-8 pb-8 border-b border-border dark:border-slate-800">
          <h3 className="font-bold mb-4">
            Selected Combos ({summaryCombos.length})
          </h3>
          <div className="space-y-2">
            {summaryCombos.map((combo) => (
              <div key={combo.id} className="flex justify-between text-sm">
                <span>
                  {combo.name} x{combo.quantity}
                </span>
                <span className="font-semibold">
                  {(
                    combo.subtotal || combo.price * combo.quantity
                  ).toLocaleString()}{" "}
                  VND
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Discount Code */}
      <div className="mb-8 pb-8 border-b border-border dark:border-slate-800">
        <h3 className="font-bold mb-4">Use Loyalty Points</h3>
        <div className="bg-purple-500/10 dark:bg-purple-900/20 border border-purple-500/30 rounded-lg p-4 mb-4">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm font-semibold">Available Points</span>
            <span className="text-2xl font-bold text-purple-600">
              {customerPoints.toLocaleString()}
            </span>
          </div>
          <p className="text-xs text-muted-foreground">
            1 point = 1,000 VND discount
          </p>
        </div>
        <div className="flex gap-2">
          <input
            type="number"
            value={pointsInput}
            onChange={(e) => {
              const value = Math.min(
                Math.max(0, parseInt(e.target.value) || 0),
                maxPointsCanUse,
              );
              setPointsInput(value);
            }}
            placeholder="Enter points to redeem"
            max={maxPointsCanUse}
            className="flex-1 px-4 py-2 rounded-lg border border-border dark:border-slate-700 bg-background dark:bg-slate-800 focus:outline-none focus:border-purple-500 transition-colors"
          />
          <button
            onClick={() => onApplyPoints?.(pointsInput)}
            className="px-4 py-2 rounded-lg bg-purple-600 text-white font-semibold hover:bg-purple-700 transition-colors"
          >
            Apply
          </button>
        </div>
        <p className="text-xs text-muted-foreground mt-2">
          Maximum points you can use: {maxPointsCanUse.toLocaleString()}
        </p>
      </div>

      {/* Price Summary */}
      <div className="space-y-3">
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Subtotal</span>
          <span className="font-semibold">
            {resolvedSubtotal.toLocaleString()} VND
          </span>
        </div>
        {pointsDiscount > 0 && (
          <div className="flex justify-between text-sm text-green-600">
            <span>Points Discount ({pointsUsed.toLocaleString()} points)</span>
            <span>-{pointsDiscount.toLocaleString()} VND</span>
          </div>
        )}
        <div className="flex justify-between items-center pt-3 border-t border-border dark:border-slate-800">
          <span className="font-bold">Total Amount</span>
          <span className="text-2xl font-bold text-purple-600">
            {resolvedTotal.toLocaleString()} VND
          </span>
        </div>
      </div>
    </div>
  );
}
