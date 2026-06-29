import React, { useEffect, useState } from "react";
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, CircularProgress, Alert, Pagination, Chip
} from "@mui/material";
import { societyApi } from "../../api/societyApi";
import { useSociety } from "../../auth/AuthContext";

const ACTION_COLORS = {
  FLAT_CREATED: "primary", WING_CREATED: "info",
  EXPENSE_PUBLISHED: "success", STATEMENT_UPLOADED: "warning",
};

export default function ActivityLogPage() {
  const { societyId } = useSociety();
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [error, setError] = useState(null);

  useEffect(() => { loadLogs(); }, [page]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const res = await societyApi.getActivityLog(societyId, page - 1, 20);
      setLogs(res.content ?? []);
      setTotalPages(res.totalPages ?? 1);
    } catch { setError("Failed to load activity log."); }
    finally { setLoading(false); }
  };

  return (
    <Box p={3}>
      <Typography variant="h5" fontWeight={700} mb={3}>Activity Log</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Action</TableCell>
                  <TableCell>Entity</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Actor</TableCell>
                  <TableCell>Time</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {logs.map((log) => (
                  <TableRow key={log.id}>
                    <TableCell>
                      <Chip label={log.action} size="small"
                        color={ACTION_COLORS[log.action] || "default"} />
                    </TableCell>
                    <TableCell>{log.entityType ?? "—"}</TableCell>
                    <TableCell sx={{ maxWidth: 300, overflow: "hidden", textOverflow: "ellipsis" }}>
                      {log.description}
                    </TableCell>
                    <TableCell>User #{log.actorUserId}</TableCell>
                    <TableCell>
                      {new Date(log.createdAt).toLocaleString("en-IN", {
                        day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit"
                      })}
                    </TableCell>
                  </TableRow>
                ))}
                {logs.length === 0 && (
                  <TableRow><TableCell colSpan={5} align="center">No activity recorded.</TableCell></TableRow>
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
