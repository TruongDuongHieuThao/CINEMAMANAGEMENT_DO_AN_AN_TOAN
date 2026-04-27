"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { Menu, X, Moon, Sun, User } from "lucide-react";
import { useTheme } from "next-themes";
import { AuthModal } from "./auth-modal";
import { clearAuthData } from "@/services/localStorageService";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore, useAuthModalStore } from "@/store";

function LogoIcon() {
  return (
    <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-purple-500 via-pink-500 to-indigo-500 flex items-center justify-center">
      <span className="text-white font-bold text-lg">C</span>
    </div>
  );
}

export function Header() {
  const [isOpen, setIsOpen] = useState(false);
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const router = useRouter();
  const pathname = usePathname();
  const userMenuRef = useRef<HTMLDivElement>(null);

  // Zustand stores
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);
  const showAuthModal = useAuthModalStore((state) => state.showAuthModal);
  const setShowAuthModal = useAuthModalStore((state) => state.setShowAuthModal);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (mounted) {
      console.log("[v0] Theme changed to:", resolvedTheme);
    }
  }, [resolvedTheme, mounted]);

  // Handle scroll to section when navigating with hash
  useEffect(() => {
    if (pathname === "/" && window.location.hash) {
      // Wait for the page to render
      setTimeout(() => {
        const hash = window.location.hash;
        const element = document.querySelector(hash);
        if (element) {
          element.scrollIntoView({ behavior: "smooth" });
        }
      }, 100);
    }
  }, [pathname]);

  // Handle click outside to close user menu
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target as Node)
      ) {
        setShowUserMenu(false);
      }
    };

    if (showUserMenu) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showUserMenu]);

  const handleLogout = () => {
    clearAuthData();
    logout();
    setShowUserMenu(false);
    router.push("/");
  };

  const navItems = [
    { label: "Now Showing", href: "#now-showing" },
    { label: "Coming Soon", href: "#coming-soon" },
    { label: "Facilities", href: "#facilities" },
  ];

  const handleNavClick = (
    e: React.MouseEvent<HTMLAnchorElement>,
    href: string,
  ) => {
    e.preventDefault();

    // If not on home page, navigate to home first with the hash
    if (pathname !== "/") {
      router.push(`/${href}`);
    } else {
      // Already on home page, just scroll to section
      const element = document.querySelector(href);
      if (element) {
        element.scrollIntoView({ behavior: "smooth" });
      }
    }

    // Close mobile menu if open
    setIsOpen(false);
  };

  const handleThemeToggle = () => {
    const newTheme = resolvedTheme === "dark" ? "light" : "dark";
    console.log("[v0] Toggling theme to:", newTheme);
    setTheme(newTheme);
  };

  return (
    <>
      <header className="fixed top-0 w-full z-50 bg-background/80 backdrop-blur-md border-b border-border">
        <div className="container-max flex items-center justify-between h-20 px-4 md:px-8">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2">
            <LogoIcon />
            <span className="gradient-text font-bold text-2xl hidden sm:inline">
              Cifastar
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-8">
            {navItems.map((item) => (
              <a
                key={item.href}
                href={item.href}
                onClick={(e) => handleNavClick(e, item.href)}
                className="text-foreground/80 hover:text-foreground transition-colors text-base font-semibold cursor-pointer"
              >
                {item.label}
              </a>
            ))}
          </nav>

          {/* Right Actions */}
          <div className="hidden md:flex items-center gap-4">
            {mounted && (
              <button
                onClick={handleThemeToggle}
                className="p-2 rounded-lg hover:bg-muted transition-colors"
                aria-label="Toggle theme"
                title={`Switch to ${
                  resolvedTheme === "dark" ? "light" : "dark"
                } mode`}
              >
                {resolvedTheme === "dark" ? (
                  <Sun size={20} />
                ) : (
                  <Moon size={20} />
                )}
              </button>
            )}

            {isAuthenticated ? (
              <div className="relative" ref={userMenuRef}>
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-lg border border-border hover:bg-muted transition-colors text-base font-semibold"
                >
                  <User size={18} />
                  <span>Profile</span>
                </button>

                {showUserMenu && (
                  <div className="absolute right-0 mt-2 w-48 bg-card border border-border rounded-lg shadow-lg py-2 z-50">
                    <Link
                      href="/profile"
                      className="block px-4 py-2 text-base hover:bg-muted transition-colors"
                      onClick={() => setShowUserMenu(false)}
                    >
                      My Profile
                    </Link>
                    <Link
                      href="/my-tickets"
                      className="block px-4 py-2 text-base hover:bg-muted transition-colors"
                      onClick={() => setShowUserMenu(false)}
                    >
                      My Tickets
                    </Link>
                    <hr className="my-2 border-border" />
                    <button
                      onClick={handleLogout}
                      className="w-full text-left px-4 py-2 text-base text-red-600 hover:bg-muted transition-colors"
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <>
                <button
                  onClick={() => setShowAuthModal("login")}
                  className="px-5 py-2.5 rounded-lg border border-border hover:bg-muted transition-colors text-base font-semibold"
                >
                  Login
                </button>
                <button
                  onClick={() => setShowAuthModal("register")}
                  className="px-6 py-2.5 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg hover:shadow-purple-500/50 transition-all text-base"
                >
                  Sign Up
                </button>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden text-foreground hover:text-muted-foreground"
            onClick={() => setIsOpen(!isOpen)}
            aria-label="Toggle menu"
          >
            {isOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {/* Mobile Navigation */}
        {isOpen && (
          <div className="md:hidden bg-card border-b border-border">
            <nav className="flex flex-col p-4 gap-4">
              {navItems.map((item) => (
                <a
                  key={item.href}
                  href={item.href}
                  onClick={(e) => handleNavClick(e, item.href)}
                  className="text-muted-foreground hover:text-foreground transition-colors font-medium cursor-pointer"
                >
                  {item.label}
                </a>
              ))}
              <div className="flex gap-2 pt-4 border-t border-border">
                {mounted && (
                  <button
                    onClick={handleThemeToggle}
                    className="flex-1 px-4 py-2.5 rounded-lg border border-border hover:bg-muted transition-colors text-base font-semibold flex items-center justify-center gap-2"
                    aria-label="Toggle theme"
                  >
                    {resolvedTheme === "dark" ? (
                      <Sun size={18} />
                    ) : (
                      <Moon size={18} />
                    )}
                    {resolvedTheme === "dark" ? "Light" : "Dark"}
                  </button>
                )}
                <button
                  onClick={() => setShowAuthModal("login")}
                  className="flex-1 px-4 py-2.5 rounded-lg border border-border hover:bg-muted transition-colors text-base font-semibold"
                >
                  Login
                </button>
                <button
                  onClick={() => setShowAuthModal("register")}
                  className="flex-1 px-4 py-2.5 rounded-lg gradient-primary text-white font-semibold text-base"
                >
                  Sign Up
                </button>
              </div>
            </nav>
          </div>
        )}
      </header>

      {showAuthModal && (
        <AuthModal
          mode={showAuthModal}
          onClose={() => setShowAuthModal(null)}
          onSwitchMode={() =>
            setShowAuthModal(showAuthModal === "login" ? "register" : "login")
          }
        />
      )}
    </>
  );
}
