import React, { useEffect, useState } from "react";
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, CircularProgress, Alert, Pagination
} from "@mui/material";
import ReceiptIcon from "@mui/icons-material/Receipt";
import { receiptApi } from "../../api/receiptApi";
import { useSociety } from "../../auth/AuthContext";

export default function MyReceiptsPage() {
  const { societyId } = useSociety();
  const [receipts, setReceipts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [error, setError] = useState(null);

  useEffect(() => { loadReceipts(); }, [page]);

  const loadReceipts = async () => {
    setLoading(true);
    try {
      const res = await receiptApi.getMy(societyId, page - 1);
      setReceipts(res.content ?? []);
      setTotalPages(res.totalPages ?? 1);
    } catch { setError("Failed to load receipts."); }
    finally { setLoading(false); }
  };

  const MONTHS = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

  return (
    <Box p={3}>
      <Box display="flex" alignItems="center" gap={1} mb={3}>
        <ReceiptIcon color="primary" />
        <Typography variant="h5" fontWeight={700}>My Receipts</Typography>
      </Box>

      {error && <Alert severity="error">{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <>
          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Receipt No.</TableCell>
                  <TableCell>Month</TableCell>
                  <TableCell align="right">Amount (₹)</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Reference</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {receipts.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell sx={{ fontFamily: "monospace", fontSize: 13 }}>{r.receiptNumber}</TableCell>
                    <TableCell>{MONTHS[(r.month ?? 1) - 1]} {r.year}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600 }}>
                      {Number(r.amount).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
                    </TableCell>
                    <TableCell>{r.transactionDate}</TableCell>
                    <TableCell sx={{ fontFamily: "monospace", fontSize: 12 }}>
                      {r.referenceCode || "—"}
                    </TableCell>
                    <TableCell>
                      <Chip label={r.status} size="small"
                        color={r.status === "GENERATED" ? "success" : "default"} />
                    </TableCell>
                  </TableRow>
                ))}
                {receipts.length === 0 && (
                  <TableRow><TableCell colSpan={6} align="center">No receipts yet.</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <Box display="flex" justifyContent="center" mt={2}>
            <Pagination count={totalPages} page={page} onChange={(_, v) => setPage(v)} />
          </Box>
        </>
      )}
    </Box>
  );
}
