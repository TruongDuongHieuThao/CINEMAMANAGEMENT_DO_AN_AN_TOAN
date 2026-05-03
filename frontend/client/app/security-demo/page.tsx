"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuthStore } from "@/store";

export default function SecurityDemoPage() {
  const [loginIdentifier, setLoginIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState("");
  const { isAuthenticated, login } = useAuthStore();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage("");

    try {
      const response = await fetch(
        "http://localhost:8080/api/theater-mgnt/auth/customer/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include", // Include cookies
          body: JSON.stringify({ loginIdentifier, password }),
        },
      );

      const data = await response.json();
      if (response.ok && data.result?.authenticated) {
        login();
        setMessage("Login successful! Cookies set.");
      } else {
        setMessage("Login failed: " + (data.message || "Unknown error"));
      }
    } catch (error) {
      setMessage("Login error: " + error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 mb-8">
            Security Labs Demo
          </h1>
          <p className="text-lg text-gray-600 mb-12">
            Test JWT vulnerabilities and CSRF attacks using Burp Suite
          </p>
        </div>

        {/* Login Form */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            Login to Victim App
          </h2>
          {isAuthenticated ? (
            <p className="text-green-600">
              ✅ Logged in! You can now test the labs.
            </p>
          ) : (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email/Username
                </label>
                <input
                  type="text"
                  value={loginIdentifier}
                  onChange={(e) => setLoginIdentifier(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="customer@example.com"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="password"
                  required
                />
              </div>
              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {isLoading ? "Logging in..." : "Login"}
              </button>
            </form>
          )}
          {message && (
            <p
              className={`mt-4 text-sm ${message.includes("successful") ? "text-green-600" : "text-red-600"}`}
            >
              {message}
            </p>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* JWT Labs */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              JWT Vulnerabilities
            </h2>
            <div className="space-y-3">
              <Link
                href="/security-demo/jwt"
                className="block w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
              >
                Test JWT Labs
              </Link>
              <div className="text-sm text-gray-600 space-y-1">
                <p>• Lab 1: Unverified signature</p>
                <p>• Lab 2: alg:none bypass</p>
                <p>• Lab 4: JWK injection</p>
              </div>
            </div>
          </div>

          {/* CSRF Labs */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              CSRF Attacks
            </h2>
            <div className="space-y-3">
              <Link
                href="/security-demo/csrf"
                className="block w-full bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
              >
                Test CSRF Labs
              </Link>
              <div className="text-sm text-gray-600 space-y-1">
                <p>• Lab 1: No defenses</p>
                <p>• Lab 2: Method-dependent validation</p>
                <p>• Lab 3: Token presence validation</p>
                <p>• Lab 11: Referer validation</p>
              </div>
            </div>
          </div>
        </div>

        <div className="mt-12 bg-yellow-50 border border-yellow-200 rounded-lg p-6">
          <h3 className="text-lg font-medium text-yellow-800 mb-2">
            Setup Instructions
          </h3>
          <div className="text-sm text-yellow-700 space-y-1">
            <p>1. Start Burp Suite proxy on 127.0.0.1:8888</p>
            <p>2. Configure browser to use Burp proxy</p>
            <p>3. Login to create session/cookies</p>
            <p>4. Use Burp to intercept and modify requests</p>
            <p>5. Test attacks on vulnerable endpoints</p>
          </div>
        </div>
      </div>
    </div>
  );
}
