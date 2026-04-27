"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import {
  CheckCircle,
  Download,
  Share2,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import QRCode from "qrcode";
import type { Movie, Showtime, Seat, ComboItem } from "@/lib/types";
import {
  getTicketsByBooking,
  type TicketResponse,
} from "@/services/ticketService";

interface SuccessStepProps {
  movie: Movie;
  showtime: Showtime;
  selectedSeats: Seat[];
  selectedCombos: ComboItem[];
  total: number;
  bookingId: string;
  status?: string;
}

export default function SuccessStep({
  movie,
  showtime,
  selectedSeats,
  selectedCombos,
  total,
  bookingId,
  status,
}: SuccessStepProps) {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [currentTicketIndex, setCurrentTicketIndex] = useState(0);
  const [qrCodeUrls, setQrCodeUrls] = useState<string[]>([]);
  const [isLoadingTickets, setIsLoadingTickets] = useState(true);
  const bookingDate = new Date().toLocaleDateString();
  const isRefunded = (status || "").toUpperCase() === "REFUNDED";

  // Fetch tickets when component mounts (skip QR generation for refunded)
  useEffect(() => {
    const fetchTickets = async () => {
      try {
        setIsLoadingTickets(true);
        const fetchedTickets = await getTicketsByBooking(bookingId);
        setTickets(fetchedTickets);

        if (!isRefunded && fetchedTickets.length > 0) {
          // Generate QR codes for all tickets
          const qrPromises = fetchedTickets.map((ticket) =>
            QRCode.toDataURL(ticket.qrContent || ticket.ticketCode, {
              width: 300,
              margin: 2,
              color: {
                dark: "#000000",
                light: "#FFFFFF",
              },
            }),
          );
          const qrUrls = await Promise.all(qrPromises);
          setQrCodeUrls(qrUrls);
        } else {
          setQrCodeUrls([]);
        }
      } catch (error) {
        console.error("Failed to fetch tickets:", error);
      } finally {
        setIsLoadingTickets(false);
      }
    };

    if (bookingId) {
      fetchTickets();
    }
  }, [bookingId, isRefunded]);

  const handlePrevTicket = () => {
    setCurrentTicketIndex((prev) => (prev > 0 ? prev - 1 : tickets.length - 1));
  };

  const handleNextTicket = () => {
    setCurrentTicketIndex((prev) => (prev < tickets.length - 1 ? prev + 1 : 0));
  };

  const currentTicket = tickets[currentTicketIndex];

  return (
    <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
      {/* Success/Refund Message */}
      <div className="text-center mb-12">
        <div className="flex justify-center mb-4">
          <CheckCircle
            size={64}
            className={isRefunded ? "text-yellow-500" : "text-green-500"}
          />
        </div>
        {isRefunded ? (
          <>
            <h2 className="text-3xl font-bold mb-2">Booking Refunded</h2>
            <p className="text-muted-foreground">
              This booking was refunded. Tickets are no longer valid.
            </p>
          </>
        ) : (
          <>
            <h2 className="text-3xl font-bold mb-2">Booking Confirmed!</h2>
            <p className="text-muted-foreground">
              Your tickets have been successfully booked
            </p>
          </>
        )}
      </div>

      {/* Booking Details */}
      <div className="bg-muted dark:bg-slate-800 rounded-lg p-6 mb-8">
        <div className="grid grid-cols-2 gap-6 mb-6">
          <div>
            <p className="text-sm text-muted-foreground mb-1">Booking ID</p>
            <p className="font-mono font-bold">{bookingId}</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground mb-1">Booking Date</p>
            <p className="font-semibold">{bookingDate}</p>
          </div>
        </div>

        <div className="border-t border-border dark:border-slate-700 pt-6">
          <h3 className="font-bold mb-4">Movie Information</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-muted-foreground">Movie</p>
              <p className="font-semibold">{movie.title}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Time</p>
              <p className="font-semibold">{showtime.time}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Format</p>
              <p className="font-semibold">{showtime.format}</p>
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Seats</p>
              <p className="font-semibold">
                {selectedSeats.length > 0
                  ? selectedSeats.map((s) => s.id).join(", ")
                  : isRefunded
                    ? "—"
                    : ""}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Ticket Carousel */}
      <div className="mb-8">
        <h3 className="font-bold text-xl mb-4 text-center">Your Tickets</h3>

        {isRefunded ? (
          <div className="text-center py-8 text-muted-foreground">
            <p>Tickets are unavailable because this booking was refunded.</p>
          </div>
        ) : isLoadingTickets ? (
          <div className="flex justify-center py-12">
            <div className="text-center">
              <div className="inline-block w-8 h-8 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
              <p className="text-muted-foreground mt-4">Loading tickets...</p>
            </div>
          </div>
        ) : tickets.length > 0 ? (
          <div className="relative">
            {/* Ticket Counter */}
            <div className="text-center mb-4">
              <div className="inline-flex items-center gap-3">
                <button
                  onClick={handlePrevTicket}
                  className="p-2 rounded-full hover:bg-muted dark:hover:bg-slate-800 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                  disabled={tickets.length <= 1}
                  aria-label="Previous ticket"
                >
                  <ChevronLeft size={24} />
                </button>
                <span className="text-lg font-semibold min-w-[100px]">
                  Ticket {currentTicketIndex + 1} / {tickets.length}
                </span>
                <button
                  onClick={handleNextTicket}
                  className="p-2 rounded-full hover:bg-muted dark:hover:bg-slate-800 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                  disabled={tickets.length <= 1}
                  aria-label="Next ticket"
                >
                  <ChevronRight size={24} />
                </button>
              </div>
            </div>

            {/* Ticket Card */}
            {currentTicket && (
              <div className="max-w-md mx-auto">
                <div className="bg-gradient-to-br from-purple-50 to-pink-50 dark:from-purple-900/20 dark:to-pink-900/20 border-2 border-purple-300 dark:border-purple-700 rounded-2xl p-8 shadow-lg">
                  {/* QR Code */}
                  <div className="bg-white rounded-xl p-4 mb-6 flex justify-center">
                    {qrCodeUrls[currentTicketIndex] ? (
                      <img
                        src={qrCodeUrls[currentTicketIndex]}
                        alt="Ticket QR Code"
                        className="w-64 h-64"
                      />
                    ) : (
                      <div className="w-64 h-64 flex items-center justify-center text-muted-foreground">
                        Loading QR...
                      </div>
                    )}
                  </div>

                  {/* Ticket Details */}
                  <div className="space-y-3 text-center">
                    <div className="pb-3 border-b border-purple-200 dark:border-purple-700">
                      <p className="text-sm text-muted-foreground mb-1">Seat</p>
                      <p className="text-3xl font-bold text-purple-600">
                        {currentTicket.seatName}
                      </p>
                    </div>

                    <div className="pb-3 border-b border-purple-200 dark:border-purple-700">
                      <p className="text-sm text-muted-foreground mb-1">
                        Movie
                      </p>
                      <p className="text-lg font-semibold">{movie.title}</p>
                    </div>

                    <div className="pb-3 border-b border-purple-200 dark:border-purple-700">
                      <p className="text-sm text-muted-foreground mb-1">
                        Showtime
                      </p>
                      <p className="text-lg font-semibold">{showtime.time}</p>
                      <p className="text-sm text-muted-foreground">
                        {showtime.format}
                      </p>
                    </div>

                    <div className="pb-3 border-b border-purple-200 dark:border-purple-700">
                      <p className="text-sm text-muted-foreground mb-1">
                        Ticket Code
                      </p>
                      <p className="text-lg font-mono font-bold text-purple-600">
                        {currentTicket.ticketCode}
                      </p>
                    </div>

                    <div>
                      <p className="text-sm text-muted-foreground mb-1">
                        Price
                      </p>
                      <p className="text-lg font-semibold">
                        {currentTicket.price.toLocaleString()} VND
                      </p>
                    </div>
                  </div>

                  {/* Show at entrance notice */}
                  <div className="mt-6 bg-yellow-100 dark:bg-yellow-900/30 border border-yellow-300 dark:border-yellow-700 rounded-lg p-3">
                    <p className="text-sm text-center font-semibold text-yellow-800 dark:text-yellow-200">
                      Show this QR code at the entrance
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="text-center py-8 text-muted-foreground">
            <p>No tickets found. Please contact support.</p>
          </div>
        )}
      </div>

      {/* Order Summary */}
      <div className="bg-purple-500/10 dark:bg-purple-900/20 border border-purple-500/30 rounded-lg p-6 mb-8">
        <h3 className="font-bold mb-4">Order Summary</h3>
        <div className="space-y-2 mb-4">
          <div className="flex justify-between text-sm">
            <span>Seats ({selectedSeats.length})</span>
            <span className="font-semibold">
              {(selectedSeats.length * (showtime.price || 0)).toLocaleString()}{" "}
              VND
            </span>
          </div>
          {selectedCombos.length > 0 && (
            <div className="flex justify-between text-sm">
              <span>Combos ({selectedCombos.length})</span>
              <span className="font-semibold">
                {selectedCombos
                  .reduce((sum, c) => sum + c.price * (c.quantity || 1), 0)
                  .toLocaleString()}{" "}
                VND
              </span>
            </div>
          )}
        </div>
        <div className="border-t border-purple-500/30 pt-4 flex justify-between items-center">
          <span className="font-bold">
            {isRefunded ? "Refunded Amount" : "Total Paid"}
          </span>
          <span className="text-2xl font-bold text-purple-600">
            {(total || 0).toLocaleString()} VND
          </span>
        </div>
      </div>

      {/* Actions */}
      <div className="mb-8">
        <Link
          href="/"
          className="w-full block text-center px-4 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
        >
          Back to Home
        </Link>
      </div>

      {/* Important Info */}
      <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-4">
        <p className="text-sm font-semibold mb-2">Important Information</p>
        <ul className="text-sm text-muted-foreground space-y-1">
          <li>• Please arrive 15 minutes before the showtime</li>
          <li>• Show your ticket QR code at the entrance</li>
          <li>• A confirmation email has been sent to your registered email</li>
          <li>
            • You can cancel up to 2 hours before showtime for a full refund
          </li>
        </ul>
      </div>
    </div>
  );
}
