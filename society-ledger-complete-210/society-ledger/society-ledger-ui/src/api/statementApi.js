import axiosInstance from "./axiosInstance";

export const statementApi = {
  upload: async (societyId, file, bankCode = "BOB") => {
    const form = new FormData();
    form.append("file", file);
    form.append("bankCode", bankCode);
    const { data } = await axiosInstance.post(
      `/api/v1/statements/${societyId}/upload`,
      form,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return data.data;
  },

  getStatements: async (societyId, page = 0, size = 10) => {
    const { data } = await axiosInstance.get(
      `/api/v1/statements/${societyId}?page=${page}&size=${size}`
    );
    return data.data;
  },

  getTransactions: async (societyId, statementId, matchStatus, page = 0) => {
    const params = new URLSearchParams({ page, size: 50 });
    if (matchStatus) params.set("matchStatus", matchStatus);
    const { data } = await axiosInstance.get(
      `/api/v1/statements/${societyId}/${statementId}/transactions?${params}`
    );
    return data.data;
  },

  getUnmatched: async (societyId, page = 0) => {
    const { data } = await axiosInstance.get(
      `/api/v1/statements/${societyId}/transactions?matchStatus=UNMATCHED&page=${page}&size=50`
    );
    return data.data;
  },

  manualMatch: async (societyId, txnId, flatId) => {
    const { data } = await axiosInstance.patch(
      `/api/v1/statements/${societyId}/transactions/${txnId}/manual-match?flatId=${flatId}`
    );
    return data.data;
  },
};
