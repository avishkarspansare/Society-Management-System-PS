import { useEffect, useState } from 'react'
import {
  Box, Card, CardContent, Typography, Grid, CircularProgress,
  Alert, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Chip, Divider, LinearProgress, Tooltip
} from '@mui/material'
import {
  TrendingUp, AccountBalance, PendingActions, Verified,
  InfoOutlined
} from '@mui/icons-material'
import {
  PieChart, Pie, Cell, Tooltip as RTooltip, Legend,
  ResponsiveContainer
} from 'recharts'
import { financeApi } from '../../api/financeApi'
import { useAuth } from '../../auth/AuthContext'
import { formatINR, formatMonthYear } from '../../utils/formatters'

const CHART_COLORS = ['#1e40af', '#dc2626', '#059669', '#d97706', '#7c3aed', '#0891b2']

export default function FinancialTransparencyPage() {
  const { user } = useAuth()
  const [dashboard, setDashboard]   = useState(null)
  const [summary, setSummary]       = useState([])
  const [timeline, setTimeline]     = useState([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState('')

  useEffect(() => {
    const sid = user?.societyId
    if (!sid) return
    Promise.all([
      financeApi.getDashboard(sid),
      financeApi.getMonthlySummary(sid),
      financeApi.getTimeline(sid, { page: 0, size: 10 }),
    ])
      .then(([d, s, t]) => {
        setDashboard(d.data.data)
        setSummary(s.data.data?.content || [])
        setTimeline(t.data.data?.content || [])
      })
      .catch(err => setError(err.response?.data?.error || 'Failed to load financial data.'))
      .finally(() => setLoading(false))
  }, [user])

  if (loading) return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
      <CircularProgress size={48} />
    </Box>
  )

  if (error) return <Box p={4}><Alert severity="error">{error}</Alert></Box>

  const d = dashboard || {}

  // Pie chart data for expense categories
  const expenseData = (d.recentExpenses || [])
    .reduce((acc, e) => {
      const existing = acc.find(a => a.name === e.categoryName)
      if (existing) existing.value += Number(e.amount)
      else acc.push({ name: e.categoryName, value: Number(e.amount) })
      return acc
    }, [])

  const totalIncome = Number(d.ytdIncome || 0)
  const totalExp    = Number(d.ytdExpenses || 0)
  const balance     = totalIncome - totalExp
  const expPercent  = totalIncome > 0 ? Math.round((totalExp / totalIncome) * 100) : 0

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Box mb={3}>
        <Typography variant="h5" fontWeight={700}>Financial Transparency</Typography>
        <Typography variant="body2" color="text.secondary">
          Complete financial overview of your society — no secrets.
        </Typography>
      </Box>

      {/* Key Metrics */}
      <Grid container spacing={2.5} mb={3}>
        {[
          { label: 'YTD Total Collection', value: formatINR(d.ytdIncome), icon: <TrendingUp />, color: 'primary' },
          { label: 'YTD Total Expenses',   value: formatINR(d.ytdExpenses), icon: <AccountBalance />, color: 'error' },
          { label: 'Current Balance',       value: formatINR(d.ytdBalance), icon: <Verified />, color: 'success' },
          { label: 'Pending This Month',    value: `${d.pendingFlats ?? 0} flats`, icon: <PendingActions />, color: 'warning' },
        ].map(item => (
          <Grid item xs={12} sm={6} lg={3} key={item.label}>
            <Card>
              <CardContent sx={{ p: 2.5 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Box>
                    <Typography variant="caption" color="text.secondary" fontWeight={500}>
                      {item.label}
                    </Typography>
                    <Typography variant="h6" fontWeight={700} color={`${item.color}.main`} mt={0.25}>
                      {item.value}
                    </Typography>
                  </Box>
                  <Box sx={{ color: `${item.color}.main`, opacity: 0.7 }}>{item.icon}</Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Spending Health */}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ p: 3 }}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={1.5}>
            <Typography variant="h6" fontWeight={600}>Spending Health</Typography>
            <Chip
              label={expPercent <= 70 ? '✓ Healthy' : expPercent <= 90 ? '⚠ Watch' : '✗ Over-budget'}
              color={expPercent <= 70 ? 'success' : expPercent <= 90 ? 'warning' : 'error'}
              size="small"
            />
          </Box>
          <Box display="flex" alignItems="center" gap={1} mb={0.75}>
            <Typography variant="body2" color="text.secondary">
              {expPercent}% of collected funds spent
            </Typography>
            <Tooltip title="Ideally, expenses should be below 80% of collections to maintain a healthy buffer.">
              <InfoOutlined sx={{ fontSize: 14, color: 'text.secondary', cursor: 'help' }} />
            </Tooltip>
          </Box>
          <LinearProgress
            variant="determinate"
            value={Math.min(expPercent, 100)}
            color={expPercent <= 70 ? 'success' : expPercent <= 90 ? 'warning' : 'error'}
            sx={{ height: 10, borderRadius: 5 }}
          />
          <Box display="flex" justifyContent="space-between" mt={0.75}>
            <Typography variant="caption" color="text.secondary">₹0</Typography>
            <Typography variant="caption" color="text.secondary">{formatINR(totalIncome)}</Typography>
          </Box>
        </CardContent>
      </Card>

      <Grid container spacing={2.5} mb={3}>
        {/* Monthly Summary Table */}
        <Grid item xs={12} lg={7}>
          <Card>
            <CardContent sx={{ p: 0 }}>
              <Typography variant="h6" fontWeight={600} sx={{ p: 2.5, pb: 1.5 }}>
                Monthly Financial Summary
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Month</TableCell>
                      <TableCell align="right">Income</TableCell>
                      <TableCell align="right">Expenses</TableCell>
                      <TableCell align="right">Balance</TableCell>
                      <TableCell align="center">Pending</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {summary.slice(0, 12).map(row => (
                      <TableRow key={`${row.year}-${row.month}`} hover>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500}>
                            {formatMonthYear(row.month, row.year)}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" color="primary.main" fontWeight={500}>
                            {formatINR(row.totalIncome)}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" color="error.main" fontWeight={500}>
                            {formatINR(row.totalExpenses)}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography
                            variant="body2" fontWeight={600}
                            color={Number(row.closingBalance) >= 0 ? 'success.main' : 'error.main'}
                          >
                            {formatINR(row.closingBalance)}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <Chip label={row.pendingFlats} size="small"
                            color={row.pendingFlats === 0 ? 'success' : 'warning'} />
                        </TableCell>
                      </TableRow>
                    ))}
                    {summary.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                          <Typography variant="body2" color="text.secondary">
                            No monthly data yet.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Expense Breakdown Pie */}
        <Grid item xs={12} lg={5}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" fontWeight={600} mb={2}>
                Expense Breakdown (Recent)
              </Typography>
              {expenseData.length > 0 ? (
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie data={expenseData} cx="50%" cy="50%" outerRadius={90}
                      dataKey="value" nameKey="name" label={({ name, percent }) =>
                        `${name} ${(percent * 100).toFixed(0)}%`}
                      labelLine={false}>
                      {expenseData.map((_, idx) => (
                        <Cell key={idx} fill={CHART_COLORS[idx % CHART_COLORS.length]} />
                      ))}
                    </Pie>
                    <RTooltip formatter={v => formatINR(v)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Box display="flex" alignItems="center" justifyContent="center" height={260}>
                  <Typography variant="body2" color="text.secondary">
                    No expense data yet.
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Transparency Timeline */}
      <Card>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" fontWeight={600} mb={2}>
            Transparency Timeline
          </Typography>
          {timeline.length > 0 ? (
            <Box>
              {timeline.map((event, i) => (
                <Box key={event.id}>
                  <Box display="flex" gap={2} py={1.5}>
                    <Box sx={{ width: 10, height: 10, borderRadius: '50%',
                      bgcolor: 'primary.main', mt: 0.75, flexShrink: 0 }} />
                    <Box flex={1}>
                      <Typography variant="body2" fontWeight={500}>
                        {event.eventSummary}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(event.occurredAt).toLocaleString()}
                      </Typography>
                    </Box>
                    <Chip label={event.eventType.replace(/_/g, ' ')} size="small"
                      variant="outlined" sx={{ alignSelf: 'center', fontSize: '0.7rem' }} />
                  </Box>
                  {i < timeline.length - 1 && (
                    <Divider sx={{ ml: 3.5 }} />
                  )}
                </Box>
              ))}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No timeline events yet.
            </Typography>
          )}
        </CardContent>
      </Card>
    </Box>
  )
}
