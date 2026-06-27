import axiosInstance from "./axiosInstance";

export const societyApi = {
  getSociety: async (societyId) => {
    const { data } = await axiosInstance.get(`/api/v1/societies/${societyId}`);
    return data.data;
  },

  getWings: async (societyId) => {
    const { data } = await axiosInstance.get(`/api/v1/societies/${societyId}/wings`);
    return data.data;
  },

  createWing: async (societyId, wingName, totalFloors) => {
    const { data } = await axiosInstance.post(`/api/v1/societies/${societyId}/wings`, {
      wingName, totalFloors,
    });
    return data.data;
  },

  getFlats: async (societyId, page = 0, size = 50) => {
    const { data } = await axiosInstance.get(
      `/api/v1/societies/${societyId}/flats?page=${page}&size=${size}`
    );
    return data.data;
  },

  createFlat: async (societyId, flatData) => {
    const { data } = await axiosInstance.post(
      `/api/v1/societies/${societyId}/flats`,
      flatData
    );
    return data.data;
  },

  getActivityLog: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/societies/${societyId}/activity-log?page=${page}&size=${size}`
    );
    return data.data;
  },
};
