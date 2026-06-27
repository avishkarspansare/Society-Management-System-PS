import React, { useEffect, useState } from "react";
import {
  Box, Typography, Button, TextField, Dialog, DialogTitle, DialogContent,
  DialogActions, Card, CardContent, Chip, Alert, CircularProgress, Divider
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { queryApi } from "../../api/queryApi";
import { useSociety } from "../../auth/AuthContext";

const STATUS_COLORS = { OPEN: "error", IN_PROGRESS: "warning", ANSWERED: "success", CLOSED: "default" };

export default function MyQueriesPage() {
  const { societyId } = useSociety();
  const [queries, setQueries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState({ subject: "", body: "" });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadQueries(); }, []);

  const loadQueries = async () => {
    setLoading(true);
    try {
      const res = await queryApi.getMyQueries(societyId);
      setQueries(res.content ?? []);
    } catch { setError("Failed to load queries."); }
    finally { setLoading(false); }
  };

  const handleSubmit = async () => {
    if (!form.subject || !form.body) return;
    setSubmitting(true);
    try {
      await queryApi.create(societyId, form);
      setDialog(false); setForm({ subject: "", body: "" });
      loadQueries();
    } catch (e) { setError("Failed to submit query."); }
    finally { setSubmitting(false); }
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>My Queries</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>
          New Query
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          {queries.length === 0
            ? <Alert severity="info">No queries submitted yet.</Alert>
            : queries.map((q) => (
                <Card key={q.id} variant="outlined">
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                      <Typography fontWeight={600}>{q.subject}</Typography>
                      <Chip label={q.status} size="small" color={STATUS_COLORS[q.status] || "default"} />
                    </Box>
                    <Typography variant="body2" color="text.secondary" mt={1}>{q.body}</Typography>
                    {q.answer && (
                      <>
                        <Divider sx={{ my: 1.5 }} />
                        <Typography variant="body2" fontWeight={600} color="success.main">
                          Committee Response:
                        </Typography>
                        <Typography variant="body2">{q.answer}</Typography>
                      </>
                    )}
                    <Typography variant="caption" color="text.disabled" mt={1} display="block">
                      {new Date(q.createdAt).toLocaleDateString("en-IN")}
                    </Typography>
                  </CardContent>
                </Card>
              ))
          }
        </Box>
      )}

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Submit a Query</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          <TextField label="Subject *" value={form.subject} fullWidth
            onChange={(e) => setForm({ ...form, subject: e.target.value })} />
          <TextField label="Details *" multiline rows={4} value={form.body} fullWidth
            onChange={(e) => setForm({ ...form, body: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit}
            disabled={!form.subject || !form.body || submitting}>
            {submitting ? "Submitting..." : "Submit"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
