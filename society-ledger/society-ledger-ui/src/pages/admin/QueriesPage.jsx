import React, { useEffect, useState } from "react";
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, Chip, Button, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, Select, FormControl, InputLabel,
  Alert, CircularProgress
} from "@mui/material";
import { queryApi } from "../../api/queryApi";
import { useSociety } from "../../auth/AuthContext";

const STATUS_COLORS = { OPEN: "error", IN_PROGRESS: "warning", ANSWERED: "success", CLOSED: "default" };

export default function QueriesPage() {
  const { societyId } = useSociety();
  const [queries, setQueries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("");
  const [dialog, setDialog] = useState({ open: false, query: null });
  const [answer, setAnswer] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadQueries(); }, [filter]);

  const loadQueries = async () => {
    setLoading(true);
    try {
      const res = await queryApi.getAll(societyId, filter || undefined);
      setQueries(res.content ?? []);
    } catch { setError("Failed to load queries."); }
    finally { setLoading(false); }
  };

  const handleAnswer = async () => {
    if (!answer.trim()) return;
    setSubmitting(true);
    try {
      await queryApi.answer(societyId, dialog.query.id, answer);
      setDialog({ open: false, query: null }); setAnswer("");
      loadQueries();
    } catch (e) { setError("Answer failed."); }
    finally { setSubmitting(false); }
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Resident Queries</Typography>
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Filter by Status</InputLabel>
          <Select value={filter} label="Filter by Status" onChange={(e) => setFilter(e.target.value)}>
            <MenuItem value="">All</MenuItem>
            <MenuItem value="OPEN">Open</MenuItem>
            <MenuItem value="ANSWERED">Answered</MenuItem>
            <MenuItem value="CLOSED">Closed</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Subject</TableCell>
                <TableCell>Flat</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Submitted</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {queries.map((q) => (
                <TableRow key={q.id} sx={{ "&:hover": { bgcolor: "action.hover" } }}>
                  <TableCell>{q.subject}</TableCell>
                  <TableCell>{q.flatId ?? "—"}</TableCell>
                  <TableCell>
                    <Chip label={q.status} size="small" color={STATUS_COLORS[q.status] || "default"} />
                  </TableCell>
                  <TableCell>{new Date(q.createdAt).toLocaleDateString("en-IN")}</TableCell>
                  <TableCell>
                    {q.status === "OPEN" && (
                      <Button size="small" variant="outlined"
                        onClick={() => { setDialog({ open: true, query: q }); setAnswer(""); }}>
                        Answer
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {queries.length === 0 && (
                <TableRow><TableCell colSpan={5} align="center">No queries found.</TableCell></TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialog.open} onClose={() => setDialog({ open: false, query: null })} maxWidth="sm" fullWidth>
        <DialogTitle>Answer Query</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          {dialog.query && (
            <Box sx={{ p: 2, bgcolor: "grey.50", borderRadius: 1 }}>
              <Typography variant="subtitle2">{dialog.query.subject}</Typography>
              <Typography variant="body2" color="text.secondary" mt={0.5}>{dialog.query.body}</Typography>
            </Box>
          )}
          <TextField label="Your Answer" multiline rows={4} value={answer}
            onChange={(e) => setAnswer(e.target.value)} fullWidth />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ open: false, query: null })}>Cancel</Button>
          <Button variant="contained" onClick={handleAnswer} disabled={!answer.trim() || submitting}>
            {submitting ? "Submitting..." : "Submit Answer"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
