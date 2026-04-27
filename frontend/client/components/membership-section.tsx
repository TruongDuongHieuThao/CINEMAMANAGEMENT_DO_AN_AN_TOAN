"use client";

import { Check, Zap } from "lucide-react";
import { membershipTiers } from "@/lib/mock-data";

export function MembershipSection() {
  return (
    <section
      id="membership"
      className="section-padding bg-slate-950 relative overflow-hidden"
    >
      {/* Background elements */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-pink-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4 text-center">
          <div className="inline-block px-4 py-2 rounded-full bg-yellow-500/20 border border-yellow-500/50">
            <span className="text-yellow-300 text-sm font-semibold">
              Exclusive Benefits
            </span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">Membership Tiers</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Choose the perfect membership plan and unlock exclusive perks,
            discounts, and VIP experiences.
          </p>
        </div>

        {/* Membership Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          {membershipTiers.map((tier) => (
            <div
              key={tier.id}
              className={`relative rounded-2xl p-8 transition-all duration-300 ${
                tier.featured
                  ? "bg-gradient-to-br from-purple-900/40 to-pink-900/40 border-2 border-purple-500/50 shadow-2xl shadow-purple-500/20 scale-105"
                  : "bg-slate-900 border border-slate-800 hover:border-purple-500/30"
              }`}
            >
              {/* Featured Badge */}
              {tier.featured && (
                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                  <div className="px-4 py-1 rounded-full bg-gradient-to-r from-purple-600 to-pink-600 text-white text-sm font-bold flex items-center gap-1">
                    <Zap size={16} />
                    Most Popular
                  </div>
                </div>
              )}

              {/* Tier Name */}
              <h3 className="text-2xl font-bold text-white mb-2">
                {tier.name}
              </h3>

              {/* Price */}
              <div className="mb-6">
                <span className="text-4xl font-bold gradient-text">
                  ${tier.price}
                </span>
                <span className="text-slate-400 ml-2">/month</span>
              </div>

              {/* CTA Button */}
              <button
                className={`w-full py-3 rounded-lg font-semibold mb-8 transition-all ${
                  tier.featured
                    ? "gradient-primary text-white hover:shadow-lg hover:shadow-purple-500/50"
                    : "bg-slate-800 text-white hover:bg-slate-700"
                }`}
              >
                Choose Plan
              </button>

              {/* Benefits List */}
              <div className="space-y-4">
                {tier.benefits.map((benefit, idx) => (
                  <div key={idx} className="flex items-start gap-3">
                    <Check
                      size={20}
                      className="text-green-400 flex-shrink-0 mt-0.5"
                    />
                    <span className="text-slate-300">{benefit}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Additional Info */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center">
          <p className="text-slate-300 mb-4">
            All memberships include access to our mobile app, priority customer
            support, and exclusive member-only events.
          </p>
          <p className="text-slate-400 text-sm">
            Cancel anytime. No hidden fees. Your membership renews automatically
            each month.
          </p>
        </div>
      </div>
    </section>
  );
}
