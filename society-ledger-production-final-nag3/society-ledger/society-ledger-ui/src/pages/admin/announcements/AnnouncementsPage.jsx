import { useState, useEffect } from 'react'
import {
  Box, Typography, Button, Card, CardContent, CardActions,
  Grid, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Chip, IconButton, CircularProgress
} from '@mui/material'
import { Add, NotificationsActive, Archive } from '@mui/icons-material'
import { useSnackbar } from 'notistack'
import { financeApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'

const CATEGORIES = ['GENERAL', 'MAINTENANCE', 'FINANCE', 'EVENT', 'URGENT']
const CATEGORY_COLORS = { GENERAL: 'default', MAINTENANCE: 'info', FINANCE: 'success', EVENT: 'primary', URGENT: 'error' }

export default function AnnouncementsPage() {
  const { user } = useAuth()
  const { enqueueSnackbar } = useSnackbar()
  const societyId = user?.societyId
  const isAdmin = user?.role === 'ADMIN'

  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState(false)
  const [form, setForm] = useState({ title: '', body: '', category: 'GENERAL', expiresAt: '' })

  const load = async () => {
    try {
      setLoading(true)
      const res = await financeApi.getAnnouncements(societyId)
      setAnnouncements(res.data.data || [])
    } catch {
      enqueueSnackbar('Failed to load announcements', { variant: 'error' })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [societyId])

  const handleCreate = async () => {
    try {
      await financeApi.createAnnouncement(societyId, {
        ...form,
        expiresAt: form.expiresAt || null,
      })
      enqueueSnackbar('Announcement posted', { variant: 'success' })
      setDialog(false)
      setForm({ title: '', body: '', category: 'GENERAL', expiresAt: '' })
      load()
    } catch (e) {
      enqueueSnackbar(e.response?.data?.message || 'Failed to post', { variant: 'error' })
    }
  }

  const handleDeactivate = async (id) => {
    try {
      await financeApi.deleteAnnouncement(societyId, id)
      enqueueSnackbar('Announcement archived', { variant: 'success' })
      load()
    } catch {
      enqueueSnackbar('Failed to archive', { variant: 'error' })
    }
  }

  if (loading) return <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={600} display="flex" alignItems="center" gap={1}>
          <NotificationsActive /> Announcements
        </Typography>
        {isAdmin && (
          <Button variant="contained" startIcon={<Add />} onClick={() => setDialog(true)}>
            New Announcement
          </Button>
        )}
      </Box>

      {announcements.length === 0 && (
        <Card><CardContent>
          <Typography color="text.secondary" align="center">No active announcements.</Typography>
        </CardContent></Card>
      )}

      <Grid container spacing={2}>
        {announcements.map((a) => (
          <Grid item xs={12} md={6} key={a.id}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                  <Typography variant="h6" fontWeight={600}>{a.title}</Typography>
                  <Chip size="small" label={a.category} color={CATEGORY_COLORS[a.category] || 'default'} />
                </Box>
                <Typography variant="body2" mt={1} color="text.secondary">{a.body}</Typography>
                {a.expiresAt && (
                  <Typography variant="caption" color="text.secondary" mt={1} display="block">
                    Expires: {new Date(a.expiresAt).toLocaleDateString('en-IN')}
                  </Typography>
                )}
              </CardContent>
              {isAdmin && (
                <CardActions>
                  <Button size="small" startIcon={<Archive />} color="warning"
                    onClick={() => handleDeactivate(a.id)}>
                    Archive
                  </Button>
                </CardActions>
              )}
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>New Announcement</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="Title" required fullWidth
              value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            <TextField label="Message" required multiline rows={4} fullWidth
              value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })} />
            <TextField select label="Category" fullWidth
              value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              {CATEGORIES.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
            </TextField>
            <TextField label="Expires At (optional)" type="datetime-local" fullWidth
              InputLabelProps={{ shrink: true }}
              value={form.expiresAt} onChange={(e) => setForm({ ...form, expiresAt: e.target.value })} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleCreate} variant="contained" disabled={!form.title || !form.body}>Post</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
