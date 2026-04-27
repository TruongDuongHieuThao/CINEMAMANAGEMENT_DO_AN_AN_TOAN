"use client"

import { useState } from "react"
import { ChevronDown } from "lucide-react"
import { faqs } from "@/lib/mock-data"

export function FAQSection() {
  const [openId, setOpenId] = useState<string | null>(null)

  return (
    <section id="faq" className="section-padding bg-gradient-to-b from-slate-950 to-slate-900 relative overflow-hidden">
      {/* Background elements */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4 text-center">
          <div className="inline-block px-4 py-2 rounded-full bg-green-500/20 border border-green-500/50 mx-auto">
            <span className="text-green-300 text-sm font-semibold">Help & Support</span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">Frequently Asked Questions</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Find answers to common questions about booking, payments, and Cifastar services.
          </p>
        </div>

        {/* FAQ Items */}
        <div className="max-w-3xl mx-auto space-y-4">
          {faqs.map((faq) => (
            <div
              key={faq.id}
              className="bg-slate-900 border border-slate-800 rounded-lg overflow-hidden hover:border-purple-500/30 transition-all duration-300"
            >
              <button
                onClick={() => setOpenId(openId === faq.id ? null : faq.id)}
                className="w-full px-6 py-4 flex items-center justify-between hover:bg-slate-800/50 transition-colors"
              >
                <span className="text-lg font-semibold text-white text-left">{faq.question}</span>
                <ChevronDown
                  size={24}
                  className={`text-purple-400 flex-shrink-0 transition-transform duration-300 ${
                    openId === faq.id ? "rotate-180" : ""
                  }`}
                />
              </button>

              {/* Answer */}
              {openId === faq.id && (
                <div className="px-6 py-4 bg-slate-800/30 border-t border-slate-800">
                  <p className="text-slate-300 leading-relaxed">{faq.answer}</p>
                </div>
              )}
            </div>
          ))}
        </div>

        {/* Contact Support */}
      </div>
    </section>
  )
}
