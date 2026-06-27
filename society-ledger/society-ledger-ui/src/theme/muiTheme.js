import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: {
      main: '#1e40af',      // deep blue — trust, finance
      light: '#3b82f6',
      dark: '#1e3a8a',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#059669',      // emerald green — growth, money
      light: '#10b981',
      dark: '#047857',
      contrastText: '#ffffff',
    },
    error:   { main: '#dc2626' },
    warning: { main: '#d97706' },
    success: { main: '#16a34a' },
    info:    { main: '#0284c7' },
    background: {
      default: '#f8fafc',
      paper:   '#ffffff',
    },
    text: {
      primary:   '#111827',
      secondary: '#6b7280',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: { fontWeight: 700 },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 500 },
  },
  shape: { borderRadius: 10 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          borderRadius: 8,
        },
        contained: {
          boxShadow: 'none',
          '&:hover': { boxShadow: '0 2px 8px rgba(30,64,175,0.25)' },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
          border: '1px solid #f1f5f9',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#f8fafc',
          '& .MuiTableCell-head': {
            fontWeight: 600,
            color: '#374151',
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600, borderRadius: 6 },
      },
    },
  },
})

export default theme
