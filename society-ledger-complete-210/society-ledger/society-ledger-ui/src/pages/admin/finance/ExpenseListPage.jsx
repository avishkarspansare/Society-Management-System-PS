import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Button, Card, CardContent, Typography, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Chip, IconButton,
  TextField, MenuItem, Select, FormControl, InputLabel, Alert,
  CircularProgress, Tooltip, Pagination, Stack
} from '@mui/material'
import {
  Add, CloudUpload, Publish, Archive, Visibility,
  CheckCircle, HourglassEmpty, Inventory
} from '@mui/icons-material'
import { financeApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'
import { formatINR, formatDate } from '../../../utils/formatters'

const STATUS_COLORS = { DRAFT: 'default', PUBLISHED: 'success', ARCHIVED: 'warning' }
const STATUS_ICONS  = {
  DRAFT: <HourglassEmpty fontSize="small" />,
  PUBLISHED: <CheckCircle fontSize="small" />,
  ARCHIVED: <Inventory fontSize="small" />,
}

export default function ExpenseListPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [expenses, setExpenses]   = useState([])
  const [total, setTotal]         = useState(0)
  const [page, setPage]           = useState(1)
  const [status, setStatus]       = useState('')
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState('')
  const [actionLoading, setActionLoading] = useState(null)

  const loadExpenses = useCallback(async () => {
    if (!user?.societyId) return
    setLoading(true); setError('')
    try {
      const res = await financeApi.getExpenses(user.societyId, {
        page: page - 1, size: 15, status: status || undefined
      })
      const data = res.data.data
      setExpenses(data.content || [])
      setTotal(data.totalPages || 1)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to load expenses.')
    } finally { setLoading(false) }
  }, [user, page, status])

  useEffect(() => { loadExpenses() }, [loadExpenses])

  const handlePublish = async (expenseId) => {
    setActionLoading(expenseId)
    try {
      await financeApi.publishExpense(user.societyId, expenseId)
      await loadExpenses()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to publish expense.')
    } finally { setActionLoading(null) }
  }

  const handleArchive = async (expenseId) => {
    setActionLoading(expenseId)
    try {
      await financeApi.archiveExpense(user.societyId, expenseId)
      await loadExpenses()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to archive expense.')
    } finally { setActionLoading(null) }
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h5" fontWeight={700}>Expense Management</Typography>
          <Typography variant="body2" color="text.secondary">
            Create, manage and publish society expenses
          </Typography>
        </Box>
        <Button
          variant="contained" startIcon={<Add />}
          onClick={() => navigate('/admin/expenses/new')}
        >
          New Expense
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Filters */}
      <Card sx={{ mb: 2 }}>
        <CardContent sx={{ py: 1.5, px: 2, '&:last-child': { pb: 1.5 } }}>
          <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
            <FormControl size="small" sx={{ minWidth: 160 }}>
              <InputLabel>Status</InputLabel>
              <Select value={status} label="Status" onChange={e => { setStatus(e.target.value); setPage(1) }}>
                <MenuItem value="">All Statuses</MenuItem>
                <MenuItem value="DRAFT">Draft</MenuItem>
                <MenuItem value="PUBLISHED">Published</MenuItem>
                <MenuItem value="ARCHIVED">Archived</MenuItem>
              </Select>
            </FormControl>
            <Typography variant="body2" color="text.secondary" sx={{ ml: 'auto' }}>
              {expenses.length} expenses shown
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* Table */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Date</TableCell>
                <TableCell>Vendor</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Proof</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={36} />
                  </TableCell>
                </TableRow>
              ) : expenses.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <Typography variant="body2" color="text.secondary">No expenses found.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                expenses.map(exp => (
                  <TableRow key={exp.id} hover>
                    <TableCell>
                      <Typography variant="body2">{formatDate(exp.expenseDate)}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500}>{exp.vendorName}</Typography>
                      <Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 200, display: 'block' }}>
                        {exp.description}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip label={exp.categoryName} size="small" variant="outlined" />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600} color="error.main">
                        {formatINR(exp.amount)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        icon={STATUS_ICONS[exp.status]}
                        label={exp.status}
                        size="small"
                        color={STATUS_COLORS[exp.status]}
                      />
                    </TableCell>
                    <TableCell>
                      {exp.hasProof
                        ? <Chip label="Uploaded" size="small" color="success" variant="outlined" />
                        : <Chip label="Missing" size="small" color="error" variant="outlined" />}
                    </TableCell>
                    <TableCell align="right">
                      <Box display="flex" gap={0.5} justifyContent="flex-end">
                        <Tooltip title="View Details">
                          <IconButton size="small"
                            onClick={() => navigate(`/admin/expenses/${exp.id}`)}>
                            <Visibility fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {exp.status === 'DRAFT' && exp.hasProof && (
                          <Tooltip title="Publish">
                            <IconButton size="small" color="success"
                              onClick={() => handlePublish(exp.id)}
                              disabled={actionLoading === exp.id}>
                              {actionLoading === exp.id
                                ? <CircularProgress size={16} />
                                : <Publish fontSize="small" />}
                            </IconButton>
                          </Tooltip>
                        )}
                        {exp.status === 'PUBLISHED' && (
                          <Tooltip title="Archive">
                            <IconButton size="small" color="warning"
                              onClick={() => handleArchive(exp.id)}
                              disabled={actionLoading === exp.id}>
                              {actionLoading === exp.id
                                ? <CircularProgress size={16} />
                                : <Archive fontSize="small" />}
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {total > 1 && (
          <Box display="flex" justifyContent="center" py={2}>
            <Pagination count={total} page={page} onChange={(_, v) => setPage(v)} color="primary" />
          </Box>
        )}
      </Card>
    </Box>
  )
}
