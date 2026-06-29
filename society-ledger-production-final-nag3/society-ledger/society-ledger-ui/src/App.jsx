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
import AdminDashboardPage        from './pages/admin/DashboardPage'
import ExpenseListPage           from './pages/admin/finance/ExpenseListPage'
import ExpenseFormPage           from './pages/admin/finance/ExpenseFormPage'
import MonthlySummaryPage        from './pages/admin/finance/MonthlySummaryPage'
import StatementUploadPage       from './pages/admin/statement/StatementUploadPage'
import UnmatchedTransactionsPage from './pages/admin/statement/UnmatchedTransactionsPage'
import SocietyManagementPage     from './pages/admin/society/SocietyManagementPage'
import AuditReportsPage          from './pages/admin/audit/AuditReportsPage'
import AnnouncementsPage         from './pages/admin/announcements/AnnouncementsPage'

// Resident Pages
import FinancialTransparencyPage from './pages/resident/FinancialTransparencyPage'
import ReceiptListPage           from './pages/resident/ReceiptListPage'
import QueryPage                 from './pages/resident/QueryPage'

import { Box, Typography } from '@mui/material'

function ComingSoon({ title }) {
  return (
    <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center"
      minHeight="60vh" gap={2}>
      <Typography variant="h5" color="text.secondary">{title}</Typography>
      <Typography variant="body2" color="text.secondary">Coming soon.</Typography>
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
              {/* Public */}
              <Route path="/login"            element={<LoginPage />} />
              <Route path="/forgot-password"  element={<ForgotPasswordPage />} />
              <Route path="/"                 element={<Navigate to="/login" replace />} />

              {/* Admin */}
              <Route path="/admin" element={
                <ProtectedRoute requiredRole="ADMIN"><AdminLayout /></ProtectedRoute>
              }>
                <Route index                  element={<AdminDashboardPage />} />
                <Route path="society"         element={<SocietyManagementPage />} />
                <Route path="expenses"        element={<ExpenseListPage />} />
                <Route path="expenses/new"    element={<ExpenseFormPage />} />
                <Route path="statements"      element={<StatementUploadPage />} />
                <Route path="unmatched"       element={<UnmatchedTransactionsPage />} />
                <Route path="summary"         element={<MonthlySummaryPage />} />
                <Route path="audit"           element={<AuditReportsPage />} />
                <Route path="queries"         element={<QueryPage />} />
                <Route path="announcements"   element={<AnnouncementsPage />} />
                <Route path="activity-log"    element={<ComingSoon title="Activity Audit Log" />} />
                <Route path="receipts"        element={<ComingSoon title="All Receipts" />} />
              </Route>

              {/* Resident */}
              <Route path="/resident" element={
                <ProtectedRoute requiredRole="RESIDENT"><ResidentLayout /></ProtectedRoute>
              }>
                <Route index                  element={<FinancialTransparencyPage />} />
                <Route path="financials"      element={<FinancialTransparencyPage />} />
                <Route path="receipts"        element={<ReceiptListPage />} />
                <Route path="audit"           element={<AuditReportsPage />} />
                <Route path="queries"         element={<QueryPage />} />
                <Route path="announcements"   element={<AnnouncementsPage />} />
                <Route path="expenses"        element={<ComingSoon title="Published Expenses" />} />
                <Route path="family"          element={<ComingSoon title="Family Members" />} />
              </Route>

              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </SnackbarProvider>
    </ThemeProvider>
  )
}
