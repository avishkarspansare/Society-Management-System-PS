import React, { useEffect, useState } from "react";
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Button, Chip, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Alert, CircularProgress
} from "@mui/material";
import { statementApi } from "../../api/statementApi";
import { useSociety } from "../../auth/AuthContext";

export default function UnmatchedTransactionsPage() {
  const { societyId } = useSociety();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState({ open: false, txn: null });
  const [flatId, setFlatId] = useState("");
  const [matching, setMatching] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadUnmatched(); }, []);

  const loadUnmatched = async () => {
    setLoading(true);
    try {
      const res = await statementApi.getUnmatched(societyId);
      setTransactions(res.content ?? []);
    } catch { setError("Failed to load unmatched transactions."); }
    finally { setLoading(false); }
  };

  const openDialog = (txn) => { setDialog({ open: true, txn }); setFlatId(""); };

  const handleManualMatch = async () => {
    if (!flatId) return;
    setMatching(true);
    try {
      await statementApi.manualMatch(societyId, dialog.txn.id, flatId);
      setDialog({ open: false, txn: null });
      loadUnmatched();
    } catch (e) {
      setError(e.response?.data?.message || "Match failed.");
    } finally { setMatching(false); }
  };

  if (loading) return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;

  return (
    <Box p={3}>
      <Typography variant="h5" fontWeight={700} gutterBottom>Unmatched Transactions</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {transactions.length === 0
        ? <Alert severity="success">All transactions matched. 🎉</Alert>
        : (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Date</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell align="right">Amount (₹)</TableCell>
                  <TableCell>Reference</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {transactions.map((t) => (
                  <TableRow key={t.id}>
                    <TableCell>{t.transactionDate}</TableCell>
                    <TableCell sx={{ maxWidth: 200, overflow: "hidden", textOverflow: "ellipsis" }}>
                      {t.description}
                    </TableCell>
                    <TableCell align="right">{Number(t.amount).toLocaleString("en-IN")}</TableCell>
                    <TableCell><Chip label={t.referenceCode || "N/A"} size="small" /></TableCell>
                    <TableCell>
                      <Button size="small" variant="outlined" onClick={() => openDialog(t)}>
                        Manual Match
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )
      }

      <Dialog open={dialog.open} onClose={() => setDialog({ open: false, txn: null })} maxWidth="xs" fullWidth>
        <DialogTitle>Manual Match</DialogTitle>
        <DialogContent>
          {dialog.txn && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" color="text.secondary">
                ₹{Number(dialog.txn.amount).toLocaleString("en-IN")} on {dialog.txn.transactionDate}
              </Typography>
            </Box>
          )}
          <TextField label="Flat ID" value={flatId} onChange={(e) => setFlatId(e.target.value)}
            fullWidth type="number" variant="outlined" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog({ open: false, txn: null })}>Cancel</Button>
          <Button variant="contained" onClick={handleManualMatch} disabled={!flatId || matching}>
            {matching ? "Matching..." : "Confirm Match"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
