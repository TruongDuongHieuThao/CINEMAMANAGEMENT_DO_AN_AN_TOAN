"use client";

import { useState } from "react";
import Link from "next/link";
import { ChevronLeft, Clock, Users } from "lucide-react";
import type { Movie, Showtime } from "@/lib/types";

interface MovieDetailClientProps {
  movie: Movie;
  movieShowtimes: Showtime[];
  movieId: string;
}

export default function MovieDetailClient({ movie, movieShowtimes, movieId }: MovieDetailClientProps) {
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null);

  if (!movie) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4">Movie Not Found</h1>
          <Link href="/" className="text-purple-600 hover:text-purple-700 font-semibold">
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
        {/* Poster background */}
        <img
          src={movie.poster || "/placeholder.svg"}
          alt={movie.title}
          className="w-full h-full object-cover opacity-40"
        />

        {/* Gradient overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-background dark:from-slate-950 via-transparent" />

        {/* Back to Home Button — overlay trên poster */}
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
            />
          </div>

          {/* Details */}
          <div className="md:col-span-2 space-y-6">
            <div>
              <h1 className="text-4xl md:text-5xl font-bold mb-4">{movie.title}</h1>
              <div className="flex flex-wrap gap-2 mb-4">
                {movie.genre.map((g) => (
                  <span
                    key={g}
                    className="px-3 py-1 rounded-full bg-purple-500/20 text-purple-600 dark:text-purple-300 text-sm font-semibold"
                  >
                    {g}
                  </span>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Rating</p>
                <p className="text-2xl font-bold">{movie.rating}</p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Duration</p>
                <p className="text-2xl font-bold flex items-center gap-2">
                  <Clock size={20} />
                  {movie.duration}m
                </p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Release</p>
                <p className="text-lg font-bold">{new Date(movie.releaseDate).toLocaleDateString()}</p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Director</p>
                <p className="text-lg font-bold">{movie.director}</p>
              </div>
            </div>

            <div>
              <h3 className="text-lg font-bold mb-2">Cast</h3>
              <p className="text-muted-foreground">{movie.cast.join(", ")}</p>
            </div>

            <div>
              <h3 className="text-lg font-bold mb-2">Synopsis</h3>
              <p className="text-muted-foreground leading-relaxed">{movie.description}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Showtimes */}
      <div className="container-max px-4 md:px-8 py-12">
        <h2 className="text-3xl font-bold mb-8">Select Showtime</h2>

        {movieShowtimes.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {movieShowtimes.map((showtime) => (
              <button
                key={showtime.id}
                onClick={() => setSelectedShowtime(showtime)}
                className={`p-6 rounded-xl border-2 transition-all ${selectedShowtime?.id === showtime.id
                    ? "border-purple-600 bg-purple-500/10 dark:bg-purple-900/20"
                    : "border-border dark:border-slate-800 bg-card dark:bg-slate-900 hover:border-purple-600"
                  }`}
              >
                <p className="text-2xl font-bold mb-2">{showtime.time}</p>
                <p className="text-sm text-muted-foreground mb-3">{showtime.format}</p>
                <p className="text-lg font-semibold text-purple-600 mb-3">
                  {showtime.price.toLocaleString()} VND
                </p>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Users size={16} />
                  {showtime.availableSeats} seats available
                </div>
              </button>
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-muted-foreground text-lg">No showtimes available for this movie</p>
          </div>
        )}

        {selectedShowtime && (
          <div className="mt-8 flex justify-center">
            <Link
              href={`/booking/${movieId}/${selectedShowtime.id}`}
              className="px-8 py-4 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
            >
              Continue to Booking
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
