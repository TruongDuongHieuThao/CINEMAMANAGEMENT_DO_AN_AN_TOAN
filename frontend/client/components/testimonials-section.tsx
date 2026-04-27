"use client"

import { Star } from "lucide-react"
import { testimonials } from "@/lib/mock-data"

export function TestimonialsSection() {
  return (
    <section className="section-padding bg-slate-950 relative overflow-hidden">
      {/* Background elements */}
      <div className="absolute bottom-0 right-0 w-96 h-96 bg-pink-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4 text-center">
          <div className="inline-block px-4 py-2 rounded-full bg-yellow-500/20 border border-yellow-500/50 mx-auto">
            <span className="text-yellow-300 text-sm font-semibold">
              Cinema Chain Reviews
            </span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">How Guests Rate Cifastar</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Real feedback on sound quality, seating comfort, cleanliness, and service across our
            theaters.
          </p>
        </div>

        {/* Testimonials Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((testimonial) => (
            <div
              key={testimonial.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-8 hover:border-purple-500/50 transition-all duration-300 hover:shadow-lg hover:shadow-purple-500/10"
            >
              {/* Stars */}
              <div className="flex gap-1 mb-4">
                {Array.from({ length: testimonial.rating }).map((_, i) => (
                  <Star key={i} size={18} className="fill-yellow-400 text-yellow-400" />
                ))}
              </div>

              {/* Quote */}
              <p className="text-slate-300 mb-6 leading-relaxed italic">"{testimonial.content}"</p>

              {/* Author */}
              <div className="flex items-center gap-4 pt-6 border-t border-slate-800">
                <img
                  src={testimonial.avatar || "/placeholder.svg"}
                  alt={testimonial.name}
                  className="w-12 h-12 rounded-full object-cover"
                />
                <div>
                  <p className="font-semibold text-white">{testimonial.name}</p>
                  <p className="text-sm text-slate-400">{testimonial.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>

      </div>
    </section>
  )
}
