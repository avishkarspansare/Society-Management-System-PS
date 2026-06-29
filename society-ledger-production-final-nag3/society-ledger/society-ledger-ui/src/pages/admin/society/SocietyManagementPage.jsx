import { useState, useEffect } from 'react'
import {
  Box, Typography, Card, CardContent, Grid, Button,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, Chip, IconButton, Tooltip, CircularProgress
} from '@mui/material'
import { Add, Delete, Business, Apartment } from '@mui/icons-material'
import { useSnackbar } from 'notistack'
import { societyApi } from '../../../api/societyApi'
import { useAuth } from '../../../auth/AuthContext'

export default function SocietyManagementPage() {
  const { user } = useAuth()
  const { enqueueSnackbar } = useSnackbar()
  const societyId = user?.societyId

  const [wings, setWings] = useState([])
  const [flats, setFlats] = useState([])
  const [loading, setLoading] = useState(true)
  const [wingDialog, setWingDialog] = useState(false)
  const [flatDialog, setFlatDialog] = useState(false)
  const [wingName, setWingName] = useState('')
  const [flatForm, setFlatForm] = useState({
    wingId: '', flatNumber: '', floorNumber: '', areaSqft: '', paymentReferenceCode: ''
  })

  const load = async () => {
    try {
      setLoading(true)
      const [wRes, fRes] = await Promise.all([
        societyApi.getWings(societyId),
        societyApi.getFlats(societyId),
      ])
      setWings(wRes.data.data || [])
      setFlats(fRes.data.data || [])
    } catch {
      enqueueSnackbar('Failed to load society data', { variant: 'error' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [societyId])

  const handleAddWing = async () => {
    try {
      await societyApi.createWing(societyId, { wingName })
      enqueueSnackbar('Wing created', { variant: 'success' })
      setWingDialog(false)
      setWingName('')
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Failed to create wing', { variant: 'error' })
    }
  }

  const handleAddFlat = async () => {
    try {
      await societyApi.createFlat(societyId, {
        ...flatForm,
        wingId: Number(flatForm.wingId),
        floorNumber: flatForm.floorNumber ? Number(flatForm.floorNumber) : null,
        areaSqft: flatForm.areaSqft ? Number(flatForm.areaSqft) : null,
      })
      enqueueSnackbar('Flat created', { variant: 'success' })
      setFlatDialog(false)
      setFlatForm({ wingId: '', flatNumber: '', floorNumber: '', areaSqft: '', paymentReferenceCode: '' })
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Failed to create flat', { variant: 'error' })
    }
  }

  const handleDeleteWing = async (wingId) => {
    if (!window.confirm('Delete this wing and all its flats?')) return
    try {
      await societyApi.deleteWing(societyId, wingId)
      enqueueSnackbar('Wing deleted', { variant: 'success' })
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Cannot delete wing', { variant: 'error' })
    }
  }

  if (loading) return <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>

  return (
    <Box>
      <Typography variant="h5" fontWeight={600} mb={3}>Society Management</Typography>

      {/* Wings */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6" display="flex" alignItems="center" gap={1}>
              <Business fontSize="small" /> Wings ({wings.length})
            </Typography>
            <Button variant="contained" startIcon={<Add />} size="small"
              onClick={() => setWingDialog(true)}>
              Add Wing
            </Button>
          </Box>
          <Grid container spacing={1}>
            {wings.map((w) => (
              <Grid item key={w.id}>
                <Chip
                  label={`Wing ${w.wingName}`}
                  onDelete={() => handleDeleteWing(w.id)}
                  color="primary" variant="outlined"
                  deleteIcon={<Delete fontSize="small" />}
                />
              </Grid>
            ))}
            {wings.length === 0 && (
              <Grid item xs={12}>
                <Typography color="text.secondary" variant="body2">No wings yet. Add the first wing.</Typography>
              </Grid>
            )}
          </Grid>
        </CardContent>
      </Card>

      {/* Flats */}
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6" display="flex" alignItems="center" gap={1}>
              <Apartment fontSize="small" /> Flats ({flats.length})
            </Typography>
            <Button variant="contained" startIcon={<Add />} size="small"
              onClick={() => setFlatDialog(true)} disabled={wings.length === 0}>
              Add Flat
            </Button>
          </Box>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Flat No.</TableCell>
                  <TableCell>Wing</TableCell>
                  <TableCell>Floor</TableCell>
                  <TableCell>Area (sq.ft)</TableCell>
                  <TableCell>Payment Ref</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {flats.map((f) => (
                  <TableRow key={f.id} hover>
                    <TableCell><strong>{f.flatNumber}</strong></TableCell>
                    <TableCell>{wings.find(w => w.id === f.wingId)?.wingName || f.wingId}</TableCell>
                    <TableCell>{f.floorNumber ?? '—'}</TableCell>
                    <TableCell>{f.areaSqft ?? '—'}</TableCell>
                    <TableCell><code style={{ fontSize: '0.75rem' }}>{f.paymentReferenceCode}</code></TableCell>
                    <TableCell>
                      <Chip size="small" label={f.isOccupied ? 'Occupied' : 'Vacant'}
                        color={f.isOccupied ? 'success' : 'default'} />
                    </TableCell>
                  </TableRow>
                ))}
                {flats.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} align="center">
                      <Typography color="text.secondary" variant="body2">No flats yet.</Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Add Wing Dialog */}
      <Dialog open={wingDialog} onClose={() => setWingDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Add Wing</DialogTitle>
        <DialogContent>
          <TextField label="Wing Name (e.g. A, B, C)" fullWidth autoFocus
            value={wingName} onChange={(e) => setWingName(e.target.value)}
            sx={{ mt: 1 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWingDialog(false)}>Cancel</Button>
          <Button onClick={handleAddWing} variant="contained" disabled={!wingName.trim()}>Create</Button>
        </DialogActions>
      </Dialog>

      {/* Add Flat Dialog */}
      <Dialog open={flatDialog} onClose={() => setFlatDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add Flat</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid item xs={12}>
              <TextField select label="Wing" fullWidth required
                value={flatForm.wingId} onChange={(e) => setFlatForm({ ...flatForm, wingId: e.target.value })}>
                {wings.map((w) => <MenuItem key={w.id} value={w.id}>Wing {w.wingName}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField label="Flat Number" fullWidth required
                value={flatForm.flatNumber} onChange={(e) => setFlatForm({ ...flatForm, flatNumber: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Floor" type="number" fullWidth
                value={flatForm.floorNumber} onChange={(e) => setFlatForm({ ...flatForm, floorNumber: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Area (sq.ft)" type="number" fullWidth
                value={flatForm.areaSqft} onChange={(e) => setFlatForm({ ...flatForm, areaSqft: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField label="Payment Reference Code" fullWidth required
                helperText="Unique code used to match bank transactions"
                value={flatForm.paymentReferenceCode}
                onChange={(e) => setFlatForm({ ...flatForm, paymentReferenceCode: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFlatDialog(false)}>Cancel</Button>
          <Button onClick={handleAddFlat} variant="contained"
            disabled={!flatForm.wingId || !flatForm.flatNumber || !flatForm.paymentReferenceCode}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
