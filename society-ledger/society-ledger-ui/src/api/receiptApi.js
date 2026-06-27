import axiosInstance from "./axiosInstance";

export const receiptApi = {
  getMy: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/receipts/${societyId}/my?page=${page}&size=${size}`
    );
    return data.data;
  },

  getAll: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/receipts/${societyId}?page=${page}&size=${size}`
    );
    return data.data;
  },

  getForFlat: async (societyId, flatId, page = 0) => {
    const { data } = await axiosInstance.get(
      `/api/v1/receipts/${societyId}/flat/${flatId}?page=${page}&size=20`
    );
    return data.data;
  },
};
