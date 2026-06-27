import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { CssBaseline, ThemeProvider, createTheme } from "@mui/material";

// Auth
import LoginPage from "./pages/auth/LoginPage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";

// Layouts
import AdminLayout from "./layouts/AdminLayout";
import ResidentLayout from "./layouts/ResidentLayout";

// Admin Pages
import DashboardPage from "./pages/admin/DashboardPage";
import SocietyManagementPage from "./pages/admin/SocietyManagementPage";
import ExpensesPage from "./pages/admin/ExpensesPage";
import StatementUploadPage from "./pages/admin/StatementUploadPage";
import UnmatchedTransactionsPage from "./pages/admin/UnmatchedTransactionsPage";
import AuditReportsPage from "./pages/admin/AuditReportsPage";
import QueriesPage from "./pages/admin/QueriesPage";
import AnnouncementsPage from "./pages/admin/AnnouncementsPage";
import ActivityLogPage from "./pages/admin/ActivityLogPage";

// Resident Pages
import ResidentDashboardPage from "./pages/resident/ResidentDashboardPage";
import FinancialTransparencyPage from "./pages/resident/FinancialTransparencyPage";
import MyReceiptsPage from "./pages/resident/MyReceiptsPage";
import MyQueriesPage from "./pages/resident/MyQueriesPage";

const theme = createTheme({
  palette: {
    primary: { main: "#1976d2" },
    secondary: { main: "#388e3c" },
  },
  typography: {
    fontFamily: "Inter, Roboto, sans-serif",
  },
  components: {
    MuiCard: { defaultProps: { elevation: 0 } },
    MuiButton: { defaultProps: { disableElevation: true } },
  },
});

function PrivateRoute({ children, allowedRoles }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={user.role === "ADMIN" ? "/admin" : "/resident"} replace />;
  }
  return children;
}

function AppRoutes() {
  const { user, loading } = useAuth();
  if (loading) return null;

  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={user
        ? <Navigate to={user.role === "ADMIN" ? "/admin" : "/resident"} replace />
        : <LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />

      {/* Admin */}
      <Route path="/admin" element={
        <PrivateRoute allowedRoles={["ADMIN"]}><AdminLayout /></PrivateRoute>
      }>
        <Route index element={<DashboardPage />} />
        <Route path="society" element={<SocietyManagementPage />} />
        <Route path="expenses" element={<ExpensesPage />} />
        <Route path="statements/upload" element={<StatementUploadPage />} />
        <Route path="statements/unmatched" element={<UnmatchedTransactionsPage />} />
        <Route path="audit" element={<AuditReportsPage />} />
        <Route path="queries" element={<QueriesPage />} />
        <Route path="announcements" element={<AnnouncementsPage />} />
        <Route path="activity-log" element={<ActivityLogPage />} />
      </Route>

      {/* Resident */}
      <Route path="/resident" element={
        <PrivateRoute allowedRoles={["RESIDENT"]}><ResidentLayout /></PrivateRoute>
      }>
        <Route index element={<ResidentDashboardPage />} />
        <Route path="finances" element={<FinancialTransparencyPage />} />
        <Route path="receipts" element={<MyReceiptsPage />} />
        <Route path="queries" element={<MyQueriesPage />} />
      </Route>

      {/* Default redirect */}
      <Route path="/" element={
        user ? <Navigate to={user.role === "ADMIN" ? "/admin" : "/resident"} replace />
             : <Navigate to="/login" replace />
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}
