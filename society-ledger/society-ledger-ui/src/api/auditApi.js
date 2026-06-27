import axiosInstance from "./axiosInstance";

export const auditApi = {
  upload: async (societyId, { title, description, periodFrom, periodTo, file }) => {
    const form = new FormData();
    form.append("file", file);
    form.append("title", title);
    if (description) form.append("description", description);
    if (periodFrom)  form.append("periodFrom", periodFrom);
    if (periodTo)    form.append("periodTo", periodTo);
    const { data } = await axiosInstance.post(
      `/api/v1/audit/${societyId}/reports`,
      form,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return data.data;
  },

  getAll: async (societyId, page = 0, size = 20) => {
    const { data } = await axiosInstance.get(
      `/api/v1/audit/${societyId}/reports?page=${page}&size=${size}`
    );
    return data.data;
  },

  publish: async (societyId, id) => {
    const { data } = await axiosInstance.patch(
      `/api/v1/audit/${societyId}/reports/${id}/publish`
    );
    return data.data;
  },
};
