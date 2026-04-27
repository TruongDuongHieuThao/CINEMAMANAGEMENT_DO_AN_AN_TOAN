"use client";

import { useState, useEffect } from "react";
import { MovieCard } from "./movie-card";
import { getNowShowingMovies, mapMovieForDisplay } from "@/lib/api-movie";

export function NowShowingSection() {
  const [movies, setMovies] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedGenre, setSelectedGenre] = useState<string | null>(null);

  // Fetch movies khi component mount
  useEffect(() => {
    const fetchMovies = async () => {
      try {
        setLoading(true);
        const data = await getNowShowingMovies();

        // ✅ Kiểm tra data trước khi map
        if (data && Array.isArray(data)) {
          const mappedMovies = data.map(mapMovieForDisplay);
          setMovies(mappedMovies);
        } else {
          console.error("API response is not an array:", data);
          setMovies([]);
        }
      } catch (error) {
        console.error("Error fetching movies:", error);
        setMovies([]);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, []);

  // Lấy danh sách genres từ movies
  const genres = Array.from(new Set(movies.flatMap((m) => m.genre)));

  // Filter movies theo genre
  const filteredMovies = selectedGenre
    ? movies.filter((m) => m.genre.includes(selectedGenre))
    : movies;

  return (
    <section
      id="now-showing"
      className="section-padding bg-background dark:bg-slate-950 relative overflow-hidden"
    >
      {/* Background elements */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4">
          <div className="inline-block px-4 py-2 rounded-full bg-purple-500/20 border border-purple-500/50">
            <span className="text-purple-300 text-sm font-semibold">
              Featured Content
            </span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">Now Showing</span>
          </h2>

          <p className="text-xl text-muted-foreground max-w-2xl">
            Discover the latest blockbusters and critically acclaimed films
            playing at Cifastar now.
          </p>
        </div>

        {/* Loading state */}
        {loading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-12">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="bg-gray-700 aspect-[2/3] rounded-lg mb-3"></div>
                <div className="h-4 bg-gray-700 rounded mb-2"></div>
                <div className="h-3 bg-gray-700 rounded w-2/3"></div>
              </div>
            ))}
          </div>
        )}

        {/* Movies loaded */}
        {!loading && movies.length > 0 && (
          <>
            {/* Genre Filter */}
            <div className="mb-12 flex flex-wrap gap-3">
              <button
                onClick={() => setSelectedGenre(null)}
                className={`px-4 py-2 rounded-lg font-semibold transition-all ${
                  selectedGenre === null
                    ? "gradient-primary text-white"
                    : "bg-muted dark:bg-slate-800 text-muted-foreground hover:bg-muted/80 dark:hover:bg-slate-700"
                }`}
              >
                All
              </button>
              {genres.map((genre) => (
                <button
                  key={genre}
                  onClick={() => setSelectedGenre(genre)}
                  className={`px-4 py-2 rounded-lg font-semibold transition-all ${
                    selectedGenre === genre
                      ? "gradient-primary text-white"
                      : "bg-muted dark:bg-slate-800 text-muted-foreground hover:bg-muted/80 dark:hover:bg-slate-700"
                  }`}
                >
                  {genre}
                </button>
              ))}
            </div>

            {/* Movies Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-12">
              {filteredMovies.map((movie) => (
                <MovieCard key={movie.id} movie={movie} />
              ))}
            </div>
          </>
        )}

        {/* Empty state */}
        {!loading && movies.length === 0 && (
          <div className="text-center py-12">
            <p className="text-muted-foreground text-xl">
              No movies currently showing
            </p>
          </div>
        )}

      </div>
    </section>
  );
}
