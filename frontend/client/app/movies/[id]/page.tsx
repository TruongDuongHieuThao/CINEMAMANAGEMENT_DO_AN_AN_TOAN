"use client";

import { useState, useEffect, useMemo } from "react";
import Link from "next/link";
import { use } from "react";
import {
  ChevronLeft,
  Clock,
  MapPin,
  Calendar as CalendarIcon,
} from "lucide-react";
import type { Showtime } from "@/lib/types";
import {
  getMovieById,
  getMovieBySlug,
  mapMovieForDisplay,
  getScreeningsByMovieId,
  mapScreeningToShowtime,
  getAllCinemas,
} from "@/lib/api-movie";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ReviewsSection } from "./reviews-section";

export default function MovieDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const [movie, setMovie] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [selectedCinema, setSelectedCinema] = useState<string>("");
  const [selectedDate, setSelectedDate] = useState<string>("");
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(
    null,
  );
  const [allShowtimes, setAllShowtimes] = useState<Showtime[]>([]);
  const [cinemas, setCinemas] = useState<
    Array<{ id: string; name: string; location?: string }>
  >([]);
  const [showtimeLoading, setShowtimeLoading] = useState(false);
  const [cinemaLoading, setCinemaLoading] = useState(false);

  // Load movie by slug or id
  useEffect(() => {
    const fetchMovie = async () => {
      if (!id) return;
      try {
        setLoading(true);
        let data;
        
        // Kiểm tra xem id có phải UUID format không
        const isUUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id);
        
        if (isUUID) {
          // Nếu là UUID, gọi trực tiếp getMovieById
          data = await getMovieById(id);
        } else {
          // Nếu không phải UUID, chỉ gọi getMovieBySlug
          data = await getMovieBySlug(id);
        }
        
        const mapped = mapMovieForDisplay(data);
        setMovie(mapped);
      } catch (error) {
        console.error("Error fetching movie", error);
        setMovie(null);
      } finally {
        setLoading(false);
      }
    };
    fetchMovie();
  }, [id]);

  // Load cinemas once
  useEffect(() => {
    const fetchCinemas = async () => {
      try {
        setCinemaLoading(true);
        const data = await getAllCinemas();
        setCinemas(data || []);
      } catch (error) {
        console.error("Error fetching cinemas", error);
        setCinemas([]);
      } finally {
        setCinemaLoading(false);
      }
    };
    fetchCinemas();
  }, []);

  // Load screenings for this movie
  useEffect(() => {
    const fetchScreenings = async () => {
      if (!movie?.id) return; // Đợi movie được load trước
      try {
        setShowtimeLoading(true);
        const data = await getScreeningsByMovieId(movie.id); // Dùng movie.id thực sự
        const mapped = (data || [])
          .map(mapScreeningToShowtime)
          .filter(Boolean) as Showtime[];
        setAllShowtimes(mapped);

        // Default selection: All cinemas and today's date
        if (mapped.length > 0) {
          const today = new Date().toISOString().split("T")[0]; // Today's date in YYYY-MM-DD format
          const now = new Date();

          // Get only future dates (today or later)
          const futureDates = mapped
            .filter((st) => {
              const start = st.startDateTime
                ? new Date(st.startDateTime)
                : new Date(`${st.date}T${st.time}`);
              return start.getTime() >= now.getTime();
            })
            .map((st) => st.date)
            .filter(Boolean);

          const availableDates = Array.from(new Set(futureDates)).sort();

          // Set to today if available, otherwise first available future date
          const defaultDate = availableDates.includes(today)
            ? today
            : availableDates[0];

          // Always set to today or first available future date
          if (defaultDate) {
            setSelectedDate(defaultDate);
          }
          setSelectedCinema("all"); // "all" = All Cinemas
        }
      } catch (error) {
        console.error("Error fetching screenings", error);
        setAllShowtimes([]);
      } finally {
        setShowtimeLoading(false);
      }
    };

    fetchScreenings();
  }, [movie]); // Dependency là movie thay vì id

  // Derived data
  const availableCinemas = useMemo(() => {
    const cinemaMap = new Map(cinemas.map((c) => [c.id, c]));
    const ids = Array.from(
      new Set(allShowtimes.map((st) => st.cinemaId).filter(Boolean)),
    ) as string[];
    return ids
      .map((cid) => cinemaMap.get(cid))
      .filter(Boolean) as typeof cinemas;
  }, [cinemas, allShowtimes]);

  const availableDates = useMemo(() => {
    const now = new Date();
    const futureDates = allShowtimes
      .filter((st) => {
        const start = st.startDateTime
          ? new Date(st.startDateTime)
          : new Date(`${st.date}T${st.time}`);
        return start.getTime() > now.getTime();
      })
      .map((st) => st.date)
      .filter(Boolean);
    return Array.from(new Set(futureDates)).sort();
  }, [allShowtimes]);

  const filteredShowtimes = useMemo(() => {
    const now = new Date();
    return allShowtimes.filter((st) => {
      // filter by cinema/date
      if (
        selectedCinema &&
        selectedCinema !== "all" &&
        st.cinemaId !== selectedCinema
      )
        return false;
      if (selectedDate && st.date !== selectedDate) return false;

      // remove past showtimes (including same-day already ended)
      const start = st.startDateTime
        ? new Date(st.startDateTime)
        : new Date(`${st.date}T${st.time}`);
      return start.getTime() > now.getTime();
    });
  }, [allShowtimes, selectedCinema, selectedDate]);

  useEffect(() => {
    const fetchMovie = async () => {
      try {
        setLoading(true);
        const data = await getMovieById(id);

        if (data) {
          const mappedMovie = mapMovieForDisplay(data);
          setMovie(mappedMovie);
        } else {
          console.error("❌ No data returned");
        }
      } catch (error) {
        console.error("❌ Error fetching movie:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchMovie();
  }, [id]);

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 pt-20">
        <div className="container-max px-4 md:px-8 py-12">
          <div className="animate-pulse space-y-8">
            <div className="bg-gray-700 h-96 rounded-xl"></div>
            <div className="space-y-4">
              <div className="bg-gray-700 h-8 w-1/2 rounded"></div>
              <div className="bg-gray-700 h-4 w-full rounded"></div>
              <div className="bg-gray-700 h-4 w-3/4 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Movie not found
  if (!movie) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4">Movie Not Found</h1>
          <p className="text-muted-foreground mb-6">ID: {id}</p>
          <Link
            href="/"
            className="text-purple-600 hover:text-purple-700 font-semibold"
          >
            Back to Home
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background dark:bg-slate-950 pt-20">
      {/* Movie Hero */}
      <div className="relative h-96 md:h-[500px] overflow-hidden bg-gradient-to-b from-purple-900/20 to-background dark:to-slate-950">
        <img
          src={movie.poster || "/placeholder.svg"}
          alt={movie.title}
          className="w-full h-full object-cover opacity-40"
          onError={(e) => {
            e.currentTarget.src = "/placeholder.svg";
          }}
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background dark:from-slate-950 via-transparent" />

        <div className="absolute top-6 left-6 z-20">
          <Link
            href="/"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-black/40 backdrop-blur-md
                       text-white font-medium hover:bg-black/60 transition-colors"
          >
            <ChevronLeft size={20} />
            Back to Home
          </Link>
        </div>
      </div>

      {/* Movie Info */}
      <div className="container-max px-4 md:px-8 -mt-32 relative z-10 mb-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Poster */}
          <div className="flex justify-center md:justify-start">
            <img
              src={movie.poster || "/placeholder.svg"}
              alt={movie.title}
              className="w-48 h-72 rounded-xl shadow-2xl object-cover"
              onError={(e) => {
                e.currentTarget.src = "/placeholder.svg";
              }}
            />
          </div>

          {/* Details */}
          <div className="md:col-span-2 space-y-6">
            <div>
              <h1 className="text-4xl md:text-5xl font-bold mb-4">
                {movie.title}
              </h1>
              <div className="flex flex-wrap gap-2 mb-4">
                {movie.genre && movie.genre.length > 0 ? (
                  movie.genre.map((g: string) => (
                    <span
                      key={g}
                      className="px-3 py-1 rounded-full bg-purple-500/20 text-purple-600 dark:text-purple-300 text-sm font-semibold"
                    >
                      {g}
                    </span>
                  ))
                ) : (
                  <span className="text-muted-foreground text-sm">
                    No genres
                  </span>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Rating</p>
                <p className="text-2xl font-bold">{movie.rating || "NR"}</p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Duration</p>
                <p className="text-2xl font-bold flex items-center gap-2">
                  <Clock size={20} />
                  {movie.duration || 0}m
                </p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Release</p>
                <p className="text-lg font-bold">
                  {movie.releaseDate
                    ? new Date(movie.releaseDate).toLocaleDateString()
                    : "TBA"}
                </p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Director</p>
                <p className="text-lg font-bold">
                  {movie.director || "Unknown"}
                </p>
              </div>
            </div>

            {/* ✅ FIX: Cast có thể là string hoặc array */}
            {movie.cast && movie.cast.length > 0 && (
              <div>
                <h3 className="text-lg font-bold mb-2">Cast</h3>
                <p className="text-muted-foreground">
                  {Array.isArray(movie.cast)
                    ? movie.cast.join(", ")
                    : movie.cast}
                </p>
              </div>
            )}

            <div>
              <h3 className="text-lg font-bold mb-2">Synopsis</h3>
              <p className="text-muted-foreground leading-relaxed">
                {movie.description || "No description available."}
              </p>
            </div>

            {/* Trailer button nếu có */}
            {movie.trailerUrl && (
              <div>
                <a
                  href={movie.trailerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-6 py-3 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 transition-colors"
                >
                  <svg
                    className="w-5 h-5"
                    fill="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path d="M8 5v14l11-7z" />
                  </svg>
                  Watch Trailer
                </a>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Showtimes */}
      <div className="container-max px-4 md:px-8 py-12">
        <h2 className="text-3xl font-bold mb-8">Select Showtime</h2>

        {showtimeLoading ? (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Loading showtimes...</p>
          </div>
        ) : allShowtimes.length > 0 ? (
          <>
            {/* Cinema & Date Filter - Same Row */}
            <div className="mb-8 grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Cinema Filter */}
              {availableCinemas.length > 0 && (
                <div>
                  <label className="block text-sm font-semibold mb-3 flex items-center gap-2">
                    <MapPin size={18} className="text-purple-600" />
                    Select Cinema
                  </label>
                  <Select
                    value={selectedCinema}
                    onValueChange={(value) => {
                      setSelectedCinema(value);
                      setSelectedShowtime(null);
                    }}
                  >
                    <div className="w-full h-12 rounded-lg border border-border bg-card px-4 flex items-center">
                      <SelectTrigger
                        className="
                          w-full border-0 p-0
                          text-base font-semibold leading-normal
                          focus:outline-none
                          focus:ring-0
                          focus:ring-offset-0
                          shadow-none
                        "
                      >
                        <SelectValue className="text-base font-semibold" />
                      </SelectTrigger>
                    </div>

                    <SelectContent className="bg-card dark:bg-slate-900 border-border dark:border-slate-800">
                      {/* All Cinemas option */}
                      <SelectItem
                        value="all"
                        className="cursor-pointer hover:bg-purple-500/10 py-3"
                      >
                        <div className="flex flex-col">
                          <span className="font-semibold text-base">
                            All Cinemas
                          </span>
                        </div>
                      </SelectItem>
                      {availableCinemas.map((cinema) => (
                        <SelectItem
                          key={cinema.id}
                          value={cinema.id}
                          className="cursor-pointer hover:bg-purple-500/10 py-3"
                        >
                          <div className="flex flex-col">
                            <span className="font-semibold text-base">
                              {cinema.name}
                            </span>
                            {cinema.location && (
                              <span className="text-sm text-muted-foreground">
                                {cinema.location}
                              </span>
                            )}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}

              {/* Date Filter (Calendar with disabled dates) */}
              {availableDates.length > 0 && (
                <div>
                  <label className="block text-sm font-semibold mb-3 flex items-center gap-2">
                    <CalendarIcon size={18} className="text-purple-600" />
                    Select Date
                  </label>
                  <Popover>
                    <PopoverTrigger asChild>
                      <div className="w-full h-12 rounded-lg border border-border bg-card px-4 flex items-center cursor-pointer hover:border-purple-400 transition-colors">
                        <Button
                          variant="ghost"
                          className={cn(
                            "w-full h-full justify-start text-left p-0 font-semibold text-base hover:bg-transparent",
                            !selectedDate && "text-muted-foreground",
                          )}
                        >
                          {selectedDate ? (
                            (() => {
                              const [year, month, day] = selectedDate
                                .split("-")
                                .map(Number);
                              const dateObj = new Date(year, month - 1, day);
                              return dateObj.toLocaleDateString("en-US", {
                                weekday: "short",
                                year: "numeric",
                                month: "short",
                                day: "numeric",
                              });
                            })()
                          ) : (
                            <span>Pick a date</span>
                          )}
                        </Button>
                      </div>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={
                          selectedDate
                            ? (() => {
                                const [year, month, day] = selectedDate
                                  .split("-")
                                  .map(Number);
                                return new Date(year, month - 1, day);
                              })()
                            : undefined
                        }
                        onSelect={(date) => {
                          if (date) {
                            const year = date.getFullYear();
                            const month = String(date.getMonth() + 1).padStart(
                              2,
                              "0",
                            );
                            const day = String(date.getDate()).padStart(2, "0");
                            const dateStr = `${year}-${month}-${day}`;
                            setSelectedDate(dateStr);
                            setSelectedShowtime(null);
                          }
                        }}
                        disabled={(date) => {
                          const year = date.getFullYear();
                          const month = String(date.getMonth() + 1).padStart(
                            2,
                            "0",
                          );
                          const day = String(date.getDate()).padStart(2, "0");
                          const dateStr = `${year}-${month}-${day}`;
                          return !availableDates.includes(dateStr);
                        }}
                        initialFocus
                      />
                    </PopoverContent>
                  </Popover>
                </div>
              )}
            </div>

            {/* Showtimes */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {filteredShowtimes.map((showtime) => (
                <button
                  key={showtime.id}
                  onClick={() => setSelectedShowtime(showtime)}
                  className={`p-6 rounded-xl border-2 transition-all ${
                    selectedShowtime?.id === showtime.id
                      ? "border-purple-500 bg-gradient-to-br from-purple-500/20 to-purple-600/20 dark:from-purple-600/20 dark:to-purple-700/20 shadow-lg shadow-purple-500/20"
                      : "border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800/50 hover:border-purple-400 hover:shadow-md hover:shadow-purple-500/10"
                  }`}
                >
                  <p className="text-2xl font-bold mb-3 text-left bg-gradient-to-r from-purple-600 to-purple-400 bg-clip-text text-transparent">
                    {showtime.time}
                  </p>
                  <div className="text-sm text-slate-600 dark:text-slate-400 space-y-1 mb-4 text-left">
                    <p className="flex items-center gap-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-purple-500"></span>
                      Room name:{" "}
                      {showtime.roomName ? `${showtime.roomName}` : "N/A"}
                    </p>
                    <p className="flex items-center gap-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-purple-400"></span>
                      Cinema name:{" "}
                      {showtime.cinemaName ? `${showtime.cinemaName}` : "N/A"}
                    </p>
                  </div>
                  
                </button>
              ))}
            </div>

            {filteredShowtimes.length === 0 && (
              <div className="text-center py-12 bg-slate-800/50 rounded-xl">
                <p className="text-muted-foreground text-lg">
                  No showtimes available for selected cinema and date
                </p>
              </div>
            )}

            {selectedShowtime && (
              <div className="mt-8 flex justify-center">
                <Link
                  href={`/booking/${movie.id}/${selectedShowtime.id}`}
                  className="px-8 py-4 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
                >
                  Continue to Booking
                </Link>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-12 bg-slate-800/50 rounded-xl">
            <p className="text-muted-foreground text-lg">
              Showtimes for this movie will be available soon
            </p>
          </div>
        )}
      </div>

      {/* Reviews & Ratings Section */}
      <div className="container-max px-4 md:px-8 py-12 border-t border-border dark:border-slate-800">
        <ReviewsSection movieId={movie.id} movieStatus={movie.status} />
      </div>
    </div>
  );
}
