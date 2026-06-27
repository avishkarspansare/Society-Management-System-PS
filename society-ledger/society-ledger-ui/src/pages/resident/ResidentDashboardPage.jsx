import React, { useEffect, useState } from "react";
import {
  Box, Grid, Card, CardContent, Typography, Chip, Divider,
  CircularProgress, Alert, List, ListItem, ListItemText, Button
} from "@mui/material";
import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import ReceiptIcon from "@mui/icons-material/Receipt";
import QuestionAnswerIcon from "@mui/icons-material/QuestionAnswer";
import AnnouncementIcon from "@mui/icons-material/Announcement";
import { financeApi } from "../../api/financeApi";
import { receiptApi } from "../../api/receiptApi";
import { queryApi } from "../../api/queryApi";
import { useSociety } from "../../auth/AuthContext";
import { useNavigate } from "react-router-dom";

function StatCard({ icon, label, value, color = "primary.main", sub }) {
  return (
    <Card variant="outlined">
      <CardContent sx={{ display: "flex", alignItems: "center", gap: 2 }}>
        <Box sx={{ color, fontSize: 40 }}>{icon}</Box>
        <Box>
          <Typography variant="body2" color="text.secondary">{label}</Typography>
          <Typography variant="h5" fontWeight={700}>{value}</Typography>
          {sub && <Typography variant="caption" color="text.secondary">{sub}</Typography>}
        </Box>
      </CardContent>
    </Card>
  );
}

export default function ResidentDashboardPage() {
  const { societyId } = useSociety();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [receipts, setReceipts] = useState([]);
  const [queries, setQueries] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [rRes, qRes, aRes] = await Promise.all([
          receiptApi.getMy(societyId, 0, 3),
          queryApi.getMyQueries(societyId, 0, 5),
          financeApi.getAnnouncements(societyId),
        ]);
        setReceipts(rRes.content ?? []);
        setQueries(qRes.content ?? []);
        setAnnouncements((aRes ?? []).slice(0, 3));
      } catch { setError("Failed to load dashboard data."); }
      finally { setLoading(false); }
    };
    load();
  }, [societyId]);

  if (loading) return <Box display="flex" justifyContent="center" mt={8}><CircularProgress /></Box>;

  const lastReceipt = receipts[0];
  const openQueries = queries.filter((q) => q.status === "OPEN").length;

  return (
    <Box p={3}>
      <Typography variant="h5" fontWeight={700} mb={3}>My Dashboard</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={2} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<ReceiptIcon fontSize="inherit" />}
            label="Last Payment"
            value={lastReceipt ? `₹${Number(lastReceipt.amount).toLocaleString("en-IN")}` : "—"}
            sub={lastReceipt ? lastReceipt.transactionDate : "No receipts yet"}
            color="success.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<QuestionAnswerIcon fontSize="inherit" />}
            label="Open Queries"
            value={openQueries}
            color="warning.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<AnnouncementIcon fontSize="inherit" />}
            label="Announcements"
            value={announcements.length}
            color="info.main"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Recent Receipts */}
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography fontWeight={600}>Recent Receipts</Typography>
                <Button size="small" onClick={() => navigate("/resident/receipts")}>View All</Button>
              </Box>
              <Divider sx={{ mb: 1 }} />
              {receipts.length === 0
                ? <Typography variant="body2" color="text.secondary">No receipts yet.</Typography>
                : receipts.map((r) => (
                    <Box key={r.id} display="flex" justifyContent="space-between" py={0.5}>
                      <Box>
                        <Typography variant="body2">{r.receiptNumber}</Typography>
                        <Typography variant="caption" color="text.secondary">{r.transactionDate}</Typography>
                      </Box>
                      <Typography fontWeight={600} color="success.main">
                        ₹{Number(r.amount).toLocaleString("en-IN")}
                      </Typography>
                    </Box>
                  ))
              }
            </CardContent>
          </Card>
        </Grid>

        {/* Announcements */}
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Typography fontWeight={600} mb={1}>Announcements</Typography>
              <Divider sx={{ mb: 1 }} />
              {announcements.length === 0
                ? <Typography variant="body2" color="text.secondary">No announcements.</Typography>
                : announcements.map((a) => (
                    <Box key={a.id} py={0.5}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body2" fontWeight={600}>{a.title}</Typography>
                        {a.isPinned && <Chip label="Pinned" size="small" color="warning" />}
                      </Box>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(a.createdAt).toLocaleDateString("en-IN")}
                      </Typography>
                    </Box>
                  ))
              }
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
