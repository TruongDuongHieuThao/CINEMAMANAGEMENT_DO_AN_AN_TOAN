"use client";

import { useEffect, useState } from "react";
import { getMyInfo, updateMyEmailForCsrfDemo } from "@/services/customerService";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store";
import type { CustomerInfo } from "@/services/customerService";

export default function ProfilePage() {
  const [userInfo, setUserInfo] = useState<CustomerInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [emailDraft, setEmailDraft] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveResult, setSaveResult] = useState<string | null>(null);
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const displayName = userInfo
    ? `${userInfo.firstName} ${userInfo.lastName}`.trim()
    : "User";

  useEffect(() => {
    const fetchUserInfo = async () => {
      if (!isAuthenticated) {
        router.push("/");
        return;
      }

      try {
        const info = await getMyInfo();
        setUserInfo(info);
        setEmailDraft(info.email || "");
      } catch (err) {
        console.error("Failed to fetch user info:", err);
        setError("Failed to load profile information");
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [router, isAuthenticated]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button
            onClick={() => router.push("/")}
            className="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
          >
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen pt-20 pb-12 px-4">
      <div className="container mx-auto max-w-4xl">
        <h1 className="text-3xl font-bold mb-8">My Profile</h1>

        <div className="bg-card border border-border rounded-2xl p-8">
          <div className="flex items-center gap-6 mb-8">
            <div className="w-24 h-24 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 flex items-center justify-center text-white text-3xl font-bold">
              {displayName.charAt(0).toUpperCase() || "U"}
            </div>
            <div>
              <h2 className="text-2xl font-bold mb-1">
                {displayName}
              </h2>
              <p className="text-muted-foreground">
                {userInfo?.email || "No email"}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-sm text-muted-foreground">Email</label>
              {editing ? (
                <input
                  type="email"
                  value={emailDraft}
                  onChange={(e) => setEmailDraft(e.target.value)}
                  className="mt-2 w-full max-w-md rounded-md border border-gray-500 bg-white px-3 py-2 text-black"
                  placeholder="new-email@example.com"
                />
              ) : (
                <p className="text-lg">{userInfo?.email || "N/A"}</p>
              )}
            </div>

            {userInfo?.phoneNumber && (
              <div>
                <label className="text-sm text-muted-foreground">
                  Phone Number
                </label>
                <p className="text-lg">{userInfo.phoneNumber}</p>
              </div>
            )}

            {userInfo?.address && (
              <div>
                <label className="text-sm text-muted-foreground">Address</label>
                <p className="text-lg">{userInfo.address}</p>
              </div>
            )}

            {userInfo?.dob && (
              <div>
                <label className="text-sm text-muted-foreground">
                  Date of Birth
                </label>
                <p className="text-lg">
                  {new Date(userInfo.dob).toLocaleDateString()}
                </p>
              </div>
            )}
          </div>

          <div className="mt-8">
            {!editing ? (
              <button
                onClick={() => {
                  setEmailDraft(userInfo?.email || "");
                  setSaveResult(null);
                  setEditing(true);
                }}
                className="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
              >
                Edit Profile
              </button>
            ) : (
              <div className="space-y-3">
                <div className="flex gap-3">
                  <button
                    disabled={saving}
                    onClick={async () => {
                      try {
                        setSaving(true);
                        const updated = await updateMyEmailForCsrfDemo(emailDraft, "vulnerable");
                        setUserInfo(updated);
                        setSaveResult("Updated by vulnerable endpoint (for CSRF demo).");
                        setEditing(false);
                      } catch (e) {
                        const message = e instanceof Error ? e.message : "Update failed";
                        setSaveResult(message);
                      } finally {
                        setSaving(false);
                      }
                    }}
                    className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition-colors disabled:opacity-60"
                  >
                    Save (Vulnerable)
                  </button>
                  <button
                    disabled={saving}
                    onClick={async () => {
                      try {
                        setSaving(true);
                        const updated = await updateMyEmailForCsrfDemo(emailDraft, "defended");
                        setUserInfo(updated);
                        setSaveResult("Updated by defended endpoint.");
                        setEditing(false);
                      } catch (e) {
                        const message = e instanceof Error ? e.message : "Update failed";
                        setSaveResult(message);
                      } finally {
                        setSaving(false);
                      }
                    }}
                    className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 transition-colors disabled:opacity-60"
                  >
                    Save (Defended)
                  </button>
                  <button
                    disabled={saving}
                    onClick={() => {
                      setEditing(false);
                      setEmailDraft(userInfo?.email || "");
                    }}
                    className="px-4 py-2 rounded-lg bg-gray-600 text-white hover:bg-gray-700 transition-colors disabled:opacity-60"
                  >
                    Cancel
                  </button>
                </div>
                {saveResult && <p className="text-sm text-muted-foreground">{saveResult}</p>}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
