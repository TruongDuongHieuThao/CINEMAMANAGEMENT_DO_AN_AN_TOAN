"use client"

import { useState } from "react"
import { Mail, Phone, MapPin, Send } from "lucide-react"
import type React from "react"

export function ContactSection() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  })
  const [submitted, setSubmitted] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitted(true)
    setTimeout(() => {
      setSubmitted(false)
      setFormData({ name: "", email: "", subject: "", message: "" })
    }, 3000)
  }

  return (
    <section id="contact" className="section-padding bg-slate-950 relative overflow-hidden">
      {/* Background elements */}
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-pink-600/5 rounded-full blur-3xl" />
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4 text-center">
          <div className="inline-block px-4 py-2 rounded-full bg-red-500/20 border border-red-500/50 mx-auto">
            <span className="text-red-300 text-sm font-semibold">Get In Touch</span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">Contact Us</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl mx-auto">
            Have questions? We'd love to hear from you. Send us a message and we'll respond as soon as possible.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-12">
          {/* Contact Info Cards */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 hover:border-purple-500/50 transition-all">
            <div className="w-12 h-12 rounded-lg bg-purple-500/20 flex items-center justify-center mb-4">
              <Phone className="text-purple-400" size={24} />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">Phone</h3>
            <p className="text-slate-400">+1 (555) 123-4567</p>
            <p className="text-slate-500 text-sm mt-2">Mon-Fri, 9AM-6PM EST</p>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 hover:border-purple-500/50 transition-all">
            <div className="w-12 h-12 rounded-lg bg-pink-500/20 flex items-center justify-center mb-4">
              <Mail className="text-pink-400" size={24} />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">Email</h3>
            <p className="text-slate-400">support@cineplex.com</p>
            <p className="text-slate-500 text-sm mt-2">We'll reply within 24 hours</p>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 hover:border-purple-500/50 transition-all">
            <div className="w-12 h-12 rounded-lg bg-yellow-500/20 flex items-center justify-center mb-4">
              <MapPin className="text-yellow-400" size={24} />
            </div>
            <h3 className="text-lg font-bold text-white mb-2">Location</h3>
            <p className="text-slate-400">123 Cinema Street</p>
            <p className="text-slate-500 text-sm mt-2">New York, NY 10001</p>
          </div>
        </div>

        {/* Contact Form */}
        <div className="max-w-2xl mx-auto bg-gradient-to-br from-slate-900 to-slate-800 border border-slate-800 rounded-2xl p-8 md:p-12">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-semibold text-white mb-2">Name</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Your name"
                  className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-white mb-2">Email</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="your@email.com"
                  className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-white mb-2">Subject</label>
              <input
                type="text"
                name="subject"
                value={formData.subject}
                onChange={handleChange}
                placeholder="How can we help?"
                className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-white mb-2">Message</label>
              <textarea
                name="message"
                value={formData.message}
                onChange={handleChange}
                placeholder="Your message..."
                rows={5}
                className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors resize-none"
                required
              />
            </div>

            <button
              type="submit"
              className="w-full px-6 py-4 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg hover:shadow-purple-500/50 transition-all flex items-center justify-center gap-2 group"
            >
              <Send size={20} />
              {submitted ? "Message Sent!" : "Send Message"}
            </button>
          </form>
        </div>
      </div>
    </section>
  )
}
