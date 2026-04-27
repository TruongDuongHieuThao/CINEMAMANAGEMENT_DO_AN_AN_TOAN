"use client";

import { useEffect, useState } from "react";
import { getToken, getUserInfo } from "@/services/localStorageService";
import { useRouter } from "next/navigation";
import {
  getTicketsByCustomer,
  TicketResponse,
  markTicketForTransfer,
  cancelTicketTransfer,
} from "@/services/ticketService";
import { getMyInfo } from "@/services/customerService";

export default function MyTicketsPage() {
  const router = useRouter();
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [page, setPage] = useState(1);
  const [customerId, setCustomerId] = useState<string | null>(null);
  const [passingTicket, setPassingTicket] = useState<string | null>(null);

  const PAGE_SIZE = 9;

  useEffect(() => {
    const token = getToken();
    if (!token) {
      router.push("/");
      return;
    }

    const fetchTickets = async () => {
      try {
        setLoading(true);

        // Try to get user info from localStorage first
        let userInfo = getUserInfo();

        // If not available in localStorage, fetch from API
        if (!userInfo || (!userInfo.id && !userInfo.customerId)) {
          try {
            userInfo = await getMyInfo();
          } catch (error: any) {
            console.error("Error fetching user info:", error);
            setError("Unable to fetch user information. Please sign in again.");
            return;
          }
        }

        // Get customer ID (prefer 'id' field, fallback to 'customerId')
        const cusId = userInfo?.id || userInfo?.customerId;

        if (!cusId) {
          setError("Customer information not found");
          return;
        }

        setCustomerId(cusId);
        const fetchedTickets = await getTicketsByCustomer(cusId);
        setTickets(fetchedTickets);
        setError(null);
      } catch (err) {
        console.error("Error fetching tickets:", err);
        setError("Failed to load tickets. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchTickets();
  }, [router]);

  const canPassTicket = (ticket: TicketResponse) => {
    if (ticket.status !== "ACTIVE") return false;

    const screeningTime = new Date(ticket.startTime).getTime();
    const now = Date.now();
    const hoursUntilScreening = (screeningTime - now) / (1000 * 60 * 60);

    return hoursUntilScreening >= 1;
  };

  const handlePassTicket = async (ticketCode: string) => {
    if (!customerId) return;

    try {
      setPassingTicket(ticketCode);
      await markTicketForTransfer(ticketCode, customerId);

      // Refresh tickets
      const fetchedTickets = await getTicketsByCustomer(customerId);
      setTickets(fetchedTickets);
    } catch (error: any) {
      console.error("Error marking ticket for transfer:", error);
      alert(
        error?.response?.data?.message || "Failed to mark ticket for transfer",
      );
    } finally {
      setPassingTicket(null);
    }
  };

  const handleCancelPass = async (ticketCode: string) => {
    if (!customerId) return;

    try {
      setPassingTicket(ticketCode);
      await cancelTicketTransfer(ticketCode, customerId);

      // Refresh tickets
      const fetchedTickets = await getTicketsByCustomer(customerId);
      setTickets(fetchedTickets);
    } catch (error: any) {
      console.error("Error cancelling ticket transfer:", error);
      alert(
        error?.response?.data?.message || "Failed to cancel ticket transfer",
      );
    } finally {
      setPassingTicket(null);
    }
  };

  const filteredTickets = tickets.filter((ticket) => {
    const normalizedSearch = searchTerm.trim().toLowerCase();
    const matchesTitle = normalizedSearch
      ? ticket.movieTitle.toLowerCase().includes(normalizedSearch)
      : true;

    const parsedTicketDate = ticket.startTime
      ? Date.parse(ticket.startTime)
      : NaN;
    const ticketDate = Number.isNaN(parsedTicketDate)
      ? null
      : new Date(parsedTicketDate).toISOString().slice(0, 10);
    const matchesDate = purchaseDate ? ticketDate === purchaseDate : true;

    return matchesTitle && matchesDate;
  });

  useEffect(() => {
    setPage(1);
  }, [searchTerm, purchaseDate, tickets]);

  const totalPages = Math.max(1, Math.ceil(filteredTickets.length / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const startIdx = (currentPage - 1) * PAGE_SIZE;
  const endIdx = startIdx + PAGE_SIZE;
  const paginatedTickets = filteredTickets.slice(startIdx, endIdx);

  return (
    <div className="min-h-screen pt-20 pb-12 px-4">
      <div className="container mx-auto max-w-6xl">
        <h1 className="text-3xl font-bold mb-8">My Tickets</h1>

        {loading ? (
          <div className="bg-card border border-border rounded-2xl p-8">
            <div className="text-center py-12">
              <div className="flex justify-center items-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
              </div>
              <p className="mt-4 text-muted-foreground">
                Loading your tickets...
              </p>
            </div>
          </div>
        ) : error ? (
          <div className="bg-card border border-border rounded-2xl p-8">
            <div className="text-center py-12">
              <div className="w-24 h-24 mx-auto mb-4 rounded-full bg-red-500/10 flex items-center justify-center">
                <svg
                  className="w-12 h-12 text-red-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8v4m0 4v.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <h2 className="text-xl font-semibold mb-2">Error</h2>
              <p className="text-muted-foreground mb-6">{error}</p>
              <button
                onClick={() => window.location.reload()}
                className="px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
              >
                Try Again
              </button>
            </div>
          </div>
        ) : tickets.length === 0 ? (
          <div className="bg-card border border-border rounded-2xl p-8">
            <div className="text-center py-12">
              <div className="w-24 h-24 mx-auto mb-4 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 flex items-center justify-center">
                <svg
                  className="w-12 h-12 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
                  />
                </svg>
              </div>
              <h2 className="text-xl font-semibold mb-2">No Tickets Yet</h2>
              <p className="text-muted-foreground mb-6">
                You haven't booked any tickets yet. Start booking to see your
                tickets here!
              </p>
              <button
                onClick={() => router.push("/movies")}
                className="px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
              >
                Browse Movies
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="bg-card border border-border rounded-2xl p-4 mb-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-2">
                    Search by movie name
                  </label>
                  <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Enter movie name..."
                    className="w-full px-3 py-2 rounded-lg border border-border bg-background focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-2">
                    Filter by showtime date
                  </label>
                  <input
                    type="date"
                    value={purchaseDate}
                    onChange={(e) => setPurchaseDate(e.target.value)}
                    className="w-full px-3 py-2 rounded-lg border border-border bg-background focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>
                <div className="flex items-end">
                  <button
                    onClick={() => {
                      setSearchTerm("");
                      setPurchaseDate("");
                    }}
                    className="w-full px-4 py-2 rounded-lg border border-border text-sm font-semibold hover:bg-muted transition-colors"
                  >
                    Clear filters
                  </button>
                </div>
              </div>
            </div>

            {filteredTickets.length === 0 ? (
              <div className="bg-card border border-border rounded-2xl p-8 text-center">
                <p className="text-muted-foreground">
                  No tickets match the current filters.
                </p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {paginatedTickets.map((ticket) => (
                    <div
                      key={ticket.id}
                      className={`relative rounded-xl p-6 transition-all hover:shadow-xl hover:-translate-y-1 border-2 bg-card ${
                        ticket.status === "ACTIVE"
                          ? "border-green-300 dark:border-green-700"
                          : ticket.status === "USED"
                            ? "border-blue-300 dark:border-blue-700"
                            : "border-red-300 dark:border-red-700"
                      }`}
                    >
                      <div className="flex justify-between items-start mb-4">
                        <div className="flex-1">
                          <h3 className="font-bold text-2xl text-purple-700 dark:text-purple-300 mb-3 line-clamp-2">
                            {ticket.movieTitle}
                          </h3>
                          <div className="space-y-1">
                            <p className="text-xs text-slate-500 dark:text-slate-400">
                              <span className="font-semibold">Ticket:</span>{" "}
                              {ticket.ticketCode}
                            </p>
                            <p className="text-xs text-slate-500 dark:text-slate-400">
                              <span className="font-semibold">Seat:</span>{" "}
                              {ticket.seatName}
                            </p>
                          </div>
                        </div>
                        <span
                          className={`px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap ml-2 ${
                            ticket.status === "ACTIVE"
                              ? "bg-green-500 text-white"
                              : ticket.status === "FOR_TRANSFER"
                                ? "bg-orange-500 text-white"
                                : ticket.status === "USED"
                                  ? "bg-blue-500 text-white"
                                  : "bg-red-500 text-white"
                          }`}
                        >
                          {ticket.status === "FOR_TRANSFER"
                            ? "PASS"
                            : ticket.status}
                        </span>
                      </div>
                      <div className="space-y-2.5 text-sm mb-4 pb-4 border-b-2 border-current border-opacity-20">
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-slate-700 dark:text-slate-300">
                            Showtime
                          </span>
                          <span className="text-right text-slate-600 dark:text-slate-300">
                            {new Date(ticket.startTime).toLocaleString()}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-slate-700 dark:text-slate-300">
                            Price
                          </span>
                          <span className="text-right text-purple-600 dark:text-purple-400 font-bold">
                            {ticket.price.toLocaleString()} VND
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-slate-700 dark:text-slate-300">
                            Purchased
                          </span>
                          <span className="text-right text-slate-600 dark:text-slate-300 text-xs">
                            {ticket.createdAt
                              ? new Date(ticket.createdAt).toLocaleString()
                              : new Date(
                                  ticket.purchaseDate || ticket.startTime,
                                ).toLocaleString()}
                          </span>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-slate-700 dark:text-slate-300">
                            Expires
                          </span>
                          <span className="text-right text-orange-600 dark:text-orange-400 font-semibold text-xs">
                            {new Date(ticket.expiresAt).toLocaleString()}
                          </span>
                        </div>
                      </div>
                      {ticket.qrContent && (
                        <div className="mb-4 p-4 bg-white dark:bg-slate-800 rounded-xl flex justify-center border border-slate-200 dark:border-slate-700">
                          <img
                            src={`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(ticket.qrContent)}`}
                            alt="QR Code"
                            className="w-32 h-32"
                          />
                        </div>
                      )}

                      {/* Pass/Cancel Pass Button */}
                      {ticket.status === "ACTIVE" && canPassTicket(ticket) && (
                        <button
                          onClick={() => handlePassTicket(ticket.ticketCode)}
                          disabled={passingTicket === ticket.ticketCode}
                          className="w-full px-4 py-2.5 rounded-lg bg-orange-500 hover:bg-orange-600 text-white font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {passingTicket === ticket.ticketCode
                            ? "Processing..."
                            : "Pass Ticket"}
                        </button>
                      )}

                      {ticket.status === "FOR_TRANSFER" && (
                        <button
                          onClick={() => handleCancelPass(ticket.ticketCode)}
                          disabled={passingTicket === ticket.ticketCode}
                          className="w-full px-4 py-2.5 rounded-lg bg-gray-500 hover:bg-gray-600 text-white font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {passingTicket === ticket.ticketCode
                            ? "Processing..."
                            : "Cancel Pass"}
                        </button>
                      )}
                    </div>
                  ))}
                </div>
                <div className="flex flex-col sm:flex-row items-center justify-between mt-6 gap-3">
                  <span className="text-sm text-muted-foreground">
                    Showing {filteredTickets.length === 0 ? 0 : startIdx + 1}-
                    {Math.min(endIdx, filteredTickets.length)} of{" "}
                    {filteredTickets.length}
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setPage((prev) => Math.max(1, prev - 1))}
                      disabled={currentPage === 1}
                      className="px-4 py-2 rounded-lg border border-border text-sm font-semibold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted transition-colors"
                    >
                      Previous
                    </button>
                    <span className="text-sm font-semibold">
                      Page {currentPage} / {totalPages}
                    </span>
                    <button
                      onClick={() =>
                        setPage((prev) => Math.min(totalPages, prev + 1))
                      }
                      disabled={currentPage === totalPages}
                      className="px-4 py-2 rounded-lg border border-border text-sm font-semibold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted transition-colors"
                    >
                      Next
                    </button>
                  </div>
                </div>
              </>
            )}
          </>
        )}
      </div>
    </div>
  );
}
