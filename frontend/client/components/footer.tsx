"use client"

import Link from "next/link"
import { Facebook, Twitter, Instagram, Linkedin } from "lucide-react"

export function Footer() {
  const currentYear = new Date().getFullYear()

  const footerLinks = {
    Company: [
      { label: "About Us", href: "#" },
      { label: "Careers", href: "#" },
      { label: "Press", href: "#" },
      { label: "Blog", href: "#" },
    ],
    Support: [
      { label: "Help Center", href: "#" },
      { label: "Contact Us", href: "#contact" },
      { label: "FAQ", href: "#faq" },
      { label: "Feedback", href: "#" },
    ],
    Legal: [
      { label: "Privacy Policy", href: "#" },
      { label: "Terms of Service", href: "#" },
      { label: "Cookie Policy", href: "#" },
      { label: "Accessibility", href: "#" },
    ],
    Social: [
      { label: "Facebook", href: "#", icon: Facebook },
      { label: "Twitter", href: "#", icon: Twitter },
      { label: "Instagram", href: "#", icon: Instagram },
      { label: "LinkedIn", href: "#", icon: Linkedin },
    ],
  }

  return (
    <footer className="bg-background border-t border-border relative overflow-hidden">
      {/* Background elements */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-purple-600/5 rounded-full blur-3xl" />

      <div className="container-max px-4 md:px-8 py-16 relative z-10">
        {/* Main Footer Content */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 mb-12">
          {/* Brand Section */}
          <div className="lg:col-span-1">
            <Link href="/" className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 gradient-primary rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">C</span>
              </div>
              <span className="gradient-text font-bold text-lg">CINEPLEX</span>
            </Link>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Experience premium cinema entertainment with cutting-edge technology and unforgettable moments.
            </p>
          </div>

          {/* Footer Links */}
          {Object.entries(footerLinks)
            .slice(0, 3)
            .map(([category, links]) => (
              <div key={category}>
                <h4 className="font-semibold text-foreground mb-4">{category}</h4>
                <ul className="space-y-2">
                  {links.map((link) => (
                    <li key={link.label}>
                      <Link
                        href={link.href}
                        className="text-muted-foreground hover:text-foreground transition-colors text-sm"
                      >
                        {link.label}
                      </Link>
                    </li>
                  ))}
                </ul>
              </div>
            ))}

          {/* Social Links */}
          <div>
            <h4 className="font-semibold text-foreground mb-4">Follow Us</h4>
            <div className="flex gap-4">
              {footerLinks.Social.map((link) => {
                const Icon = link.icon
                return (
                  <a
                    key={link.label}
                    href={link.href}
                    className="w-10 h-10 rounded-lg bg-muted hover:bg-purple-600 flex items-center justify-center text-muted-foreground hover:text-white transition-all"
                    aria-label={link.label}
                  >
                    <Icon size={20} />
                  </a>
                )
              })}
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="border-t border-border my-8" />

        {/* Bottom Footer */}
        <div className="flex flex-col md:flex-row items-center justify-between gap-4">
          <p className="text-muted-foreground text-sm">
            &copy; {currentYear} CINEPLEX. All rights reserved. Crafted with passion for cinema lovers.
          </p>

          {/* Newsletter Signup */}
          <div className="flex gap-2">
            <input
              type="email"
              placeholder="Enter your email"
              className="px-4 py-2 rounded-lg bg-muted border border-border text-foreground placeholder-muted-foreground text-sm focus:outline-none focus:border-purple-500 transition-colors"
            />
            <button className="px-4 py-2 rounded-lg gradient-primary text-white font-semibold text-sm hover:shadow-lg transition-all">
              Subscribe
            </button>
          </div>
        </div>
      </div>
    </footer>
  )
}
