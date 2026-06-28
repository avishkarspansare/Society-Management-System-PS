import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Card, CardContent, Typography, TextField, Button,
  Grid, MenuItem, Alert, CircularProgress, Divider, Paper
} from '@mui/material'
import { CloudUpload, Save, ArrowBack } from '@mui/icons-material'
import { financeApi } from '../../../api/financeApi'
import { useAuth } from '../../../auth/AuthContext'

export default function ExpenseFormPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [categories, setCategories] = useState([])
  const [form, setForm] = useState({
    categoryId: '', amount: '', vendorName: '',
    description: '', expenseDate: new Date().toISOString().split('T')[0],
  })
  const [proofFile, setProofFile]   = useState(null)
  const [loading, setLoading]       = useState(false)
  const [catLoading, setCatLoading] = useState(true)
  const [error, setError]           = useState('')

  useEffect(() => {
    financeApi.getCategories(user.societyId)
      .then(res => setCategories(res.data.data || []))
      .catch(() => setError('Failed to load categories.'))
      .finally(() => setCatLoading(false))
  }, [user])

  const handleChange = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleFileChange = (e) => {
    const file = e.target.files[0]
    if (file) setProofFile(file)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.categoryId) { setError('Please select a category.'); return }
    setError(''); setLoading(true)
    try {
      const res = await financeApi.createExpense(user.societyId, {
        ...form,
        categoryId: Number(form.categoryId),
        amount: Number(form.amount),
      })
      const expenseId = res.data.data.id

      // Upload proof immediately if provided
      if (proofFile) {
        await financeApi.uploadProof(user.societyId, expenseId, proofFile)
      }

      navigate('/admin/expenses', { state: { success: 'Expense created successfully.' } })
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create expense.')
    } finally { setLoading(false) }
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 800, mx: 'auto' }}>
      <Box display="flex" alignItems="center" gap={1.5} mb={3}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/admin/expenses')} variant="outlined" size="small">
          Back
        </Button>
        <Box>
          <Typography variant="h5" fontWeight={700}>New Expense</Typography>
          <Typography variant="body2" color="text.secondary">
            Fill in all details. Proof is required before publishing.
          </Typography>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <CardContent sx={{ p: 3 }}>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={2.5}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Vendor Name" name="vendorName" fullWidth required
                  value={form.vendorName} onChange={handleChange}
                  placeholder="e.g. ABC Plumbing Services"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Amount (₹)" name="amount" type="number" fullWidth required
                  value={form.amount} onChange={handleChange}
                  inputProps={{ min: 1, step: '0.01' }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  select label="Category" name="categoryId" fullWidth required
                  value={form.categoryId} onChange={handleChange}
                  disabled={catLoading}
                >
                  {catLoading
                    ? <MenuItem disabled>Loading...</MenuItem>
                    : categories.map(cat => (
                      <MenuItem key={cat.id} value={cat.id}>{cat.name}</MenuItem>
                    ))}
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Expense Date" name="expenseDate" type="date" fullWidth required
                  value={form.expenseDate} onChange={handleChange}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Description" name="description" fullWidth required multiline rows={3}
                  value={form.description} onChange={handleChange}
                  placeholder="Describe what this expense was for..."
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 1 }} />
                <Typography variant="subtitle2" fontWeight={600} mb={1.5}>
                  Expense Proof <Typography component="span" color="error.main">*</Typography>
                </Typography>
                <Typography variant="caption" color="text.secondary" display="block" mb={1.5}>
                  Upload invoice, bill or photo (PDF, JPEG, PNG — max 10 MB).
                  Required before expense can be published.
                </Typography>

                <Paper
                  variant="outlined"
                  sx={{
                    p: 3, textAlign: 'center', borderStyle: 'dashed',
                    borderColor: proofFile ? 'success.main' : 'divider',
                    bgcolor: proofFile ? 'success.50' : 'grey.50',
                    cursor: 'pointer', borderRadius: 2,
                    '&:hover': { borderColor: 'primary.main', bgcolor: 'primary.50' },
                  }}
                  component="label"
                >
                  <input type="file" hidden accept=".pdf,.jpg,.jpeg,.png,.webp"
                    onChange={handleFileChange} />
                  <CloudUpload sx={{ fontSize: 36, color: proofFile ? 'success.main' : 'text.secondary', mb: 1 }} />
                  <Typography variant="body2" fontWeight={500}>
                    {proofFile ? proofFile.name : 'Click to upload proof document'}
                  </Typography>
                  {proofFile && (
                    <Typography variant="caption" color="success.main">
                      ✓ {(proofFile.size / 1024).toFixed(1)} KB
                    </Typography>
                  )}
                </Paper>
              </Grid>

              <Grid item xs={12}>
                <Box display="flex" gap={2} justifyContent="flex-end" mt={1}>
                  <Button variant="outlined" onClick={() => navigate('/admin/expenses')}>
                    Cancel
                  </Button>
                  <Button type="submit" variant="contained" startIcon={<Save />}
                    disabled={loading} sx={{ minWidth: 140 }}>
                    {loading ? <CircularProgress size={20} color="inherit" /> : 'Save as Draft'}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  )
}
