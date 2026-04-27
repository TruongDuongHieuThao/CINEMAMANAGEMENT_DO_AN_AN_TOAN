"use client";

import { X } from "lucide-react";
import { useState } from "react";
import { login, register } from "@/services/authService";
import { getRedirectPath, clearRedirectPath } from "@/lib/auth-utils";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store";
import { getErrorMessage, isApiError } from "@/lib/errors";

interface AuthModalProps {
  mode: "login" | "register";
  onClose: () => void;
  onSwitchMode: () => void;
}

export function AuthModal({ mode, onClose, onSwitchMode }: AuthModalProps) {
  const isLogin = mode === "login";
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  // Zustand store
  const authLogin = useAuthStore((state) => state.login);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    const formData = new FormData(e.currentTarget);
    const password = formData.get("password") as string;

    try {
      if (isLogin) {
        const loginIdentifier = formData.get("loginIdentifier") as string;
        const response = await login({ loginIdentifier, password });
        console.log("Login successful:", response);

        // Update auth context
        authLogin();

        // Get redirect path or default to home
        const redirectPath = getRedirectPath();
        clearRedirectPath();

        // Close modal first
        onClose();

        // Redirect to intended page
        if (redirectPath && redirectPath !== "/") {
          router.push(redirectPath);
        } else {
          router.push("/");
        }
      } else {
        // Validate password confirmation
        const confirmPassword = formData.get("confirmPassword") as string;
        if (password !== confirmPassword) {
          setError("Passwords do not match");
          setIsLoading(false);
          return;
        }

        const username = formData.get("username") as string;
        const email = formData.get("email") as string;
        const phoneNumber = formData.get("phoneNumber") as string;
        const firstName = formData.get("firstName") as string;
        const lastName = formData.get("lastName") as string;
        const dob = formData.get("dob") as string;
        const gender = formData.get("gender") as string;
        const address = formData.get("address") as string;

        const response = await register({
          username,
          password,
          email,
          phoneNumber,
          firstName,
          lastName,
          dob,
          gender,
          address,
        });
        console.log("Registration successful:", response);

        // Update auth context
        authLogin();

        // Close modal
        onClose();

        // Redirect to home
        router.push("/");
      }
    } catch (err: any) {
      // Backend always returns structured response with code
      // If code !== 1000, it's an error with a message
      const errorCode = err?.response?.data?.code || err?.code;
      const backendMessage = err?.response?.data?.message || err?.message;

      // Default fallback message
      let errorMessage = isLogin
        ? "Login failed. Please try again."
        : "Registration failed. Please try again.";

      // Priority: Use backend message if available (code !== 1000)
      if (backendMessage) {
        errorMessage = backendMessage;
      }

      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
    // Only close if clicking the backdrop itself, not its children
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      onClick={handleBackdropClick}
    >
      <div
        className={`bg-card border border-border rounded-2xl p-8 w-full relative max-h-[90vh] overflow-y-auto ${
          isLogin ? "max-w-md" : "max-w-2xl"
        }`}
      >
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-muted-foreground hover:text-foreground"
          aria-label="Close modal"
        >
          <X size={24} />
        </button>

        <h2 className="text-2xl font-bold mb-6">
          {isLogin ? "Login" : "Create Account"}
        </h2>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-red-600 text-sm">
            {error}
          </div>
        )}

        {/* Form */}
        <form className="space-y-4" onSubmit={handleSubmit}>
          {!isLogin && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    First Name
                  </label>
                  <input
                    type="text"
                    name="firstName"
                    placeholder="First name"
                    className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Last Name
                  </label>
                  <input
                    type="text"
                    name="lastName"
                    placeholder="Last name"
                    className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Username
                </label>
                <input
                  type="text"
                  name="username"
                  placeholder="Choose a username"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Email</label>
                <input
                  type="email"
                  name="email"
                  placeholder="your@email.com"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Phone Number
                  </label>
                  <input
                    type="tel"
                    name="phoneNumber"
                    placeholder="Your phone number"
                    className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Date of Birth
                  </label>
                  <input
                    type="date"
                    name="dob"
                    className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Gender</label>
                <select
                  name="gender"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                >
                  <option value="">Select gender</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Address
                </label>
                <input
                  type="text"
                  name="address"
                  placeholder="Your address"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Password
                </label>
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Confirm Password
                </label>
                <input
                  type="password"
                  name="confirmPassword"
                  placeholder="••••••••"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>
            </>
          )}

          {isLogin && (
            <>
              <div>
                <label className="block text-sm font-medium mb-2">
                  Email, Phone or Username
                </label>
                <input
                  type="text"
                  name="loginIdentifier"
                  placeholder="Enter email, phone or username"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Password
                </label>
                <input
                  type="password"
                  name="password"
                  placeholder="••••••••"
                  className="w-full px-4 py-2 rounded-lg border border-border bg-background focus:outline-none focus:border-purple-500 transition-colors"
                  required
                />
              </div>
            </>
          )}

          <button
            type="submit"
            disabled={isLoading}
            className="w-full px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? "Processing..." : isLogin ? "Login" : "Create Account"}
          </button>
        </form>

        {/* Switch Mode */}
        <p className="text-center text-sm text-muted-foreground mt-4">
          {isLogin ? "Don't have an account? " : "Already have an account? "}
          <button
            onClick={onSwitchMode}
            className="text-purple-600 hover:text-purple-700 font-semibold"
          >
            {isLogin ? "Sign Up" : "Login"}
          </button>
        </p>
      </div>
    </div>
  );
}
