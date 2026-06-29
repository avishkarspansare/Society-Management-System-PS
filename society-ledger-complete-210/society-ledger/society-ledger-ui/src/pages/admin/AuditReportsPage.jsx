import React, { useEffect, useState } from "react";
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, LinearProgress, Alert, Dialog,
  DialogTitle, DialogContent, DialogActions, TextField
} from "@mui/material";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import { auditApi } from "../../api/auditApi";
import { useSociety } from "../../auth/AuthContext";

const statusColor = { UPLOADED: "default", REVIEWED: "info", PUBLISHED: "success", ARCHIVED: "warning" };

export default function AuditReportsPage() {
  const { societyId } = useSociety();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState(false);
  const [file, setFile] = useState(null);
  const [form, setForm] = useState({ title: "", description: "", periodFrom: "", periodTo: "" });
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadReports(); }, []);

  const loadReports = async () => {
    setLoading(true);
    try { const res = await auditApi.getAll(societyId); setReports(res.content ?? []); }
    catch { setError("Failed to load audit reports."); }
    finally { setLoading(false); }
  };

  const handleUpload = async () => {
    if (!file || !form.title) return;
    setUploading(true); setError(null);
    try {
      await auditApi.upload(societyId, { ...form, file });
      setDialog(false); setFile(null);
      setForm({ title: "", description: "", periodFrom: "", periodTo: "" });
      loadReports();
    } catch (e) { setError(e.response?.data?.message || "Upload failed."); }
    finally { setUploading(false); }
  };

  const handlePublish = async (id) => {
    try { await auditApi.publish(societyId, id); loadReports(); }
    catch (e) { setError("Publish failed."); }
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Audit Reports</Typography>
        <Button variant="contained" startIcon={<UploadFileIcon />} onClick={() => setDialog(true)}>
          Upload Report
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading && <LinearProgress />}

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Period</TableCell>
              <TableCell>File</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Uploaded</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {reports.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{r.title}</TableCell>
                <TableCell>
                  {r.periodFrom && r.periodTo ? `${r.periodFrom} → ${r.periodTo}` : "—"}
                </TableCell>
                <TableCell sx={{ fontSize: 12 }}>{r.fileName}</TableCell>
                <TableCell>
                  <Chip label={r.status} size="small" color={statusColor[r.status] || "default"} />
                </TableCell>
                <TableCell>{new Date(r.createdAt).toLocaleDateString("en-IN")}</TableCell>
                <TableCell>
                  {r.status === "UPLOADED" && (
                    <Button size="small" onClick={() => handlePublish(r.id)}>Publish</Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {reports.length === 0 && !loading && (
              <TableRow><TableCell colSpan={6} align="center">No audit reports yet.</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Upload Audit Report</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          <TextField label="Title *" value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })} fullWidth />
          <TextField label="Description" value={form.description} multiline rows={2}
            onChange={(e) => setForm({ ...form, description: e.target.value })} fullWidth />
          <Box display="flex" gap={2}>
            <TextField label="Period From" type="date" InputLabelProps={{ shrink: true }}
              value={form.periodFrom} onChange={(e) => setForm({ ...form, periodFrom: e.target.value })} fullWidth />
            <TextField label="Period To" type="date" InputLabelProps={{ shrink: true }}
              value={form.periodTo} onChange={(e) => setForm({ ...form, periodTo: e.target.value })} fullWidth />
          </Box>
          <Button variant="outlined" component="label" startIcon={<UploadFileIcon />}>
            {file ? file.name : "Select PDF / Image"}
            <input type="file" hidden accept=".pdf,image/*" onChange={(e) => setFile(e.target.files[0])} />
          </Button>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleUpload}
            disabled={!file || !form.title || uploading}>
            {uploading ? "Uploading..." : "Upload"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
