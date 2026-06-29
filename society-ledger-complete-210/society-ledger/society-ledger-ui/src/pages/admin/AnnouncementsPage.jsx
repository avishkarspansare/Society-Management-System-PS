import React, { useEffect, useState } from "react";
import {
  Box, Typography, Button, Card, CardContent, CardActions, Chip, Divider,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, FormControlLabel,
  Switch, Alert, CircularProgress, IconButton
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import PushPinIcon from "@mui/icons-material/PushPin";
import { financeApi } from "../../api/financeApi";
import { useSociety } from "../../auth/AuthContext";

export default function AnnouncementsPage() {
  const { societyId } = useSociety();
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialog, setDialog] = useState(false);
  const [form, setForm] = useState({ title: "", content: "", isPinned: false });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadAnnouncements(); }, []);

  const loadAnnouncements = async () => {
    setLoading(true);
    try { setAnnouncements(await financeApi.getAnnouncements(societyId)); }
    catch { setError("Failed to load announcements."); }
    finally { setLoading(false); }
  };

  const handleCreate = async () => {
    if (!form.title || !form.content) return;
    setSaving(true);
    try {
      await financeApi.createAnnouncement(societyId, form.title, form.content, form.isPinned);
      setDialog(false); setForm({ title: "", content: "", isPinned: false });
      loadAnnouncements();
    } catch (e) { setError("Failed to create announcement."); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    try { await financeApi.deleteAnnouncement(societyId, id); loadAnnouncements(); }
    catch { setError("Failed to delete announcement."); }
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5" fontWeight={700}>Announcements</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialog(true)}>
          New Announcement
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          {announcements.length === 0
            ? <Alert severity="info">No announcements yet.</Alert>
            : announcements.map((a) => (
                <Card key={a.id} variant="outlined"
                  sx={{ borderLeft: a.isPinned ? "4px solid" : "1px solid",
                    borderLeftColor: a.isPinned ? "warning.main" : "divider" }}>
                  <CardContent>
                    <Box display="flex" alignItems="center" gap={1} mb={0.5}>
                      {a.isPinned && <PushPinIcon fontSize="small" color="warning" />}
                      <Typography fontWeight={700}>{a.title}</Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">{a.content}</Typography>
                    <Typography variant="caption" color="text.disabled" mt={1} display="block">
                      {new Date(a.createdAt).toLocaleDateString("en-IN")}
                    </Typography>
                  </CardContent>
                  <CardActions sx={{ pt: 0 }}>
                    <IconButton size="small" color="error" onClick={() => handleDelete(a.id)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </CardActions>
                </Card>
              ))
          }
        </Box>
      )}

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>New Announcement</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          <TextField label="Title *" value={form.title} fullWidth
            onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <TextField label="Content *" multiline rows={4} value={form.content} fullWidth
            onChange={(e) => setForm({ ...form, content: e.target.value })} />
          <FormControlLabel
            control={<Switch checked={form.isPinned}
              onChange={(e) => setForm({ ...form, isPinned: e.target.checked })} />}
            label="Pin this announcement"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate}
            disabled={!form.title || !form.content || saving}>
            {saving ? "Posting..." : "Post"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
