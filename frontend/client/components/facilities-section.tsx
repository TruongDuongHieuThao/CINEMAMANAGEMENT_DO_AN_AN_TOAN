"use client"

import { facilities } from "@/lib/mock-data"

export function FacilitiesSection() {
  return (
    <section
      id="facilities"
      className="section-padding bg-gradient-to-b from-slate-900 to-slate-950 relative overflow-hidden"
    >
      {/* Background elements */}
      <div className="absolute top-0 left-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4 text-center">
          <div className="inline-block px-4 py-2 rounded-full bg-blue-500/20 border border-blue-500/50 mx-auto">
            <span className="text-blue-300 text-sm font-semibold">Premium Experience</span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">World-Class Facilities</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Experience cinema like never before with our state-of-the-art facilities and premium amenities.
          </p>
        </div>

        {/* Facilities Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {facilities.map((facility) => (
            <div
              key={facility.id}
              className="group relative bg-slate-900 border border-slate-800 rounded-xl p-8 hover:border-purple-500/50 transition-all duration-300 hover:shadow-lg hover:shadow-purple-500/10"
            >
              {/* Icon */}
              <div className="text-5xl mb-4 group-hover:scale-110 transition-transform duration-300">
                {facility.icon}
              </div>

              {/* Content */}
              <h3 className="text-xl font-bold text-white mb-2">{facility.name}</h3>
              <p className="text-slate-400">{facility.description}</p>

              {/* Accent line */}
              <div className="absolute bottom-0 left-0 h-1 bg-gradient-to-r from-purple-600 to-pink-600 w-0 group-hover:w-full transition-all duration-300 rounded-full" />
            </div>
          ))}
        </div>

        {/* Additional Info */}
        <div className="mt-16 bg-gradient-to-r from-purple-900/20 to-pink-900/20 border border-purple-500/30 rounded-2xl p-8 md:p-12">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-center">
            <div>
              <p className="text-3xl font-bold gradient-text mb-2">15+</p>
              <p className="text-slate-300">Premium Screens</p>
            </div>
            <div>
              <p className="text-3xl font-bold gradient-text mb-2">2000+</p>
              <p className="text-slate-300">Comfortable Seats</p>
            </div>
            <div>
              <p className="text-3xl font-bold gradient-text mb-2">4K</p>
              <p className="text-slate-300">Resolution Standard</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
