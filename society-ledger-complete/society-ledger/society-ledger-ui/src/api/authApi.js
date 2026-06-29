import axiosInstance from './axiosInstance'

export const authApi = {
  register: (data)              => axiosInstance.post('/auth/register', data),
  login:    (data)              => axiosInstance.post('/auth/login', data),
  logout:   (refreshToken)      => axiosInstance.post('/auth/logout', { refreshToken }),
  getMe:    ()                  => axiosInstance.get('/auth/me'),
  initiateForgotPassword: (email) =>
    axiosInstance.post('/auth/forgot-password/initiate', { email }),
  verifyOtp: (email, otpCode)   =>
    axiosInstance.post('/auth/forgot-password/verify-otp', { email, otpCode }),
  resetPassword: (data)         =>
    axiosInstance.post('/auth/forgot-password/reset', data),
}
