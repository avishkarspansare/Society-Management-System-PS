import { useState } from 'react'
import { Outlet, useNavigate, useLocation, Link as RouterLink } from 'react-router-dom'
import {
  Box, Drawer, AppBar, Toolbar, List, ListItem, ListItemButton,
  ListItemIcon, ListItemText, Typography, IconButton, Avatar,
  Divider, Tooltip, useMediaQuery, useTheme, Chip
} from '@mui/material'
import {
  Dashboard, AccountBalance, Receipt, Description,
  QuestionAnswer, Campaign, People, Menu as MenuIcon,
  Logout, TrendingUp
} from '@mui/icons-material'
import { useAuth } from '../../auth/AuthContext'

const DRAWER_WIDTH = 248

const NAV_ITEMS = [
  { label: 'My Dashboard',         icon: <Dashboard />,       path: '/resident' },
  { label: 'Financial Overview',   icon: <TrendingUp />,      path: '/resident/financials' },
  { label: 'Expenses',             icon: <AccountBalance />,  path: '/resident/expenses' },
  { label: 'My Receipts',          icon: <Receipt />,         path: '/resident/receipts' },
  { label: 'Audit Reports',        icon: <Description />,     path: '/resident/audit' },
  { label: 'Public Queries',       icon: <QuestionAnswer />,  path: '/resident/queries' },
  { label: 'Announcements',        icon: <Campaign />,        path: '/resident/announcements' },
  { label: 'My Family',            icon: <People />,          path: '/resident/family' },
]

export default function ResidentLayout() {
  const theme    = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const { user, logout } = useAuth()
  const navigate  = useNavigate()
  const location  = useLocation()

  const [mobileOpen, setMobileOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2.5, bgcolor: 'secondary.main' }}>
        <Box display="flex" alignItems="center" gap={1.5}>
          <AccountBalance sx={{ color: 'white', fontSize: 26 }} />
          <Box>
            <Typography variant="subtitle1" fontWeight={700} color="white" lineHeight={1.2}>
              Society Ledger
            </Typography>
            <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.75)' }}>
              Resident Portal
            </Typography>
          </Box>
        </Box>
      </Box>

      <Divider />

      <List sx={{ flex: 1, py: 1, px: 1 }}>
        {NAV_ITEMS.map(({ label, icon, path }) => {
          const active = location.pathname === path ||
            (path !== '/resident' && location.pathname.startsWith(path))
          return (
            <ListItem key={path} disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                component={RouterLink}
                to={path}
                selected={active}
                sx={{
                  borderRadius: 2,
                  '&.Mui-selected': {
                    bgcolor: 'secondary.main', color: 'white',
                    '& .MuiListItemIcon-root': { color: 'white' },
                    '&:hover': { bgcolor: 'secondary.dark' },
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

      <Box sx={{ p: 2 }}>
        <Box display="flex" alignItems="center" gap={1.5}>
          <Avatar sx={{ width: 36, height: 36, bgcolor: 'secondary.main', fontSize: '0.875rem' }}>
            {user?.email?.charAt(0).toUpperCase()}
          </Avatar>
          <Box flex={1} minWidth={0}>
            <Typography variant="body2" fontWeight={600} noWrap>{user?.email}</Typography>
            <Chip label="RESIDENT" size="small" color="secondary"
              sx={{ height: 18, fontSize: '0.65rem' }} />
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
      {isMobile && (
        <AppBar position="fixed" color="secondary" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
          <Toolbar>
            <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(o => !o)} sx={{ mr: 2 }}>
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" fontWeight={700} flex={1}>Society Ledger</Typography>
          </Toolbar>
        </AppBar>
      )}

      {!isMobile && (
        <Drawer variant="permanent"
          sx={{ width: DRAWER_WIDTH, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, border: 'none',
            boxShadow: '2px 0 8px rgba(0,0,0,0.06)' } }}>
          {drawer}
        </Drawer>
      )}

      {isMobile && (
        <Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}>
          {drawer}
        </Drawer>
      )}

      <Box component="main" sx={{
        flex: 1, ml: isMobile ? 0 : `${DRAWER_WIDTH}px`,
        mt: isMobile ? '64px' : 0, bgcolor: 'background.default', minHeight: '100vh',
      }}>
        <Outlet />
      </Box>
    </Box>
  )
}
