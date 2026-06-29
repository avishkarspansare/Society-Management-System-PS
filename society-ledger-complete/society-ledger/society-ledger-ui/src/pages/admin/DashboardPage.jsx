import { useEffect, useState } from 'react'
import {
  Box, Grid, Card, CardContent, Typography, CircularProgress,
  Alert, Chip, Divider, List, ListItem, ListItemText, Avatar
} from '@mui/material'
import {
  TrendingUp, AccountBalance, Receipt, PendingActions,
  CheckCircle, ArrowUpward, ArrowDownward
} from '@mui/icons-material'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, LineChart, Line, Legend
} from 'recharts'
import { financeApi } from '../../api/financeApi'
import { useAuth } from '../../auth/AuthContext'
import { formatINR, formatMonthYear } from '../../utils/formatters'

function StatCard({ title, value, subtitle, icon, color = 'primary', trend }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent sx={{ p: 3 }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="body2" color="text.secondary" fontWeight={500} gutterBottom>
              {title}
            </Typography>
            <Typography variant="h5" fontWeight={700} color={`${color}.main`}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary">{subtitle}</Typography>
            )}
          </Box>
          <Avatar sx={{ bgcolor: `${color}.main`, width: 48, height: 48 }}>
            {icon}
          </Avatar>
        </Box>
        {trend !== undefined && (
          <Box display="flex" alignItems="center" gap={0.5} mt={1}>
            {trend >= 0
              ? <ArrowUpward sx={{ fontSize: 14, color: 'success.main' }} />
              : <ArrowDownward sx={{ fontSize: 14, color: 'error.main' }} />}
            <Typography variant="caption" color={trend >= 0 ? 'success.main' : 'error.main'}>
              {Math.abs(trend)}% vs last month
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  )
}

export default function AdminDashboardPage() {
  const { user } = useAuth()
  const [dashboard, setDashboard] = useState(null)
  const [summary, setSummary]     = useState([])
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState('')

  useEffect(() => {
    const sid = user?.societyId
    if (!sid) return
    Promise.all([
      financeApi.getDashboard(sid),
      financeApi.getMonthlySummary(sid),
    ])
      .then(([dashRes, sumRes]) => {
        setDashboard(dashRes.data.data)
        setSummary(sumRes.data.data?.content || [])
      })
      .catch(err => setError(err.response?.data?.error || 'Failed to load dashboard.'))
      .finally(() => setLoading(false))
  }, [user])

  if (loading) return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
      <CircularProgress size={48} />
    </Box>
  )

  if (error) return (
    <Box p={4}><Alert severity="error">{error}</Alert></Box>
  )

  const d = dashboard || {}

  // Prepare chart data — last 6 months from summary
  const chartData = [...summary]
    .slice(0, 6)
    .reverse()
    .map(m => ({
      name: formatMonthYear(m.month, m.year).slice(0, 3),
      Income: Number(m.totalIncome || 0),
      Expenses: Number(m.totalExpenses || 0),
      Balance: Number(m.closingBalance || 0),
    }))

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      {/* Page Header */}
      <Box mb={3}>
        <Typography variant="h5" fontWeight={700}>Admin Dashboard</Typography>
        <Typography variant="body2" color="text.secondary">
          Financial overview for your society
        </Typography>
      </Box>

      {/* Stat Cards */}
      <Grid container spacing={2.5} mb={3}>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="This Month's Income"
            value={formatINR(d.currentMonthIncome)}
            subtitle={formatMonthYear(d.currentMonth, d.currentYear)}
            icon={<TrendingUp />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="This Month's Expenses"
            value={formatINR(d.currentMonthExpenses)}
            icon={<AccountBalance />}
            color="error"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Current Balance"
            value={formatINR(d.currentMonthBalance)}
            subtitle="Income − Expenses"
            icon={<Receipt />}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Pending Payments"
            value={d.pendingFlats ?? '—'}
            subtitle="Flats yet to pay"
            icon={<PendingActions />}
            color="warning"
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} mb={3}>
        {/* Income vs Expenses Bar Chart */}
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} mb={2}>
                Income vs Expenses — Last 6 Months
              </Typography>
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={chartData} barGap={4}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 11 }}
                    tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
                  <Tooltip formatter={v => formatINR(v)} />
                  <Legend />
                  <Bar dataKey="Income" fill="#1e40af" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Expenses" fill="#dc2626" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Balance Trend */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} mb={2}>Balance Trend</Typography>
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
                  <Tooltip formatter={v => formatINR(v)} />
                  <Line type="monotone" dataKey="Balance" stroke="#059669"
                    strokeWidth={2.5} dot={{ fill: '#059669', r: 4 }} />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Expenses */}
      <Grid container spacing={2.5}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} mb={2}>Recent Published Expenses</Typography>
              {d.recentExpenses?.length > 0 ? (
                <List disablePadding>
                  {d.recentExpenses.map((exp, i) => (
                    <Box key={exp.expenseId}>
                      <ListItem disablePadding sx={{ py: 1.25 }}>
                        <ListItemText
                          primary={exp.vendorName}
                          secondary={`${exp.categoryName} · ${exp.expenseDate}`}
                          primaryTypographyProps={{ fontWeight: 500, variant: 'body2' }}
                          secondaryTypographyProps={{ variant: 'caption' }}
                        />
                        <Typography variant="body2" fontWeight={600} color="error.main">
                          {formatINR(exp.amount)}
                        </Typography>
                      </ListItem>
                      {i < d.recentExpenses.length - 1 && <Divider />}
                    </Box>
                  ))}
                </List>
              ) : (
                <Typography variant="body2" color="text.secondary">No published expenses yet.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* YTD Summary */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} mb={2}>Year-to-Date Summary</Typography>
              <Grid container spacing={2}>
                {[
                  { label: 'YTD Income',   value: formatINR(d.ytdIncome),    color: 'primary' },
                  { label: 'YTD Expenses', value: formatINR(d.ytdExpenses),  color: 'error' },
                  { label: 'YTD Balance',  value: formatINR(d.ytdBalance),   color: 'success' },
                ].map(item => (
                  <Grid item xs={12} key={item.label}>
                    <Box display="flex" justifyContent="space-between" alignItems="center"
                      sx={{ p: 2, bgcolor: `${item.color}.50`, borderRadius: 2,
                        border: '1px solid', borderColor: `${item.color}.100` }}>
                      <Typography variant="body2" fontWeight={500}>{item.label}</Typography>
                      <Typography variant="subtitle1" fontWeight={700} color={`${item.color}.main`}>
                        {item.value}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}
