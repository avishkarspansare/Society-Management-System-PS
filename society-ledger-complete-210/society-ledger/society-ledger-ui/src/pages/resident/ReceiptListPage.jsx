import { useEffect, useState } from 'react'
import {
  Box, Card, CardContent, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, IconButton, Tooltip,
  CircularProgress, Alert, Chip, Pagination
} from '@mui/material'
import { Download, Receipt } from '@mui/icons-material'
import { receiptApi } from '../../api/financeApi'
import { useAuth } from '../../auth/AuthContext'
import { formatINR, formatMonthYear } from '../../utils/formatters'

export default function ReceiptListPage() {
  const { user } = useAuth()
  const [receipts, setReceipts] = useState([])
  const [total, setTotal]       = useState(1)
  const [page, setPage]         = useState(1)
  const [loading, setLoading]   = useState(true)
  const [downloading, setDownloading] = useState(null)
  const [error, setError]       = useState('')

  useEffect(() => {
    if (!user?.societyId || !user?.flatId) return
    setLoading(true)
    receiptApi.getMyReceipts(user.societyId, user.flatId, { page: page - 1, size: 15 })
      .then(res => {
        const data = res.data.data
        setReceipts(data.content || [])
        setTotal(data.totalPages || 1)
      })
      .catch(err => setError(err.response?.data?.error || 'Failed to load receipts.'))
      .finally(() => setLoading(false))
  }, [user, page])

  const handleDownload = async (receipt) => {
    setDownloading(receipt.id)
    try {
      const res = await receiptApi.downloadReceipt(user.societyId, receipt.id)
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url
      a.download = `${receipt.receiptNumber}.pdf`
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)
    } catch {
      setError('Failed to download receipt.')
    } finally { setDownloading(null) }
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Box display="flex" alignItems="center" gap={1.5} mb={3}>
        <Receipt color="primary" sx={{ fontSize: 28 }} />
        <Box>
          <Typography variant="h5" fontWeight={700}>My Receipts</Typography>
          <Typography variant="body2" color="text.secondary">
            Download your maintenance payment receipts
          </Typography>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Receipt No.</TableCell>
                <TableCell>Payment For</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell>Generated On</TableCell>
                <TableCell align="center">Download</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={36} />
                  </TableCell>
                </TableRow>
              ) : receipts.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    <Typography variant="body2" color="text.secondary">
                      No receipts found. Receipts appear after payments are matched.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                receipts.map(r => (
                  <TableRow key={r.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={600} color="primary.main">
                        {r.receiptNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500}>
                        {formatMonthYear(r.paymentMonth, r.paymentYear)}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" fontWeight={600} color="success.main">
                        {formatINR(r.amount)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {r.generatedAt ? new Date(r.generatedAt).toLocaleDateString('en-IN') : '—'}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      {r.hasPdf ? (
                        <Tooltip title="Download PDF Receipt">
                          <IconButton
                            size="small" color="primary"
                            onClick={() => handleDownload(r)}
                            disabled={downloading === r.id}
                          >
                            {downloading === r.id
                              ? <CircularProgress size={18} />
                              : <Download />}
                          </IconButton>
                        </Tooltip>
                      ) : (
                        <Chip label="Generating..." size="small" color="warning" />
                      )}
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
