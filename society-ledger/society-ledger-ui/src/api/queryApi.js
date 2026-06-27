import axiosInstance from "./axiosInstance";

export const queryApi = {
  create: async (societyId, { subject, body }) => {
    const { data } = await axiosInstance.post(`/api/v1/queries/${societyId}`, { subject, body });
    return data.data;
  },

  getAll: async (societyId, status, page = 0, size = 20) => {
    const params = new URLSearchParams({ page, size });
    if (status) params.set("status", status);
    const { data } = await axiosInstance.get(
      `/api/v1/queries/${societyId}?${params}`
    );
    return data.data;
  },

  getMyQueries: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/queries/${societyId}?page=${page}&size=${size}`
    );
    return data.data;
  },

  answer: async (societyId, queryId, answer) => {
    const { data } = await axiosInstance.patch(
      `/api/v1/queries/${societyId}/${queryId}/answer`,
      { answer }
    );
    return data.data;
  },

  close: async (societyId, queryId) => {
    const { data } = await axiosInstance.patch(
      `/api/v1/queries/${societyId}/${queryId}/close`
    );
    return data.data;
  },
};
