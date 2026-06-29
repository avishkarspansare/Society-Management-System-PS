import { useEffect, useState, useCallback } from 'react'
import {
  Box, Card, CardContent, Typography, Button, TextField,
  Alert, CircularProgress, Chip, Divider, Dialog,
  DialogTitle, DialogContent, DialogActions, Accordion,
  AccordionSummary, AccordionDetails, Pagination
} from '@mui/material'
import {
  Add, ExpandMore, QuestionAnswer, CheckCircle, HourglassEmpty
} from '@mui/icons-material'
import { queryApi } from '../../api/financeApi'
import { useAuth } from '../../auth/AuthContext'

const STATUS_COLORS = { OPEN: 'warning', ANSWERED: 'success', CLOSED: 'default' }

export default function QueryPage() {
  const { user, isAdmin } = useAuth()
  const [queries, setQueries]   = useState([])
  const [total, setTotal]       = useState(1)
  const [page, setPage]         = useState(1)
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')
  const [success, setSuccess]   = useState('')

  // New query dialog
  const [newOpen, setNewOpen]   = useState(false)
  const [newForm, setNewForm]   = useState({ subject: '', body: '' })
  const [submitting, setSubmitting] = useState(false)

  // Response dialog (admin)
  const [replyOpen, setReplyOpen]   = useState(false)
  const [replyQuery, setReplyQuery] = useState(null)
  const [replyText, setReplyText]   = useState('')
  const [replying, setReplying]     = useState(false)

  const loadQueries = useCallback(async () => {
    setLoading(true); setError('')
    try {
      const res = await queryApi.getQueries(user.societyId, { page: page - 1, size: 10 })
      const data = res.data.data
      setQueries(data.content || [])
      setTotal(data.totalPages || 1)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to load queries.')
    } finally { setLoading(false) }
  }, [user, page])

  useEffect(() => { loadQueries() }, [loadQueries])

  const handleSubmitQuery = async () => {
    if (!newForm.subject.trim() || !newForm.body.trim()) {
      setError('Subject and question are required.'); return
    }
    setSubmitting(true); setError('')
    try {
      await queryApi.createQuery(user.societyId, newForm)
      setSuccess('Your query has been submitted.')
      setNewOpen(false)
      setNewForm({ subject: '', body: '' })
      await loadQueries()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to submit query.')
    } finally { setSubmitting(false) }
  }

  const handleReply = async () => {
    if (!replyText.trim()) return
    setReplying(true)
    try {
      await queryApi.respondToQuery(user.societyId, replyQuery.id, { response: replyText })
      setSuccess('Response posted successfully.')
      setReplyOpen(false); setReplyText('')
      await loadQueries()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to post response.')
    } finally { setReplying(false) }
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h5" fontWeight={700}>Public Queries</Typography>
          <Typography variant="body2" color="text.secondary">
            Questions and answers visible to all society members
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={() => setNewOpen(true)}>
          Ask a Question
        </Button>
      </Box>

      {error   && <Alert severity="error"   sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}

      {loading ? (
        <Box display="flex" justifyContent="center" py={8}><CircularProgress size={40} /></Box>
      ) : queries.length === 0 ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 6 }}>
            <QuestionAnswer sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
            <Typography variant="body1" color="text.secondary">No queries yet.</Typography>
            <Typography variant="body2" color="text.secondary">
              Be the first to ask a question about society finances.
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <>
          {queries.map(q => (
            <Card key={q.id} sx={{ mb: 2 }}>
              <Accordion disableGutters elevation={0} sx={{ border: 'none' }}>
                <AccordionSummary expandIcon={<ExpandMore />} sx={{ px: 3, py: 1 }}>
                  <Box flex={1} display="flex" alignItems="center" gap={1.5} flexWrap="wrap">
                    <Box flex={1}>
                      <Typography variant="subtitle2" fontWeight={600}>{q.subject}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        Asked {new Date(q.createdAt).toLocaleDateString('en-IN')}
                      </Typography>
                    </Box>
                    <Chip
                      label={q.status}
                      size="small"
                      color={STATUS_COLORS[q.status]}
                      icon={q.status === 'ANSWERED' ? <CheckCircle fontSize="small" /> : <HourglassEmpty fontSize="small" />}
                    />
                  </Box>
                </AccordionSummary>

                <AccordionDetails sx={{ px: 3, pt: 0, pb: 2 }}>
                  <Divider sx={{ mb: 2 }} />

                  {/* Question */}
                  <Box sx={{ bgcolor: 'grey.50', borderRadius: 2, p: 2, mb: 2 }}>
                    <Typography variant="caption" color="text.secondary" fontWeight={600}
                      display="block" mb={0.5}>QUESTION</Typography>
                    <Typography variant="body2">{q.body}</Typography>
                  </Box>

                  {/* Responses */}
                  {q.responses?.map(r => (
                    <Box key={r.id} sx={{ bgcolor: 'primary.50', borderRadius: 2, p: 2,
                      mb: 1, borderLeft: '3px solid', borderColor: 'primary.main' }}>
                      <Typography variant="caption" color="primary.main" fontWeight={600}
                        display="block" mb={0.5}>
                        ADMIN RESPONSE · {new Date(r.createdAt).toLocaleDateString('en-IN')}
                      </Typography>
                      <Typography variant="body2">{r.response}</Typography>
                    </Box>
                  ))}

                  {/* Admin Reply Button */}
                  {isAdmin && q.status === 'OPEN' && (
                    <Button size="small" variant="outlined" sx={{ mt: 1 }}
                      onClick={() => { setReplyQuery(q); setReplyOpen(true) }}>
                      Post Response
                    </Button>
                  )}
                </AccordionDetails>
              </Accordion>
            </Card>
          ))}

          {total > 1 && (
            <Box display="flex" justifyContent="center" mt={2}>
              <Pagination count={total} page={page} onChange={(_, v) => setPage(v)} color="primary" />
            </Box>
          )}
        </>
      )}

      {/* New Query Dialog */}
      <Dialog open={newOpen} onClose={() => setNewOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle fontWeight={700}>Ask a Question</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" mb={2}>
            Your question will be visible to all society members along with the admin's response.
          </Typography>
          <TextField label="Subject" fullWidth required sx={{ mb: 2 }}
            value={newForm.subject} onChange={e => setNewForm(f => ({ ...f, subject: e.target.value }))}
            placeholder="e.g. Why was ₹50,000 spent on corridor repairs?" />
          <TextField label="Your Question" fullWidth required multiline rows={4}
            value={newForm.body} onChange={e => setNewForm(f => ({ ...f, body: e.target.value }))}
            placeholder="Provide more details about your question..." />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setNewOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmitQuery} disabled={submitting}>
            {submitting ? <CircularProgress size={20} color="inherit" /> : 'Submit Question'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Admin Reply Dialog */}
      <Dialog open={replyOpen} onClose={() => setReplyOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle fontWeight={700}>Post Response</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" mb={1}>
            Responding to: <strong>{replyQuery?.subject}</strong>
          </Typography>
          <Box sx={{ bgcolor: 'grey.50', borderRadius: 1.5, p: 2, mb: 2 }}>
            <Typography variant="body2">{replyQuery?.body}</Typography>
          </Box>
          <TextField label="Your Response" fullWidth required multiline rows={4}
            value={replyText} onChange={e => setReplyText(e.target.value)}
            placeholder="Provide a clear and transparent answer..." />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => { setReplyOpen(false); setReplyText('') }}>Cancel</Button>
          <Button variant="contained" onClick={handleReply}
            disabled={replying || !replyText.trim()}>
            {replying ? <CircularProgress size={20} color="inherit" /> : 'Post Response'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
