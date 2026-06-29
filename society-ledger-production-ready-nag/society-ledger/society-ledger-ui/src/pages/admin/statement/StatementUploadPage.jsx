import { useEffect, useState } from 'react'
import {
  Box, Card, CardContent, Typography, Button, Grid, Alert,
  CircularProgress, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Paper, MenuItem, TextField
} from '@mui/material'
import { CloudUpload, CheckCircle, Error, HourglassEmpty } from '@mui/icons-material'
import { statementApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'
import { formatDate } from '../../../utils/formatters'

const BANKS = [{ value: 'BOB', label: 'Bank of Baroda' }]
const MONTHS = Array.from({ length: 12 }, (_, i) => ({
  value: i + 1, label: new Date(2000, i).toLocaleString('default', { month: 'long' })
}))
const currentYear = new Date().getFullYear()
const YEARS = Array.from({ length: 5 }, (_, i) => currentYear - i)

const STATUS_MAP = {
  COMPLETED: { label: 'Completed', color: 'success', icon: <CheckCircle fontSize="small" /> },
  PROCESSING: { label: 'Processing', color: 'warning', icon: <HourglassEmpty fontSize="small" /> },
  FAILED: { label: 'Failed', color: 'error', icon: <Error fontSize="small" /> },
}

export default function StatementUploadPage() {
  const { user } = useAuth()
  const [statements, setStatements] = useState([])
  const [loading, setLoading]       = useState(true)
  const [uploading, setUploading]   = useState(false)
  const [error, setError]           = useState('')
  const [success, setSuccess]       = useState('')
  const [file, setFile]             = useState(null)
  const [form, setForm] = useState({
    bankCode: 'BOB',
    month: new Date().getMonth() + 1,
    year: currentYear,
  })

  const loadStatements = async () => {
    try {
      const res = await statementApi.getStatements(user.societyId, { page: 0, size: 20 })
      setStatements(res.data.data?.content || [])
    } catch { setError('Failed to load statements.') }
    finally { setLoading(false) }
  }

  useEffect(() => { loadStatements() }, [user])

  const handleUpload = async (e) => {
    e.preventDefault()
    if (!file) { setError('Please select a bank statement file.'); return }
    setError(''); setSuccess(''); setUploading(true)
    try {
      await statementApi.upload(user.societyId, file, form.bankCode, form.month, form.year)
      setSuccess('Statement uploaded and processed successfully. Matching engine is running.')
      setFile(null)
      await loadStatements()
    } catch (err) {
      setError(err.response?.data?.error || 'Upload failed.')
    } finally { setUploading(false) }
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Box mb={3}>
        <Typography variant="h5" fontWeight={700}>Bank Statement Upload</Typography>
        <Typography variant="body2" color="text.secondary">
          Upload monthly bank statements. Transactions are auto-matched to flats.
        </Typography>
      </Box>

      {error   && <Alert severity="error"   sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      {/* Upload Form */}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" fontWeight={600} mb={2}>Upload New Statement</Typography>
          <form onSubmit={handleUpload}>
            <Grid container spacing={2} alignItems="flex-end">
              <Grid item xs={12} sm={4} md={3}>
                <TextField select label="Bank" fullWidth size="small"
                  value={form.bankCode} onChange={e => setForm(f => ({ ...f, bankCode: e.target.value }))}>
                  {BANKS.map(b => <MenuItem key={b.value} value={b.value}>{b.label}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={6} sm={4} md={2}>
                <TextField select label="Month" fullWidth size="small"
                  value={form.month} onChange={e => setForm(f => ({ ...f, month: e.target.value }))}>
                  {MONTHS.map(m => <MenuItem key={m.value} value={m.value}>{m.label}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={6} sm={4} md={2}>
                <TextField select label="Year" fullWidth size="small"
                  value={form.year} onChange={e => setForm(f => ({ ...f, year: e.target.value }))}>
                  {YEARS.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid item xs={12} md={3}>
                <Paper variant="outlined" sx={{ p: 1.5, borderStyle: 'dashed',
                  borderColor: file ? 'success.main' : 'divider', borderRadius: 1.5,
                  textAlign: 'center', cursor: 'pointer', bgcolor: file ? 'success.50' : 'grey.50' }}
                  component="label">
                  <input type="file" hidden accept=".csv,.xlsx,.xls"
                    onChange={e => setFile(e.target.files[0])} />
                  <Typography variant="caption" display="block" color={file ? 'success.main' : 'text.secondary'}>
                    <CloudUpload sx={{ fontSize: 18, verticalAlign: 'middle', mr: 0.5 }} />
                    {file ? file.name : 'Select CSV / Excel'}
                  </Typography>
                </Paper>
              </Grid>
              <Grid item xs={12} md={2}>
                <Button type="submit" variant="contained" fullWidth disabled={uploading}
                  startIcon={uploading ? <CircularProgress size={16} color="inherit" /> : <CloudUpload />}>
                  {uploading ? 'Uploading...' : 'Upload'}
                </Button>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

      {/* Statement History */}
      <Card>
        <CardContent sx={{ p: 0 }}>
          <Typography variant="h6" fontWeight={600} sx={{ p: 2.5, pb: 0 }}>
            Statement History
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Bank</TableCell>
                  <TableCell>Period</TableCell>
                  <TableCell>File</TableCell>
                  <TableCell>Transactions</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Uploaded</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                      <CircularProgress size={36} />
                    </TableCell>
                  </TableRow>
                ) : statements.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                      <Typography variant="body2" color="text.secondary">
                        No statements uploaded yet.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  statements.map(s => {
                    const status = STATUS_MAP[s.uploadStatus] || STATUS_MAP.PROCESSING
                    return (
                      <TableRow key={s.id} hover>
                        <TableCell>
                          <Chip label={s.bankName} size="small" variant="outlined" />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500}>
                            {MONTHS.find(m => m.value === s.statementMonth)?.label} {s.statementYear}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="text.secondary" noWrap
                            sx={{ maxWidth: 180 }}>
                            {s.fileName}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">{s.transactionCount ?? '—'}</Typography>
                        </TableCell>
                        <TableCell>
                          <Chip icon={status.icon} label={status.label}
                            size="small" color={status.color} />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="text.secondary">
                            {s.uploadedAt ? formatDate(s.uploadedAt) : '—'}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    )
                  })
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  )
}
