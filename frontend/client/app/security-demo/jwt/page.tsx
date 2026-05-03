"use client";

import { useState, useEffect } from "react";

export default function JwtLabsPage() {
  const [result, setResult] = useState("");
  const [token, setToken] = useState("");

  useEffect(() => {
    // Get token from cookie
    const cookies = document.cookie.split("; ");
    const accessToken = cookies
      .find((row) => row.startsWith("access_token="))
      ?.split("=")[1];
    if (accessToken) {
      setToken(accessToken);
    }
  }, []);

  const testLab = async (labNumber: number) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/theater-mgnt/security-lab/jwt/lab${labNumber}/whoami`,
        {
          credentials: "include",
        },
      );

      const data = await response.text();
      setResult(`Lab ${labNumber}: ${response.status}\n${data}`);
    } catch (error) {
      setResult(`Lab ${labNumber}: Error - ${error}`);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-center text-gray-900 mb-8">
          JWT Labs Test
        </h1>

        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Current Access Token
            </label>
            <textarea
              value={token}
              readOnly
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-sm font-mono"
              placeholder="Token will appear here after login..."
            />
          </div>

          <div className="grid grid-cols-3 gap-4 mb-4">
            <button
              onClick={() => testLab(1)}
              className="bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
            >
              Test Lab 1 (Unverified)
            </button>
            <button
              onClick={() => testLab(2)}
              className="bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
            >
              Test Lab 2 (alg:none)
            </button>
            <button
              onClick={() => testLab(4)}
              className="bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 transition-colors"
            >
              Test Lab 4 (JWK)
            </button>
          </div>
        </div>

        {result && (
          <div className="bg-gray-100 rounded-lg p-4 mb-6">
            <h3 className="font-medium text-gray-900 mb-2">Result:</h3>
            <pre className="text-sm text-gray-700 whitespace-pre-wrap font-mono">
              {result}
            </pre>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-medium text-red-800 mb-2">
              Lab 1: Unverified Signature
            </h3>
            <p className="text-sm text-red-700">
              Server decodes JWT without verifying signature. Use Burp JWT
              Editor to modify payload.
            </p>
          </div>

          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-medium text-red-800 mb-2">Lab 2: alg:none</h3>
            <p className="text-sm text-red-700">
              Server accepts JWT with alg: none. Remove signature in Burp JWT
              Editor.
            </p>
          </div>

          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-medium text-red-800 mb-2">
              Lab 4: JWK Injection
            </h3>
            <p className="text-sm text-red-700">
              Server trusts JWK header. Use Burp JWT Editor Attack → Embedded
              JWK.
            </p>
          </div>
        </div>

        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-medium text-blue-800 mb-2">
            How to Attack with Burp
          </h3>
          <div className="text-sm text-blue-700 space-y-1">
            <p>1. Login to get access_token cookie</p>
            <p>2. Send request to vulnerable endpoint to Repeater</p>
            <p>3. Use JWT Editor tab to modify token</p>
            <p>4. Change payload (Lab 1) or algorithm (Lab 2/4)</p>
            <p>5. Send modified request to test vulnerability</p>
          </div>
        </div>
      </div>
    </div>
  );
}
