import { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, InputAdornment, IconButton, Divider, CircularProgress
} from '@mui/material'
import { Visibility, VisibilityOff, AccountBalance } from '@mui/icons-material'
import { useAuth } from '../../auth/AuthContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()

  const [form, setForm]       = useState({ email: '', password: '' })
  const [showPwd, setShowPwd] = useState(false)
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(form.email, form.password)
      navigate(user.role === 'ADMIN' ? '/admin' : '/resident', { replace: true })
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Please check your credentials.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #1d4ed8 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
      }}
    >
      <Card sx={{ maxWidth: 420, width: '100%', borderRadius: 3 }}>
        <CardContent sx={{ p: 4 }}>
          {/* Logo & Title */}
          <Box display="flex" flexDirection="column" alignItems="center" mb={3}>
            <Box
              sx={{
                width: 60, height: 60, borderRadius: 2,
                bgcolor: 'primary.main', display: 'flex',
                alignItems: 'center', justifyContent: 'center', mb: 2,
              }}
            >
              <AccountBalance sx={{ color: 'white', fontSize: 30 }} />
            </Box>
            <Typography variant="h5" fontWeight={700} color="text.primary">
              Society Ledger
            </Typography>
            <Typography variant="body2" color="text.secondary" mt={0.5}>
              Financial Transparency Platform
            </Typography>
          </Box>

          <Divider sx={{ mb: 3 }} />

          {error && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              label="Email Address"
              name="email"
              type="email"
              fullWidth
              required
              value={form.email}
              onChange={handleChange}
              sx={{ mb: 2 }}
              autoComplete="email"
            />
            <TextField
              label="Password"
              name="password"
              type={showPwd ? 'text' : 'password'}
              fullWidth
              required
              value={form.password}
              onChange={handleChange}
              sx={{ mb: 1 }}
              autoComplete="current-password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPwd(s => !s)} edge="end">
                      {showPwd ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Box display="flex" justifyContent="flex-end" mb={3}>
              <Typography
                component={RouterLink}
                to="/forgot-password"
                variant="body2"
                color="primary"
                sx={{ textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
              >
                Forgot Password?
              </Typography>
            </Box>

            <Button
              type="submit"
              variant="contained"
              fullWidth
              size="large"
              disabled={loading}
              sx={{ py: 1.4, fontSize: '1rem' }}
            >
              {loading ? <CircularProgress size={22} color="inherit" /> : 'Sign In'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  )
}
