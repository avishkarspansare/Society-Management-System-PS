import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi } from '../api/authApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)

  const loadUser = useCallback(async () => {
    const token = localStorage.getItem('sl_access_token')
    if (!token) { setLoading(false); return }
    try {
      const { data } = await authApi.getMe()
      setUser(data.data)
    } catch {
      localStorage.clear()
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadUser() }, [loadUser])

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password })
    const { accessToken, refreshToken, ...userInfo } = data.data
    localStorage.setItem('sl_access_token', accessToken)
    localStorage.setItem('sl_refresh_token', refreshToken)
    setUser(userInfo)
    return userInfo
  }

  const logout = async () => {
    const refreshToken = localStorage.getItem('sl_refresh_token')
    try { if (refreshToken) await authApi.logout(refreshToken) } catch {}
    localStorage.clear()
    setUser(null)
  }

  const isAdmin    = user?.role === 'ADMIN'
  const isResident = user?.role === 'RESIDENT'

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin, isResident }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
