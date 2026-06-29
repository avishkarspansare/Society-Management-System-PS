import { useState, useEffect } from 'react'
import {
  Box, Typography, Card, CardContent, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Button, Dialog,
  DialogTitle, DialogContent, DialogActions, TextField, MenuItem,
  Chip, CircularProgress, LinearProgress, Alert
} from '@mui/material'
import { LinkOff, Link } from '@mui/icons-material'
import { useSnackbar } from 'notistack'
import { statementApi } from '../../../api/financeApi'
import { societyApi } from '../../../api/societyApi'
import { useAuth } from '../../../auth/AuthContext'

function fmt(v) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(v || 0)
}

export default function UnmatchedTransactionsPage() {
  const { user } = useAuth()
  const { enqueueSnackbar } = useSnackbar()
  const societyId = user?.societyId

  const [transactions, setTransactions] = useState([])
  const [flats, setFlats] = useState([])
  const [loading, setLoading] = useState(true)
  const [matching, setMatching] = useState(false)
  const [dialog, setDialog] = useState(false)
  const [selectedTxn, setSelectedTxn] = useState(null)
  const [matchForm, setMatchForm] = useState({ flatId: '', paymentMonth: '', paymentYear: '', paymentType: 'MAINTENANCE' })

  const load = async () => {
    try {
      setLoading(true)
      const [txnRes, flatRes] = await Promise.all([
        statementApi.getUnmatched(societyId),
        societyApi.getFlats(societyId),
      ])
      setTransactions(txnRes.data.data || [])
      setFlats(flatRes.data.data || [])
    } catch {
      enqueueSnackbar('Failed to load data', { variant: 'error' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [societyId])

  const openDialog = (txn) => {
    setSelectedTxn(txn)
    setMatchForm({ flatId: '', paymentMonth: txn.transactionDate ? new Date(txn.transactionDate).getMonth() + 1 : '', paymentYear: txn.transactionDate ? new Date(txn.transactionDate).getFullYear() : '', paymentType: 'MAINTENANCE' })
    setDialog(true)
  }

  const handleManualMatch = async () => {
    try {
      setMatching(true)
      await statementApi.manualMatch(societyId, selectedTxn.id, {
        flatId: Number(matchForm.flatId),
        paymentMonth: Number(matchForm.paymentMonth),
        paymentYear: Number(matchForm.paymentYear),
        paymentType: matchForm.paymentType,
      })
      enqueueSnackbar('Transaction matched successfully', { variant: 'success' })
      setDialog(false)
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Match failed', { variant: 'error' })
    } finally {
      setMatching(false)
    }
  }

  return (
    <Box>
      <Typography variant="h5" fontWeight={600} mb={3} display="flex" alignItems="center" gap={1}>
        <LinkOff /> Unmatched Transactions ({transactions.length})
      </Typography>

      {transactions.length > 0 && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          These credit transactions could not be auto-matched to any flat. Manually match them to generate receipts.
        </Alert>
      )}

      {loading ? <LinearProgress /> : (
        <Card>
          <CardContent>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell align="right">Amount</TableCell>
                    <TableCell>Reason</TableCell>
                    <TableCell>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.map((t) => (
                    <TableRow key={t.id} hover>
                      <TableCell>{t.transactionDate}</TableCell>
                      <TableCell sx={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {t.description}
                      </TableCell>
                      <TableCell><code>{t.referenceNumber || '—'}</code></TableCell>
                      <TableCell align="right" sx={{ color: 'success.main', fontWeight: 600 }}>
                        {fmt(t.creditAmount)}
                      </TableCell>
                      <TableCell>
                        <Chip size="small" label={t.reason || 'No match'} color="warning" />
                      </TableCell>
                      <TableCell>
                        <Button size="small" startIcon={<Link />} variant="outlined"
                          onClick={() => openDialog(t)}>
                          Match
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                  {transactions.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography color="text.secondary">No unmatched transactions. 🎉</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Manual Match — {fmt(selectedTxn?.creditAmount)}</DialogTitle>
        <DialogContent>
          {selectedTxn && (
            <Box mb={2} p={1.5} bgcolor="grey.50" borderRadius={1}>
              <Typography variant="caption" color="text.secondary">Transaction</Typography>
              <Typography variant="body2">{selectedTxn.description}</Typography>
              <Typography variant="caption">Ref: {selectedTxn.referenceNumber || '—'} | Date: {selectedTxn.transactionDate}</Typography>
            </Box>
          )}
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField select label="Flat" required fullWidth
              value={matchForm.flatId} onChange={(e) => setMatchForm({ ...matchForm, flatId: e.target.value })}>
              {flats.map(f => <MenuItem key={f.id} value={f.id}>Flat {f.flatNumber} ({f.paymentReferenceCode})</MenuItem>)}
            </TextField>
            <Box display="flex" gap={2}>
              <TextField label="Month" type="number" required inputProps={{ min: 1, max: 12 }}
                value={matchForm.paymentMonth} onChange={(e) => setMatchForm({ ...matchForm, paymentMonth: e.target.value })} />
              <TextField label="Year" type="number" required
                value={matchForm.paymentYear} onChange={(e) => setMatchForm({ ...matchForm, paymentYear: e.target.value })} />
            </Box>
            <TextField select label="Payment Type" fullWidth
              value={matchForm.paymentType} onChange={(e) => setMatchForm({ ...matchForm, paymentType: e.target.value })}>
              {['MAINTENANCE', 'PARKING', 'WATER', 'OTHER'].map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleManualMatch} variant="contained" disabled={matching || !matchForm.flatId}>
            {matching ? <CircularProgress size={20} /> : 'Confirm Match'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
