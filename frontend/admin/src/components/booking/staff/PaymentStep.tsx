import { useState } from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CheckCircle2, Loader2, Wallet } from "lucide-react";
import { format } from "date-fns";
import type { Seat, ComboItem, Showtime } from "../../../lib/types";
import type { MovieSimple } from "@/services/movieService";
import httpClient from "@/configurations/httpClient";

interface PaymentStepProps {
  bookingId: string;
  total: number;
  subtotal: number;
  discount: number;
  showtime: Showtime & { roomName: string; cinemaName: string };
  movie: MovieSimple;
  selectedSeats: Seat[];
  selectedCombos: ComboItem[];
  onPaymentSuccess: () => void;
  onExternalPaymentStart?: () => void;
}

export default function PaymentStep({
  bookingId,
  total,
  subtotal,
  discount,
  showtime,
  movie,
  selectedSeats,
  selectedCombos,
  onPaymentSuccess,
}: PaymentStepProps) {
  const [selectedMethod] = useState<"cash">("cash");
  const [isPaying, setIsPaying] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  const seatTotal = selectedSeats.reduce(
    (sum, seat) => sum + (seat.price || 0),
    0,
  );
  const comboTotal = selectedCombos.reduce(
    (sum, combo) => sum + combo.price * (combo.quantity || 1),
    0,
  );

  const handleCashPayment = async () => {
    if (isPaying) return;
    setPaymentError(null);
    setIsPaying(true);

    try {
      const invoiceResponse = await httpClient.post(
        `/bookings/${bookingId}/create-invoice`,
      );
      const invoiceId = invoiceResponse.data?.result?.id;

      if (!invoiceId) {
        throw new Error("Không lấy được mã hóa đơn từ hệ thống");
      }

      const paymentResponse = await httpClient.post(`/payment/cash/${invoiceId}`);
      if (paymentResponse.data?.result?.code === "00") {
        onPaymentSuccess();
      } else {
        throw new Error(
          paymentResponse.data?.result?.message || "Không thể xử lý thanh toán tiền mặt",
        );
      }
    } catch (error: any) {
      setPaymentError(
        error?.response?.data?.message ||
          error?.message ||
          "Không thể xử lý thanh toán tiền mặt",
      );
    } finally {
      setIsPaying(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500">Payment</p>
          <h2 className="text-2xl font-bold text-gray-900">
            Complete your booking
          </h2>
          <p className="text-sm text-gray-500">Booking ID: {bookingId}</p>
        </div>
        <div className="hidden sm:flex items-center gap-2 text-xs text-gray-500">
          <CheckCircle2 className="w-4 h-4 text-emerald-500" />
          Secure checkout
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <Card className="p-6 space-y-6 shadow-sm">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Payment Method
            </h3>
            <p className="text-sm text-gray-500">Choose how you want to pay</p>
          </div>

          <div className="space-y-3">
            <button
              className="w-full border rounded-lg px-4 py-3 text-left transition flex items-center gap-3 border-blue-600 bg-blue-50"
              disabled
            >
              <span className="h-5 w-5 rounded-full border flex items-center justify-center border-blue-600 bg-blue-600">
                <span className="h-2.5 w-2.5 bg-white rounded-full" />
              </span>
              <div className="flex items-center justify-between flex-1">
                <div className="flex items-center gap-3">
                  <Wallet className="w-5 h-5 text-gray-600" />
                  <div>
                    <p className="font-semibold text-gray-900">Cash</p>
                    <p className="text-sm text-gray-500">Collect cash at counter</p>
                  </div>
                </div>
                <CheckCircle2 className="w-5 h-5 text-blue-600" />
              </div>
            </button>
          </div>

          {paymentError && (
            <div className="mt-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {paymentError}
            </div>
          )}

          <div className="pt-4 grid gap-3 sm:flex sm:items-center sm:justify-between">
            <div className="text-sm text-gray-600 space-y-1">
              <p>Collect cash and confirm the booking.</p>
            </div>

            <Button
              onClick={handleCashPayment}
              disabled={isPaying}
              variant="outline"
            >
              {isPaying ? "Confirming..." : "Confirm cash payment"}
            </Button>
          </div>
        </Card>

        <Card className="p-6 space-y-4 shadow-sm">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Order Summary
            </h3>
            <p className="text-sm text-gray-500">
              Review your selection before paying
            </p>
          </div>

          <div className="space-y-2 text-sm">
            <div className="flex items-start justify-between">
              <div>
                <p className="font-semibold text-gray-900">{movie.title}</p>
                <p className="text-gray-500">
                  {format(new Date(showtime.time), "PPpp")}
                </p>
                <p className="text-gray-500">
                  {showtime.cinemaName} • {showtime.roomName}
                </p>
              </div>
            </div>
          </div>

          <div className="border-t pt-4 space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">
                Seats ({selectedSeats.length})
              </span>
              <span className="font-semibold text-gray-900">
                {seatTotal.toLocaleString()} VND
              </span>
            </div>
            <div className="flex flex-wrap gap-2 text-gray-700">
              {selectedSeats
                .slice()
                .sort((a, b) => {
                  if (a.row === b.row) return a.number - b.number;
                  return a.row.localeCompare(b.row);
                })
                .map((seat) => (
                  <span
                    key={seat.id}
                    className="px-2 py-1 rounded bg-gray-100 font-semibold text-xs"
                  >
                    {seat.row}
                    {seat.number}
                  </span>
                ))}
            </div>

            {selectedCombos.length > 0 && (
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-gray-600">Combos</span>
                  <span className="font-semibold text-gray-900">
                    {comboTotal.toLocaleString()} VND
                  </span>
                </div>
                <div className="space-y-1 text-gray-700">
                  {selectedCombos.map((combo) => (
                    <div
                      key={combo.id}
                      className="flex justify-between text-xs"
                    >
                      <span>
                        {combo.name} × {combo.quantity || 1}
                      </span>
                      <span className="font-semibold">
                        {(combo.price * (combo.quantity || 1)).toLocaleString()}{" "}
                        VND
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="border-t pt-4 space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Subtotal</span>
              <span className="font-semibold text-gray-900">
                {subtotal.toLocaleString()} VND
              </span>
            </div>
            {discount > 0 && (
              <div className="flex justify-between text-emerald-600">
                <span>Discount</span>
                <span>-{discount.toLocaleString()} VND</span>
              </div>
            )}
            <div className="flex justify-between items-center border-t pt-3 text-base font-bold text-blue-700">
              <span>Total</span>
              <span className="text-2xl">{total.toLocaleString()} VND</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
