import { useState } from 'react'
import { useNavigate, Link as RouterLink } from 'react-router-dom'
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, CircularProgress, Stepper, Step, StepLabel,
  InputAdornment, IconButton
} from '@mui/material'
import { AccountBalance, Visibility, VisibilityOff } from '@mui/icons-material'
import { authApi } from '../../api/authApi'

const STEPS = ['Enter Email', 'Verify OTP', 'Reset Password']

export default function ForgotPasswordPage() {
  const navigate = useNavigate()

  const [activeStep, setActiveStep] = useState(0)
  const [email, setEmail]           = useState('')
  const [otp, setOtp]               = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPwd, setShowPwd]       = useState(false)
  const [error, setError]           = useState('')
  const [success, setSuccess]       = useState('')
  const [loading, setLoading]       = useState(false)

  const handleInitiate = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      await authApi.initiateForgotPassword(email)
      setSuccess('OTP sent to your email. Valid for 10 minutes.')
      setActiveStep(1)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to send OTP.')
    } finally { setLoading(false) }
  }

  const handleVerifyOtp = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      await authApi.verifyOtp(email, otp)
      setSuccess('OTP verified. Please set your new password.')
      setActiveStep(2)
    } catch (err) {
      setError(err.response?.data?.error || 'Invalid or expired OTP.')
    } finally { setLoading(false) }
  }

  const handleReset = async (e) => {
    e.preventDefault()
    if (newPassword !== confirmPassword) { setError('Passwords do not match.'); return }
    if (newPassword.length < 8) { setError('Password must be at least 8 characters.'); return }
    setError(''); setLoading(true)
    try {
      await authApi.resetPassword({ email, otpCode: otp, newPassword, confirmPassword })
      setSuccess('Password reset successful! Redirecting to login...')
      setTimeout(() => navigate('/login'), 2000)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to reset password.')
    } finally { setLoading(false) }
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #1d4ed8 100%)',
        display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2,
      }}
    >
      <Card sx={{ maxWidth: 460, width: '100%', borderRadius: 3 }}>
        <CardContent sx={{ p: 4 }}>
          <Box display="flex" alignItems="center" gap={1.5} mb={3}>
            <Box sx={{ width: 40, height: 40, borderRadius: 1.5, bgcolor: 'primary.main',
              display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <AccountBalance sx={{ color: 'white', fontSize: 22 }} />
            </Box>
            <Box>
              <Typography variant="h6" fontWeight={700}>Forgot Password</Typography>
              <Typography variant="caption" color="text.secondary">Society Ledger</Typography>
            </Box>
          </Box>

          <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
            {STEPS.map(label => (
              <Step key={label}><StepLabel>{label}</StepLabel></Step>
            ))}
          </Stepper>

          {error   && <Alert severity="error"   sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{success}</Alert>}

          {/* Step 0 — Enter Email */}
          {activeStep === 0 && (
            <form onSubmit={handleInitiate}>
              <Typography variant="body2" color="text.secondary" mb={2}>
                Enter the email address associated with your account.
              </Typography>
              <TextField
                label="Email Address" type="email" fullWidth required
                value={email} onChange={e => setEmail(e.target.value)} sx={{ mb: 3 }}
              />
              <Button type="submit" variant="contained" fullWidth size="large"
                disabled={loading} sx={{ py: 1.4 }}>
                {loading ? <CircularProgress size={22} color="inherit" /> : 'Send OTP'}
              </Button>
            </form>
          )}

          {/* Step 1 — Verify OTP */}
          {activeStep === 1 && (
            <form onSubmit={handleVerifyOtp}>
              <Typography variant="body2" color="text.secondary" mb={2}>
                Enter the 6-digit OTP sent to <strong>{email}</strong>
              </Typography>
              <TextField
                label="6-Digit OTP" fullWidth required
                value={otp} onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                inputProps={{ maxLength: 6, style: { letterSpacing: '0.4em', fontSize: '1.3rem', textAlign: 'center' } }}
                sx={{ mb: 3 }}
              />
              <Button type="submit" variant="contained" fullWidth size="large"
                disabled={loading || otp.length !== 6} sx={{ py: 1.4 }}>
                {loading ? <CircularProgress size={22} color="inherit" /> : 'Verify OTP'}
              </Button>
              <Button fullWidth sx={{ mt: 1 }} onClick={() => { setActiveStep(0); setError(''); setSuccess('') }}>
                Back
              </Button>
            </form>
          )}

          {/* Step 2 — New Password */}
          {activeStep === 2 && (
            <form onSubmit={handleReset}>
              <TextField
                label="New Password" type={showPwd ? 'text' : 'password'} fullWidth required
                value={newPassword} onChange={e => setNewPassword(e.target.value)} sx={{ mb: 2 }}
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
              <TextField
                label="Confirm Password" type={showPwd ? 'text' : 'password'} fullWidth required
                value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} sx={{ mb: 3 }}
              />
              <Button type="submit" variant="contained" fullWidth size="large"
                disabled={loading} sx={{ py: 1.4 }}>
                {loading ? <CircularProgress size={22} color="inherit" /> : 'Reset Password'}
              </Button>
            </form>
          )}

          <Box mt={2} textAlign="center">
            <Typography component={RouterLink} to="/login" variant="body2" color="primary"
              sx={{ textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
              ← Back to Login
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}
