import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider, CssBaseline } from '@mui/material'
import { SnackbarProvider } from 'notistack'
import theme from './theme/muiTheme'
import { AuthProvider } from './auth/AuthContext'
import ProtectedRoute from './auth/ProtectedRoute'

// Layouts
import AdminLayout    from './components/layout/AdminLayout'
import ResidentLayout from './components/layout/ResidentLayout'

// Auth Pages
import LoginPage          from './pages/auth/LoginPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'

// Admin Pages
import AdminDashboardPage    from './pages/admin/DashboardPage'
import ExpenseListPage       from './pages/admin/finance/ExpenseListPage'
import ExpenseFormPage       from './pages/admin/finance/ExpenseFormPage'
import StatementUploadPage   from './pages/admin/statement/StatementUploadPage'

// Resident Pages
import FinancialTransparencyPage from './pages/resident/FinancialTransparencyPage'
import ReceiptListPage           from './pages/resident/ReceiptListPage'
import QueryPage                 from './pages/resident/QueryPage'

// Lazy placeholder for pages not yet built
import { Suspense, lazy } from 'react'
import { Box, CircularProgress, Typography } from '@mui/material'

function ComingSoon({ title }) {
  return (
    <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center"
      minHeight="60vh" gap={2}>
      <Typography variant="h5" color="text.secondary">{title}</Typography>
      <Typography variant="body2" color="text.secondary">Module coming soon.</Typography>
    </Box>
  )
}

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <SnackbarProvider maxSnack={3} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route path="/" element={<Navigate to="/login" replace />} />

              {/* Admin Routes */}
              <Route path="/admin" element={
                <ProtectedRoute requiredRole="ADMIN"><AdminLayout /></ProtectedRoute>
              }>
                <Route index element={<AdminDashboardPage />} />
                <Route path="society"         element={<ComingSoon title="Society & Flat Management" />} />
                <Route path="expenses"        element={<ExpenseListPage />} />
                <Route path="expenses/new"    element={<ExpenseFormPage />} />
                <Route path="expenses/:id"    element={<ComingSoon title="Expense Detail" />} />
                <Route path="statements"      element={<StatementUploadPage />} />
                <Route path="unmatched"       element={<ComingSoon title="Unmatched Transactions" />} />
                <Route path="receipts"        element={<ComingSoon title="All Receipts" />} />
                <Route path="summary"         element={<ComingSoon title="Monthly Summary" />} />
                <Route path="audit"           element={<ComingSoon title="Audit Reports" />} />
                <Route path="queries"         element={<QueryPage />} />
                <Route path="announcements"   element={<ComingSoon title="Announcements" />} />
                <Route path="activity-log"    element={<ComingSoon title="Activity Audit Log" />} />
              </Route>

              {/* Resident Routes */}
              <Route path="/resident" element={
                <ProtectedRoute requiredRole="RESIDENT"><ResidentLayout /></ProtectedRoute>
              }>
                <Route index element={<ComingSoon title="Resident Dashboard" />} />
                <Route path="financials"   element={<FinancialTransparencyPage />} />
                <Route path="expenses"     element={<ComingSoon title="Published Expenses" />} />
                <Route path="receipts"     element={<ReceiptListPage />} />
                <Route path="audit"        element={<ComingSoon title="Audit Reports" />} />
                <Route path="queries"      element={<QueryPage />} />
                <Route path="announcements" element={<ComingSoon title="Announcements" />} />
                <Route path="family"       element={<ComingSoon title="Family Members" />} />
              </Route>

              {/* Catch-all */}
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </SnackbarProvider>
    </ThemeProvider>
  )
}
