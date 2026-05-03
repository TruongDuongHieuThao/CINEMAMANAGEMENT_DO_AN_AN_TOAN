"use client";

import { useState, useEffect } from "react";
import { CONFIG } from "@/configurations/configuration";

interface CustomerInfo {
  email: string;
  firstName: string;
  lastName: string;
}

export default function CsrfDemoPage() {
  const [myInfo, setMyInfo] = useState<CustomerInfo | null>(null);
  const [loadingInfo, setLoadingInfo] = useState(false);
  const [attackEmail, setAttackEmail] = useState("hacked@attacker.com");
  const [attackResult, setAttackResult] = useState<{
    status: number;
    ok: boolean;
    message: string;
  } | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchMyInfo = async () => {
    setLoadingInfo(true);
    try {
      const res = await fetch(
        `${CONFIG.API}/customers/myInfo`,
        { credentials: "include" },
      );
      if (res.ok) {
        const data = await res.json();
        setMyInfo(data.result);
      }
    } finally {
      setLoadingInfo(false);
    }
  };

  useEffect(() => {
    fetchMyInfo();
  }, []);

  // Victim thực hiện đổi email bình thường (same-origin) — luôn thành công
  const doNormalUpdate = async () => {
    setLoading(true);
    setAttackResult(null);
    try {
      const res = await fetch(
        `${CONFIG.API}/customers/myInfo/update-email-vuln`,
        {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          credentials: "include",
          body: new URLSearchParams({ email: attackEmail }),
        },
      );
      const data = await res.json();
      setAttackResult({
        status: res.status,
        ok: res.ok,
        message: res.ok
          ? `Email đổi thành công → ${data.result?.email ?? attackEmail}`
          : `Lỗi: ${JSON.stringify(data)}`,
      });
      if (res.ok) fetchMyInfo();
    } catch (e) {
      setAttackResult({ status: 0, ok: false, message: `Lỗi mạng: ${e}` });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-2xl mx-auto space-y-6">

        {/* Tiêu đề */}
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900">CSRF Attack Demo</h1>
          <p className="mt-1 text-gray-500 text-sm">
            Trang này mô phỏng victim app — người dùng đang đăng nhập bình thường
          </p>
        </div>

        {/* Thông tin victim hiện tại */}
        <div className="bg-white rounded-xl shadow p-6">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-semibold text-gray-800">Thông tin tài khoản (Victim)</h2>
            <button
              onClick={fetchMyInfo}
              disabled={loadingInfo}
              className="text-xs text-blue-600 hover:underline disabled:opacity-50"
            >
              {loadingInfo ? "Đang tải..." : "Làm mới"}
            </button>
          </div>

          {myInfo ? (
            <div className="space-y-1 text-sm">
              <div className="flex gap-2">
                <span className="text-gray-500 w-24">Họ tên:</span>
                <span className="font-medium text-gray-900">
                  {myInfo.firstName} {myInfo.lastName}
                </span>
              </div>
              <div className="flex gap-2">
                <span className="text-gray-500 w-24">Email hiện tại:</span>
                <span
                  className="font-mono font-semibold text-blue-700 bg-blue-50 px-2 py-0.5 rounded"
                  id="current-email"
                >
                  {myInfo.email}
                </span>
              </div>
            </div>
          ) : (
            <p className="text-sm text-red-500">
              Chưa đăng nhập hoặc không lấy được thông tin.{" "}
              <a href="/security-demo" className="underline">
                Đăng nhập tại đây
              </a>
            </p>
          )}
        </div>

        {/* Bước 1: Victim thực hiện thao tác bình thường */}
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="font-semibold text-gray-800 mb-1">
            Bước 1 — Victim đổi email (same-origin, bình thường)
          </h2>
          <p className="text-xs text-gray-500 mb-4">
            Đây là hành động hợp lệ. Victim chủ động thay đổi email của mình.
          </p>

          <div className="flex gap-2">
            <input
              type="email"
              value={attackEmail}
              onChange={(e) => setAttackEmail(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm text-black bg-white focus:ring-2 focus:ring-blue-500 focus:outline-none"
              placeholder="email mới..."
            />
            <button
              onClick={doNormalUpdate}
              disabled={loading || !myInfo}
              className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? "Đang gửi..." : "Đổi email"}
            </button>
          </div>

          {attackResult && (
            <div
              className={`mt-3 p-3 rounded-md text-sm ${
                attackResult.ok
                  ? "bg-green-50 text-green-800 border border-green-200"
                  : "bg-red-50 text-red-800 border border-red-200"
              }`}
            >
              <span className="font-mono">[{attackResult.status}]</span>{" "}
              {attackResult.message}
            </div>
          )}
        </div>

        {/* Bước 2: Hướng dẫn thực hiện tấn công */}
        <div className="bg-orange-50 border border-orange-200 rounded-xl p-6">
          <h2 className="font-semibold text-orange-800 mb-1">
            Bước 2 — Thực hiện tấn công (mở trang attacker)
          </h2>
          <p className="text-sm text-orange-700 mb-4">
            Victim vẫn đang đăng nhập ở tab này. Giờ mở link bên dưới — đây là
            trang của attacker (khác origin). Trang đó sẽ tự động gửi request
            đổi email mà victim không hay biết.
          </p>

          <a
            href="http://localhost:4000/csrf-real-profile.html"
            target="_blank"
            rel="noreferrer"
            className="inline-block bg-red-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-red-700 transition-colors"
          >
            Mở trang attacker (localhost:4000) →
          </a>

          <p className="mt-3 text-xs text-orange-600">
            Sau khi mở trang attacker, nhấn <strong>Làm mới</strong> ở phần
            thông tin bên trên để thấy email đã bị thay đổi.
          </p>
        </div>

        {/* Giải thích tại sao tấn công được */}
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="font-semibold text-gray-800 mb-3">Tại sao tấn công thành công?</h2>
          <div className="space-y-2 text-sm text-gray-700">
            <div className="flex gap-2">
              <span className="text-red-500 font-bold shrink-0">✗</span>
              <span>
                <strong>Không có CSRF protection</strong> — backend cấu hình{" "}
                <code className="bg-gray-100 px-1 rounded">csrf().disable()</code>
              </span>
            </div>
            <div className="flex gap-2">
              <span className="text-red-500 font-bold shrink-0">✗</span>
              <span>
                <strong>Cookie SameSite=None</strong> — browser tự động gửi{" "}
                <code className="bg-gray-100 px-1 rounded">access_token</code>{" "}
                kèm theo mọi request, kể cả cross-site
              </span>
            </div>
            <div className="flex gap-2">
              <span className="text-red-500 font-bold shrink-0">✗</span>
              <span>
                <strong>Endpoint chỉ cần JWT</strong> — không yêu cầu thêm bất
                kỳ token hay header nào khác
              </span>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
