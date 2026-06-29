import React, { useState } from "react";
import {
  Box, Button, Typography, Alert, LinearProgress,
  Card, CardContent, Chip, Divider, MenuItem, Select, FormControl, InputLabel
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { statementApi } from "../../api/statementApi";
import { useSociety } from "../../auth/AuthContext";

export default function StatementUploadPage() {
  const { societyId } = useSociety();
  const [file, setFile] = useState(null);
  const [bankCode, setBankCode] = useState("BOB");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true); setError(null); setResult(null);
    try {
      const data = await statementApi.upload(societyId, file, bankCode);
      setResult(data);
      setFile(null);
    } catch (e) {
      setError(e.response?.data?.message || "Upload failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 680, mx: "auto", p: 3 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Upload Bank Statement
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Upload the official society bank statement. The system will automatically
        parse transactions and match maintenance payments.
      </Typography>

      <Card variant="outlined" sx={{ mb: 3 }}>
        <CardContent sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <FormControl fullWidth>
            <InputLabel>Bank</InputLabel>
            <Select value={bankCode} label="Bank" onChange={(e) => setBankCode(e.target.value)}>
              <MenuItem value="BOB">Bank of Baroda (BoB)</MenuItem>
              <MenuItem value="SBI" disabled>SBI (Coming soon)</MenuItem>
              <MenuItem value="HDFC" disabled>HDFC (Coming soon)</MenuItem>
            </Select>
          </FormControl>

          <Button
            variant="outlined" component="label" startIcon={<CloudUploadIcon />}
            sx={{ py: 2, borderStyle: "dashed" }}
          >
            {file ? file.name : "Select CSV / XLS Statement"}
            <input type="file" hidden accept=".csv,.xls,.xlsx"
              onChange={(e) => setFile(e.target.files[0])} />
          </Button>

          {file && (
            <Typography variant="body2" color="text.secondary">
              {(file.size / 1024).toFixed(1)} KB — ready to upload
            </Typography>
          )}

          {loading && <LinearProgress />}

          <Button
            variant="contained" size="large" disabled={!file || loading}
            onClick={handleUpload}
          >
            Upload & Parse
          </Button>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {result && (
        <Card sx={{ bgcolor: "success.50", border: "1px solid", borderColor: "success.200" }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={1} mb={1}>
              <CheckCircleIcon color="success" />
              <Typography fontWeight={600}>Statement Processed</Typography>
            </Box>
            <Divider sx={{ my: 1 }} />
            <Box display="flex" gap={1} flexWrap="wrap">
              <Chip label={`${result.totalTransactions ?? "?"} transactions`} />
              <Chip label={`${result.matchedCount ?? "?"} matched`} color="success" />
              <Chip label={`${result.unmatchedCount ?? "?"} unmatched`} color="warning" />
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
