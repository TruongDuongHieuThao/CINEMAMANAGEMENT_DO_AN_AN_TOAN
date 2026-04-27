"use client";

import { useEffect, useState } from "react";
import { getMyInfo } from "@/services/customerService";
import { getToken } from "@/services/localStorageService";
import { useRouter } from "next/navigation";

export default function ProfilePage() {
  const [userInfo, setUserInfo] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchUserInfo = async () => {
      const token = getToken();

      if (!token) {
        router.push("/");
        return;
      }

      try {
        const info = await getMyInfo();
        setUserInfo(info);
      } catch (err: any) {
        console.error("Failed to fetch user info:", err);
        setError("Failed to load profile information");
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [router]);

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
              {userInfo?.fullName?.charAt(0)?.toUpperCase() || "U"}
            </div>
            <div>
              <h2 className="text-2xl font-bold mb-1">
                {userInfo?.fullName || "User"}
              </h2>
              <p className="text-muted-foreground">
                {userInfo?.email || "No email"}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-sm text-muted-foreground">Email</label>
              <p className="text-lg">{userInfo?.email || "N/A"}</p>
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

            {userInfo?.dateOfBirth && (
              <div>
                <label className="text-sm text-muted-foreground">
                  Date of Birth
                </label>
                <p className="text-lg">
                  {new Date(userInfo.dateOfBirth).toLocaleDateString()}
                </p>
              </div>
            )}
          </div>

          <div className="mt-8">
            <button className="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
              Edit Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
