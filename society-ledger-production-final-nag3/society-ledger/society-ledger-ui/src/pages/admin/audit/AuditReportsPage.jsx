import { useState, useEffect } from 'react'
import {
  Box, Typography, Button, Card, CardContent,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, Chip, CircularProgress, LinearProgress
} from '@mui/material'
import { Upload, Download } from '@mui/icons-material'
import { useSnackbar } from 'notistack'
import { auditApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'

const STATUS_COLORS = { COMPLIANT: 'success', NON_COMPLIANT: 'error', PENDING: 'warning' }

export default function AuditReportsPage() {
  const { user } = useAuth()
  const { enqueueSnackbar } = useSnackbar()
  const societyId = user?.societyId

  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState({ auditorName: '', auditorFirm: '', auditYear: new Date().getFullYear(), complianceStatus: 'PENDING', remarks: '' })
  const [file, setFile] = useState(null)

  const load = async () => {
    try {
      setLoading(true)
      const res = await auditApi.getReports(societyId)
      setReports(res.data.data || [])
    } catch {
      enqueueSnackbar('Failed to load audit reports', { variant: 'error' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [societyId])

  const handleUpload = async () => {
    if (!file) { enqueueSnackbar('Please select a PDF file', { variant: 'warning' }); return }
    try {
      setUploading(true)
      const fd = new FormData()
      fd.append('file', file)
      fd.append('auditorName', form.auditorName)
      fd.append('auditorFirm', form.auditorFirm)
      fd.append('auditYear', form.auditYear)
      fd.append('complianceStatus', form.complianceStatus)
      fd.append('remarks', form.remarks)
      await auditApi.uploadReport(societyId, fd)
      enqueueSnackbar('Audit report uploaded', { variant: 'success' })
      setDialog(false)
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Upload failed', { variant: 'error' })
    } finally {
      setUploading(false)
    }
  }

  const handleDownload = async (reportId, fileName) => {
    try {
      const res = await auditApi.downloadReport(societyId, reportId)
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const a = document.createElement('a'); a.href = url; a.download = fileName || 'audit-report.pdf'; a.click()
      URL.revokeObjectURL(url)
    } catch {
      enqueueSnackbar('Download failed', { variant: 'error' })
    }
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={600}>Audit Reports</Typography>
        {user?.role === 'ADMIN' && (
          <Button variant="contained" startIcon={<Upload />} onClick={() => setDialog(true)}>
            Upload Report
          </Button>
        )}
      </Box>

      {loading ? <LinearProgress /> : (
        <Card>
          <CardContent>
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Year</TableCell>
                    <TableCell>Auditor</TableCell>
                    <TableCell>Firm</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Uploaded At</TableCell>
                    <TableCell>Report</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {reports.map((r) => (
                    <TableRow key={r.id} hover>
                      <TableCell><strong>{r.auditYear}</strong></TableCell>
                      <TableCell>{r.auditorName}</TableCell>
                      <TableCell>{r.auditorFirm || '—'}</TableCell>
                      <TableCell>
                        <Chip size="small" label={r.complianceStatus}
                          color={STATUS_COLORS[r.complianceStatus] || 'default'} />
                      </TableCell>
                      <TableCell>{r.uploadedAt ? new Date(r.uploadedAt).toLocaleDateString('en-IN') : '—'}</TableCell>
                      <TableCell>
                        {r.reportFileName && (
                          <Button size="small" startIcon={<Download />}
                            onClick={() => handleDownload(r.id, r.reportFileName)}>
                            Download
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                  {reports.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography color="text.secondary">No audit reports uploaded yet.</Typography>
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
        <DialogTitle>Upload Audit Report</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={6}>
              <TextField label="Auditor Name" fullWidth required
                value={form.auditorName} onChange={(e) => setForm({ ...form, auditorName: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Auditor Firm" fullWidth
                value={form.auditorFirm} onChange={(e) => setForm({ ...form, auditorFirm: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Audit Year" type="number" fullWidth required
                value={form.auditYear} onChange={(e) => setForm({ ...form, auditYear: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField select label="Status" fullWidth required
                value={form.complianceStatus} onChange={(e) => setForm({ ...form, complianceStatus: e.target.value })}>
                {['COMPLIANT','NON_COMPLIANT','PENDING'].map(s => <MenuItem key={s} value={s}>{s}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField label="Remarks" multiline rows={2} fullWidth
                value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <Button component="label" variant="outlined" fullWidth>
                {file ? file.name : 'Select PDF Report'}
                <input type="file" hidden accept=".pdf" onChange={(e) => setFile(e.target.files[0])} />
              </Button>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleUpload} variant="contained" disabled={uploading || !form.auditorName}>
            {uploading ? <CircularProgress size={20} /> : 'Upload'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
