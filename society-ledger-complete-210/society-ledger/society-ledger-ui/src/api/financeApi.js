import axiosInstance from "./axiosInstance";

export const financeApi = {
  getDashboard: async (societyId) => {
    const { data } = await axiosInstance.get(`/api/v1/finance/${societyId}/dashboard`);
    return data.data;
  },

  getMonthlySummary: async (societyId, year) => {
    const params = year ? `?year=${year}` : "";
    const { data } = await axiosInstance.get(`/api/v1/finance/${societyId}/monthly-summary${params}`);
    return data.data;
  },

  getTimeline: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/finance/${societyId}/timeline?page=${page}&size=${size}`
    );
    return data.data;
  },

  getExpenses: async (societyId, status, page = 0, size = 20) => {
    const params = new URLSearchParams({ page, size });
    if (status) params.set("status", status);
    const { data } = await axiosInstance.get(
      `/api/v1/finance/${societyId}/expenses?${params}`
    );
    return data.data;
  },

  createExpense: async (societyId, expenseData) => {
    const { data } = await axiosInstance.post(
      `/api/v1/finance/${societyId}/expenses`,
      expenseData
    );
    return data.data;
  },

  uploadProof: async (societyId, expenseId, file) => {
    const form = new FormData();
    form.append("file", file);
    const { data } = await axiosInstance.post(
      `/api/v1/finance/${societyId}/expenses/${expenseId}/proof`,
      form,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return data.data;
  },

  publishExpense: async (societyId, expenseId) => {
    const { data } = await axiosInstance.patch(
      `/api/v1/finance/${societyId}/expenses/${expenseId}/publish`
    );
    return data.data;
  },

  getCategories: async (societyId) => {
    const { data } = await axiosInstance.get(
      `/api/v1/finance/${societyId}/expense-categories`
    );
    return data.data;
  },

  createCategory: async (societyId, name, description) => {
    const { data } = await axiosInstance.post(
      `/api/v1/finance/${societyId}/expense-categories`,
      { name, description }
    );
    return data.data;
  },

  getAnnouncements: async (societyId) => {
    const { data } = await axiosInstance.get(
      `/api/v1/finance/${societyId}/announcements`
    );
    return data.data;
  },

  createAnnouncement: async (societyId, title, content, isPinned = false) => {
    const { data } = await axiosInstance.post(
      `/api/v1/finance/${societyId}/announcements`,
      { title, content, isPinned }
    );
    return data.data;
  },

  deleteAnnouncement: async (societyId, id) => {
    await axiosInstance.delete(`/api/v1/finance/${societyId}/announcements/${id}`);
  },
};
