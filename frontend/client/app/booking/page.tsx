"use client";

import { useEffect } from "react";
import { getToken } from "@/services/localStorageService";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function BookingPage() {
  const router = useRouter();

  useEffect(() => {
    const token = getToken();
    if (!token) {
      router.push("/");
    }
  }, [router]);

  return (
    <div className="min-h-screen pt-20 pb-12 px-4">
      <div className="container mx-auto max-w-6xl">
        <div className="mb-8">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors"
          >
            <svg
              className="w-4 h-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
            Back to Home
          </Link>
        </div>

        <h1 className="text-3xl font-bold mb-8">Book Your Tickets</h1>

        <div className="grid gap-6">
          <div className="bg-card border border-border rounded-2xl p-8">
            <div className="flex items-start gap-6">
              <div className="w-32 h-48 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center text-white font-bold">
                MOVIE
                <br />
                POSTER
              </div>

              <div className="flex-1">
                <h2 className="text-2xl font-bold mb-2">Example Movie Title</h2>
                <p className="text-muted-foreground mb-4">
                  Action • Adventure • 2h 30m • PG-13
                </p>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Select Cinema
                    </label>
                    <select className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500">
                      <option>Cinema 1 - Downtown</option>
                      <option>Cinema 2 - Mall</option>
                      <option>Cinema 3 - Plaza</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Select Date
                    </label>
                    <div className="grid grid-cols-4 gap-2">
                      {["Today", "Tomorrow", "Jan 10", "Jan 11"].map((date) => (
                        <button
                          key={date}
                          className="px-4 py-2 border border-border rounded-lg hover:bg-purple-500/10 hover:border-purple-500 transition-colors"
                        >
                          {date}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">
                      Select Time
                    </label>
                    <div className="grid grid-cols-4 gap-2">
                      {["10:00 AM", "1:30 PM", "4:00 PM", "7:30 PM"].map(
                        (time) => (
                          <button
                            key={time}
                            className="px-4 py-2 border border-border rounded-lg hover:bg-purple-500/10 hover:border-purple-500 transition-colors"
                          >
                            {time}
                          </button>
                        )
                      )}
                    </div>
                  </div>

                  <button className="w-full px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all mt-6">
                    Continue to Seat Selection
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-gradient-to-r from-purple-500/10 to-pink-500/10 border border-purple-500/20 rounded-lg p-6">
            <h3 className="font-semibold mb-2 flex items-center gap-2">
              <svg
                className="w-5 h-5 text-purple-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              Protected Page
            </h3>
            <p className="text-sm text-muted-foreground">
              This is a protected route. Only authenticated users can access
              this page. If you weren't logged in, you would have been shown the
              login modal automatically.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
