/**
 * EXAMPLE: How to use the error handling system
 *
 * Backend returns standard format:
 * {
 *   code: 1000,        // 1000 = success, others = error
 *   message: "...",    // Error/success message
 *   result: {...}      // Data (optional)
 * }
 */

import { login } from "@/services/authService";
import { getErrorMessage, getErrorCode, isApiError } from "@/lib/errors";

// ==========================================
// Example 1: Basic usage with try-catch
// ==========================================
async function handleLogin(username: string, password: string) {
  try {
    const response = await login({
      loginIdentifier: username,
      password: password,
    });

    console.log("Login success:", response);
    // Do something with response
  } catch (error) {
    // Get error message from backend (automatically handled)
    const errorMessage = getErrorMessage(error);
    console.error("Login failed:", errorMessage);

    // Display error to user
    alert(errorMessage); // or set to state for UI display
  }
}

// ==========================================
// Example 2: Advanced usage with error code
// ==========================================
async function handleLoginAdvanced(username: string, password: string) {
  try {
    const response = await login({
      loginIdentifier: username,
      password: password,
    });

    return { success: true, data: response };
  } catch (error) {
    const errorCode = getErrorCode(error);
    const errorMessage = getErrorMessage(error);

    // Handle specific error codes
    if (errorCode === 1001) {
      // User not found
      return { success: false, message: "User not found. Please register." };
    } else if (errorCode === 1002) {
      // Wrong password
      return { success: false, message: "Invalid password. Please try again." };
    } else if (errorCode === 1003) {
      // Account locked
      return { success: false, message: "Account is locked. Contact support." };
    } else {
      // Use backend message for other errors
      return { success: false, message: errorMessage };
    }
  }
}

// ==========================================
// Example 3: Using in React component
// ==========================================
import { useState } from "react";

function LoginForm() {
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const formData = new FormData(e.currentTarget);
      const username = formData.get("username") as string;
      const password = formData.get("password") as string;

      await login({ loginIdentifier: username, password });

      // Success - redirect or update UI
      window.location.href = "/dashboard";
    } catch (err) {
      // Display backend error message
      setError(getErrorMessage(err));

      // Optionally log error code for debugging
      console.error("Error code:", getErrorCode(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {error && (
        <div className="error-message bg-red-50 text-red-600 p-3 rounded">
          {error}
        </div>
      )}

      <input name="username" type="text" placeholder="Username" />
      <input name="password" type="password" placeholder="Password" />

      <button type="submit" disabled={loading}>
        {loading ? "Logging in..." : "Login"}
      </button>
    </form>
  );
}

// ==========================================
// Example 4: Check if error is ApiError
// ==========================================
async function handleWithTypeCheck(data: any) {
  try {
    // Some API call
    throw new Error("Something went wrong");
  } catch (error) {
    if (isApiError(error)) {
      // This is an error from our backend
      console.log("Backend error code:", error.code);
      console.log("Backend message:", error.message);
    } else {
      // This is a network error or other error
      console.log("Other error:", error);
    }
  }
}

// ==========================================
// Example 5: Toast notification with errors
// ==========================================
import { toast } from "sonner"; // or your toast library

async function handleWithToast(data: any) {
  try {
    // Some API call
    const response = await login(data);
    toast.success("Login successful!");
  } catch (error) {
    // Automatically show backend error message in toast
    toast.error(getErrorMessage(error));
  }
}

export {
  handleLogin,
  handleLoginAdvanced,
  LoginForm,
  handleWithTypeCheck,
  handleWithToast,
};
