import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { Box, CircularProgress } from '@mui/material'

export default function ProtectedRoute({ children, requiredRole }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress size={48} />
      </Box>
    )
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    // Redirect residents to their dashboard, admins to theirs
    return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/resident'} replace />
  }

  return children
}
