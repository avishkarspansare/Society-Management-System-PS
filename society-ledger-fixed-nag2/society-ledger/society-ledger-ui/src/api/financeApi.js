import axiosInstance from './axiosInstance'

// ─────────────── Finance API ───────────────
export const financeApi = {
  // Expenses
  getExpenses: (societyId, params) =>
    axiosInstance.get(`/finance/${societyId}/expenses`, { params }),
  createExpense: (societyId, data) =>
    axiosInstance.post(`/finance/${societyId}/expenses`, data),
  getExpenseById: (societyId, id) =>
    axiosInstance.get(`/finance/${societyId}/expenses/${id}`),
  updateExpense: (societyId, id, data) =>
    axiosInstance.put(`/finance/${societyId}/expenses/${id}`, data),
  uploadProof: (societyId, id, file) => {
    const form = new FormData()
    form.append('file', file)
    return axiosInstance.post(`/finance/${societyId}/expenses/${id}/proof`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  publishExpense: (societyId, id) =>
    axiosInstance.patch(`/finance/${societyId}/expenses/${id}/publish`),
  archiveExpense: (societyId, id) =>
    axiosInstance.patch(`/finance/${societyId}/expenses/${id}/archive`),

  // Categories
  getCategories: (societyId) =>
    axiosInstance.get(`/finance/${societyId}/expense-categories`),
  createCategory: (societyId, data) =>
    axiosInstance.post(`/finance/${societyId}/expense-categories`, data),

  // Dashboard & Summary
  getDashboard: (societyId) =>
    axiosInstance.get(`/finance/${societyId}/dashboard`),
  getMonthlySummary: (societyId, year) =>
    axiosInstance.get(`/finance/${societyId}/monthly-summary`, { params: { year } }),

  // Timeline
  getTimeline: (societyId, params) =>
    axiosInstance.get(`/finance/${societyId}/timeline`, { params }),

  // Announcements
  getAnnouncements: (societyId) =>
    axiosInstance.get(`/finance/${societyId}/announcements`),
  createAnnouncement: (societyId, data) =>
    axiosInstance.post(`/finance/${societyId}/announcements`, data),
  deleteAnnouncement: (societyId, id) =>
    axiosInstance.delete(`/finance/${societyId}/announcements/${id}`),
}

// ─────────────── Statement API ───────────────
export const statementApi = {
  upload: (societyId, file, bankCode, month, year) => {
    const form = new FormData()
    form.append('file', file)
    form.append('bankCode', bankCode)
    form.append('month', month)
    form.append('year', year)
    return axiosInstance.post(`/statements/${societyId}/upload`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  getStatements: (societyId, params) =>
    axiosInstance.get(`/statements/${societyId}`, { params }),
  getTransactions: (societyId, params) =>
    axiosInstance.get(`/statements/${societyId}/transactions`, { params }),
  getUnmatched: (societyId, params) =>
    axiosInstance.get(`/statements/${societyId}/transactions/unmatched`, { params }),
  manualMatch: (societyId, txnId, data) =>
    axiosInstance.post(`/statements/${societyId}/transactions/${txnId}/manual-match`, data),
  getPayments: (societyId, params) =>
    axiosInstance.get(`/statements/${societyId}/payments`, { params }),
  getPaymentsByFlat: (societyId, flatId, params) =>
    axiosInstance.get(`/statements/${societyId}/payments/flat/${flatId}`, { params }),
}

// ─────────────── Receipt API ───────────────
export const receiptApi = {
  getReceipts: (societyId, params) =>
    axiosInstance.get(`/receipts/${societyId}`, { params }),
  getMyReceipts: (societyId, flatId, params) =>
    axiosInstance.get(`/receipts/${societyId}/flat/${flatId}`, { params }),
  downloadReceipt: (societyId, receiptId) =>
    axiosInstance.get(`/receipts/${societyId}/${receiptId}/download`, {
      responseType: 'blob',
    }),
}

// ─────────────── Audit API ───────────────
export const auditApi = {
  uploadReport: (societyId, formData) =>
    axiosInstance.post(`/audit/${societyId}/reports`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  getReports: (societyId, params) =>
    axiosInstance.get(`/audit/${societyId}/reports`, { params }),
  downloadReport: (societyId, reportId) =>
    axiosInstance.get(`/audit/${societyId}/reports/${reportId}/download`, {
      responseType: 'blob',
    }),
}

// ─────────────── Query API ───────────────
export const queryApi = {
  getQueries: (societyId, params) =>
    axiosInstance.get(`/queries/${societyId}`, { params }),
  createQuery: (societyId, data) =>
    axiosInstance.post(`/queries/${societyId}`, data),
  getQueryById: (societyId, queryId) =>
    axiosInstance.get(`/queries/${societyId}/${queryId}`),
  respondToQuery: (societyId, queryId, data) =>
    axiosInstance.post(`/queries/${societyId}/${queryId}/respond`, data),
  closeQuery: (societyId, queryId) =>
    axiosInstance.patch(`/queries/${societyId}/${queryId}/close`),
}
