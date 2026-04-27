export const CONFIG = {
  API: import.meta.env.VITE_API_URL || "http://localhost:8080/api/theater-mgnt",
}

// Deploy API: https://api.privateclinic.id.vn/api/theater-mgnt
export const API = {
  LOGIN: "/auth/admin/login",
  FORGOT_PASSWORD: "/auth/forgot-password",
  RESET_PASSWORD: "/auth/reset-password",
  MY_INFO: "staffs/myInfo",
  UPDATE_STAFF: "/staffs/${staffId}",
}
