import { useState } from 'react'
import { Outlet, useNavigate, useLocation, Link as RouterLink } from 'react-router-dom'
import {
  Box, Drawer, AppBar, Toolbar, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, Typography, IconButton, Avatar,
  Divider, Tooltip, Menu, MenuItem, Chip, useMediaQuery, useTheme
} from '@mui/material'
import {
  Dashboard, AccountBalance, Apartment, Receipt,
  Description, QuestionAnswer, Campaign, Assessment,
  Upload, Menu as MenuIcon, Logout, Settings,
  Person, BarChart, TrendingUp
} from '@mui/icons-material'
import { useAuth } from '../../auth/AuthContext'

const DRAWER_WIDTH = 256

const NAV_ITEMS = [
  { label: 'Dashboard',          icon: <Dashboard />,       path: '/admin' },
  { label: 'Society & Flats',    icon: <Apartment />,       path: '/admin/society' },
  { label: 'Expenses',           icon: <AccountBalance />,  path: '/admin/expenses' },
  { label: 'Bank Statements',    icon: <Upload />,          path: '/admin/statements' },
  { label: 'Unmatched TXNs',     icon: <Assessment />,      path: '/admin/unmatched' },
  { label: 'Receipts',           icon: <Receipt />,         path: '/admin/receipts' },
  { label: 'Monthly Summary',    icon: <BarChart />,        path: '/admin/summary' },
  { label: 'Audit Reports',      icon: <Description />,     path: '/admin/audit' },
  { label: 'Queries',            icon: <QuestionAnswer />,  path: '/admin/queries' },
  { label: 'Announcements',      icon: <Campaign />,        path: '/admin/announcements' },
  { label: 'Activity Log',       icon: <TrendingUp />,      path: '/admin/activity-log' },
]

export default function AdminLayout() {
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [mobileOpen, setMobileOpen] = useState(false)
  const [anchorEl, setAnchorEl]     = useState(null)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Brand Header */}
      <Box sx={{ p: 2.5, bgcolor: 'primary.main' }}>
        <Box display="flex" alignItems="center" gap={1.5}>
          <AccountBalance sx={{ color: 'white', fontSize: 28 }} />
          <Box>
            <Typography variant="subtitle1" fontWeight={700} color="white" lineHeight={1.2}>
              Society Ledger
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.75)' }}>
              Admin Portal
            </Typography>
          </Box>
        </Box>
      </Box>

      <Divider />

      {/* Nav Items */}
      <List sx={{ flex: 1, py: 1, px: 1 }}>
        {NAV_ITEMS.map(({ label, icon, path }) => {
          const active = location.pathname === path ||
            (path !== '/admin' && location.pathname.startsWith(path))
          return (
            <ListItem key={path} disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                component={RouterLink}
                to={path}
                selected={active}
                sx={{
                  borderRadius: 2,
                  '&.Mui-selected': {
                    bgcolor: 'primary.main',
                    color: 'white',
                    '& .MuiListItemIcon-root': { color: 'white' },
                    '&:hover': { bgcolor: 'primary.dark' },
                  },
                }}
                onClick={() => isMobile && setMobileOpen(false)}
              >
                <ListItemIcon sx={{ minWidth: 38, color: active ? 'inherit' : 'text.secondary' }}>
                  {icon}
                </ListItemIcon>
                <ListItemText
                  primary={label}
                  primaryTypographyProps={{ fontSize: '0.875rem', fontWeight: active ? 600 : 400 }}
                />
              </ListItemButton>
            </ListItem>
          )
        })}
      </List>

      <Divider />

      {/* User Footer */}
      <Box sx={{ p: 2 }}>
        <Box display="flex" alignItems="center" gap={1.5}>
          <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: '0.875rem' }}>
            {user?.email?.charAt(0).toUpperCase()}
          </Avatar>
          <Box flex={1} minWidth={0}>
            <Typography variant="body2" fontWeight={600} noWrap>{user?.email}</Typography>
            <Chip label="ADMIN" size="small" color="primary" sx={{ height: 18, fontSize: '0.65rem' }} />
          </Box>
          <Tooltip title="Logout">
            <IconButton size="small" onClick={handleLogout} color="error">
              <Logout fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Mobile AppBar */}
      {isMobile && (
        <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
          <Toolbar>
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(o => !o)} sx={{ mr: 2 }}>
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" fontWeight={700} flex={1}>Society Ledger</Typography>
          </Toolbar>
        </AppBar>
      )}

      {/* Desktop Drawer */}
      {!isMobile && (
        <Drawer
          variant="permanent"
          sx={{
            width: DRAWER_WIDTH,
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, border: 'none',
              boxShadow: '2px 0 8px rgba(0,0,0,0.06)' },
          }}
        >
          {drawer}
        </Drawer>
      )}

      {/* Mobile Drawer */}
      {isMobile && (
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}
        >
          {drawer}
        </Drawer>
      )}

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flex: 1,
          ml: isMobile ? 0 : `${DRAWER_WIDTH}px`,
          mt: isMobile ? '64px' : 0,
          bgcolor: 'background.default',
          minHeight: '100vh',
        }}
      >
        <Outlet />
      </Box>
    </Box>
  )
}
