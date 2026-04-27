"use client";

import { useState, useEffect } from "react";
import { Loader2 } from "lucide-react";
import { getToken } from "@/services/localStorageService";
import { CONFIG } from "@/configurations/configuration";

interface PaymentStepProps {
  bookingId: string;
  total: number;
  onPaymentSuccess: () => void;
}

export default function PaymentStep({
  bookingId,
  total,
  onPaymentSuccess,
}: PaymentStepProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    const initializeCashPayment = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const token = getToken();
        if (!token) {
          throw new Error("No authentication token found. Please log in.");
        }

        const invoiceResponse = await fetch(
          `${CONFIG.API}/bookings/${bookingId}/create-invoice`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          },
        );

        if (!invoiceResponse.ok) {
          const errorText = await invoiceResponse.text();
          throw new Error(`Failed to create invoice: ${errorText}`);
        }

        const invoiceData = await invoiceResponse.json();
        const invoiceId = invoiceData?.result?.id;

        if (!invoiceId) {
          throw new Error("No invoice id returned by server");
        }

        const paymentResponse = await fetch(
          `${CONFIG.API}/payment/cash/${invoiceId}`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          },
        );

        if (!paymentResponse.ok) {
          const errorText = await paymentResponse.text();
          throw new Error(`Failed to process cash payment: ${errorText}`);
        }

        const paymentData = await paymentResponse.json();
        if (paymentData?.result?.code === "00") {
          onPaymentSuccess();
        } else {
          throw new Error(paymentData?.result?.message || "Cash payment failed");
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to process payment");
      } finally {
        setIsLoading(false);
      }
    };

    initializeCashPayment();
  }, [bookingId, onPaymentSuccess]);

  if (isLoading) {
    return (
      <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
        <h2 className="text-2xl font-bold mb-8">Payment</h2>
        <div className="flex flex-col items-center justify-center min-h-[400px]">
          <Loader2 className="w-12 h-12 animate-spin text-purple-600 mb-4" />
          <p className="text-lg font-semibold mb-2">Processing Cash Payment</p>
          <p className="text-sm text-muted-foreground text-center max-w-sm">
            Creating invoice and confirming cash payment...
          </p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
        <h2 className="text-2xl font-bold mb-8">Payment</h2>
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6 mb-6">
          <h3 className="font-bold text-red-800 dark:text-red-300 mb-2">
            Payment Error
          </h3>
          <p className="text-red-700 dark:text-red-400 text-sm">{error}</p>
        </div>
        <button
          onClick={() => window.location.reload()}
          className="w-full px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
      <h2 className="text-2xl font-bold mb-8">Payment</h2>
      <div className="flex flex-col items-center justify-center min-h-[400px]">
        <Loader2 className="w-12 h-12 animate-spin text-purple-600 mb-4" />
        <p className="text-lg font-semibold">Confirming cash payment...</p>
      </div>
    </div>
  );
}
