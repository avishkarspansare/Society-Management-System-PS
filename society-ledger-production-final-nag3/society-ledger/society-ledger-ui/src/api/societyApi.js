import axiosInstance from './axiosInstance'

// ─────────────── Society API ───────────────
export const societyApi = {
  // Society
  getMySociety: (societyId) =>
    axiosInstance.get(`/societies/${societyId}`),
  updateSociety: (societyId, data) =>
    axiosInstance.put(`/societies/${societyId}`, data),

  // Wings
  getWings: (societyId) =>
    axiosInstance.get(`/societies/${societyId}/wings`),
  createWing: (societyId, data) =>
    axiosInstance.post(`/societies/${societyId}/wings`, data),
  deleteWing: (societyId, wingId) =>
    axiosInstance.delete(`/societies/${societyId}/wings/${wingId}`),

  // Flats
  getFlats: (societyId, params) =>
    axiosInstance.get(`/societies/${societyId}/flats`, { params }),
  getFlatById: (societyId, flatId) =>
    axiosInstance.get(`/societies/${societyId}/flats/${flatId}`),
  createFlat: (societyId, data) =>
    axiosInstance.post(`/societies/${societyId}/flats`, data),
  updateFlat: (societyId, flatId, data) =>
    axiosInstance.put(`/societies/${societyId}/flats/${flatId}`, data),
  deleteFlat: (societyId, flatId) =>
    axiosInstance.delete(`/societies/${societyId}/flats/${flatId}`),

  // Family Members
  getFamilyMembers: (societyId, flatId) =>
    axiosInstance.get(`/societies/${societyId}/flats/${flatId}/members`),
  addFamilyMember: (societyId, flatId, data) =>
    axiosInstance.post(`/societies/${societyId}/flats/${flatId}/members`, data),
  removeFamilyMember: (societyId, flatId, memberId) =>
    axiosInstance.delete(`/societies/${societyId}/flats/${flatId}/members/${memberId}`),

  // Activity Audit Log
  getActivityLog: (societyId, params) =>
    axiosInstance.get(`/societies/${societyId}/activity-log`, { params }),
}
