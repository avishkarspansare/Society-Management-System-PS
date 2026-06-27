import axios from 'axios'

const axiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor — attach JWT
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sl_access_token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — handle 401 and refresh token
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true
      const refreshToken = localStorage.getItem('sl_refresh_token')
      if (!refreshToken) {
        localStorage.clear()
        window.location.href = '/login'
        return Promise.reject(error)
      }
      try {
        const { data } = await axios.post('/api/v1/auth/refresh', { refreshToken })
        const { accessToken, refreshToken: newRefresh } = data.data
        localStorage.setItem('sl_access_token', accessToken)
        localStorage.setItem('sl_refresh_token', newRefresh)
        original.headers.Authorization = `Bearer ${accessToken}`
        return axiosInstance(original)
      } catch {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
