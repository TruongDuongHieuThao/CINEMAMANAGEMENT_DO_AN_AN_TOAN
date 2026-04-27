import httpClient from "@/configurations/httpClient";

export interface MovieRevenue {
  id: string;
  movieId: string;
  cinemaId: string;
  reportDate: string;
  totalTicketsSold: number;
  totalRevenue: number;
}

export interface DailyRevenueRow {
  id: string;
  cinemaId: string;
  reportDate: string;
  ticketRevenue: number;
  comboRevenue: number;
  netRevenue: number;
  totalTransactions: number;
}

export type ReportType = "DAILY" | "WEEKLY" | "MONTHLY" | "YEARLY" | "CUSTOM";

export interface RevenueReportRow {
  id: string;
  cinemaId: string;
  reportType: ReportType;
  startDate: string;
  endDate: string;
  totalTicketRevenue: number;
  totalComboRevenue: number;
  netRevenue: number;
  generatedAt: string;
}

const base = `/revenue`;

export const revenueService = {
  async getMovieRevenue(params: {
    cinemaId?: string;
    movieId?: string;
    from?: string;
    to?: string;
  }) {
    const response = await httpClient.get<MovieRevenue[]>(`${base}/movie`, { params });
    return response.data;
  },

  async getDailyRevenue(params: { cinemaId?: string; from?: string; to?: string }) {
    const response = await httpClient.get<DailyRevenueRow[]>(`${base}/daily`, { params });
    return response.data;
  },

  async getRevenueReports(params: {
    cinemaId?: string;
    reportType?: ReportType;
    from?: string;
    to?: string;
  }) {
    const response = await httpClient.get<RevenueReportRow[]>(`${base}/reports`, { params });
    return response.data;
  },

  async generateReport(payload: {
    cinemaId: string;
    reportType: ReportType;
    startDate: string;
    endDate: string;
  }) {
    const response = await httpClient.post<RevenueReportRow>(`${base}/reports/generate`, payload);
    return response.data;
  },
};
