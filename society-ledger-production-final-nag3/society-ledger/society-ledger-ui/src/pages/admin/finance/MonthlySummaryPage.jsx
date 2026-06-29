import { useState, useEffect } from 'react'
import {
  Box, Typography, Card, CardContent, Grid, MenuItem, TextField,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Chip, CircularProgress
} from '@mui/material'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { financeApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

function fmt(v) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0)
}

export default function MonthlySummaryPage() {
  const { user } = useAuth()
  const [year, setYear] = useState(new Date().getFullYear())
  const [summaries, setSummaries] = useState([])
  const [loading, setLoading] = useState(true)

  const years = Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i)

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true)
        const res = await financeApi.getMonthlySummary(user?.societyId, year)
        setSummaries(res.data.data || [])
      } catch {
        setSummaries([])
      } finally {
        setLoading(false)
      }
    }
    if (user?.societyId) load()
  }, [user?.societyId, year])

  const chartData = summaries.map(s => ({
    name: MONTHS[s.month - 1],
    Income: Number(s.totalIncome),
    Expenses: Number(s.totalExpenses),
    Balance: Number(s.closingBalance),
  }))

  const totIncome  = summaries.reduce((a, s) => a + Number(s.totalIncome), 0)
  const totExpense = summaries.reduce((a, s) => a + Number(s.totalExpenses), 0)

  if (loading) return <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={600}>Monthly Financial Summary</Typography>
        <TextField select label="Year" size="small" value={year} onChange={(e) => setYear(e.target.value)} sx={{ width: 120 }}>
          {years.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
        </TextField>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'success.light' }}>
            <CardContent>
              <Typography variant="body2" color="success.dark">Total Income {year}</Typography>
              <Typography variant="h5" color="success.dark" fontWeight={700}>{fmt(totIncome)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'error.light' }}>
            <CardContent>
              <Typography variant="body2" color="error.dark">Total Expenses {year}</Typography>
              <Typography variant="h5" color="error.dark" fontWeight={700}>{fmt(totExpense)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Card sx={{ bgcolor: 'info.light' }}>
            <CardContent>
              <Typography variant="body2" color="info.dark">Net Surplus</Typography>
              <Typography variant="h5" color="info.dark" fontWeight={700}>{fmt(totIncome - totExpense)}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Chart */}
      {chartData.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" mb={2}>Income vs Expenses</Typography>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={chartData}>
                <XAxis dataKey="name" />
                <YAxis tickFormatter={(v) => `₹${(v/1000).toFixed(0)}k`} />
                <Tooltip formatter={(v) => fmt(v)} />
                <Legend />
                <Bar dataKey="Income"   fill="#2e7d32" radius={[4,4,0,0]} />
                <Bar dataKey="Expenses" fill="#c62828" radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      )}

      {/* Table */}
      <Card>
        <CardContent>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Month</TableCell>
                  <TableCell align="right">Income</TableCell>
                  <TableCell align="right">Expenses</TableCell>
                  <TableCell align="right">Closing Balance</TableCell>
                  <TableCell align="center">Pending Flats</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {summaries.map((s) => (
                  <TableRow key={s.id} hover>
                    <TableCell><strong>{MONTHS[s.month - 1]} {s.year}</strong></TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>{fmt(s.totalIncome)}</TableCell>
                    <TableCell align="right" sx={{ color: 'error.main'   }}>{fmt(s.totalExpenses)}</TableCell>
                    <TableCell align="right">{fmt(s.closingBalance)}</TableCell>
                    <TableCell align="center">
                      <Chip size="small" label={s.pendingFlats}
                        color={s.pendingFlats > 0 ? 'warning' : 'success'} />
                    </TableCell>
                  </TableRow>
                ))}
                {summaries.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      <Typography color="text.secondary" variant="body2">No summary data for {year}.</Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  )
}
