import React, { useEffect, useState } from "react";
import {
  Box, Typography, Grid, Card, CardContent, Divider, Button, TextField,
  Dialog, DialogTitle, DialogContent, DialogActions, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Alert,
  CircularProgress, Chip, Tab, Tabs
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { societyApi } from "../../api/societyApi";
import { useSociety } from "../../auth/AuthContext";

export default function SocietyManagementPage() {
  const { societyId } = useSociety();
  const [tab, setTab] = useState(0);
  const [wings, setWings] = useState([]);
  const [flats, setFlats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [wingDialog, setWingDialog] = useState(false);
  const [flatDialog, setFlatDialog] = useState(false);
  const [wingForm, setWingForm] = useState({ wingName: "", totalFloors: "" });
  const [flatForm, setFlatForm] = useState({ wingId: "", flatNumber: "", floorNumber: "" });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [w, f] = await Promise.all([
        societyApi.getWings(societyId),
        societyApi.getFlats(societyId),
      ]);
      setWings(Array.isArray(w) ? w : w.content ?? []);
      setFlats(f.content ?? []);
    } catch { setError("Failed to load data."); }
    finally { setLoading(false); }
  };

  const handleCreateWing = async () => {
    if (!wingForm.wingName) return;
    setSaving(true);
    try {
      await societyApi.createWing(societyId, wingForm.wingName, wingForm.totalFloors || undefined);
      setWingDialog(false); setWingForm({ wingName: "", totalFloors: "" });
      loadData();
    } catch (e) { setError(e.response?.data?.message || "Failed to create wing."); }
    finally { setSaving(false); }
  };

  const handleCreateFlat = async () => {
    if (!flatForm.wingId || !flatForm.flatNumber) return;
    setSaving(true);
    try {
      await societyApi.createFlat(societyId, {
        wingId: Number(flatForm.wingId),
        flatNumber: flatForm.flatNumber,
        floorNumber: flatForm.floorNumber ? Number(flatForm.floorNumber) : undefined,
      });
      setFlatDialog(false); setFlatForm({ wingId: "", flatNumber: "", floorNumber: "" });
      loadData();
    } catch (e) { setError(e.response?.data?.message || "Failed to create flat."); }
    finally { setSaving(false); }
  };

  return (
    <Box p={3}>
      <Typography variant="h5" fontWeight={700} mb={3}>Society Management</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label={`Wings (${wings.length})`} />
        <Tab label={`Flats (${flats.length})`} />
      </Tabs>

      {loading ? <CircularProgress /> : (
        <>
          {/* Wings Tab */}
          {tab === 0 && (
            <Box>
              <Box display="flex" justifyContent="flex-end" mb={2}>
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setWingDialog(true)}>
                  Add Wing
                </Button>
              </Box>
              <Grid container spacing={2}>
                {wings.map((w) => (
                  <Grid item xs={12} sm={6} md={4} key={w.id}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography fontWeight={700} variant="h6">{w.wingName}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {w.totalFloors ?? "?"} floors · {w.flatCount ?? 0} flats
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
                {wings.length === 0 && (
                  <Grid item xs={12}><Alert severity="info">No wings yet.</Alert></Grid>
                )}
              </Grid>
            </Box>
          )}

          {/* Flats Tab */}
          {tab === 1 && (
            <Box>
              <Box display="flex" justifyContent="flex-end" mb={2}>
                <Button variant="contained" startIcon={<AddIcon />} onClick={() => setFlatDialog(true)}>
                  Add Flat
                </Button>
              </Box>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Flat No.</TableCell>
                      <TableCell>Wing</TableCell>
                      <TableCell>Floor</TableCell>
                      <TableCell>Pay Ref</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {flats.map((f) => (
                      <TableRow key={f.id}>
                        <TableCell>{f.flatNumber}</TableCell>
                        <TableCell>{f.wingName}</TableCell>
                        <TableCell>{f.floorNumber ?? "—"}</TableCell>
                        <TableCell sx={{ fontFamily: "monospace", fontSize: 12 }}>
                          {f.paymentReferenceCode}
                        </TableCell>
                        <TableCell>
                          <Chip label={f.isOccupied ? "Occupied" : "Vacant"} size="small"
                            color={f.isOccupied ? "success" : "default"} />
                        </TableCell>
                      </TableRow>
                    ))}
                    {flats.length === 0 && (
                      <TableRow><TableCell colSpan={5} align="center">No flats yet.</TableCell></TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </>
      )}

      {/* Wing Dialog */}
      <Dialog open={wingDialog} onClose={() => setWingDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Add Wing</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          <TextField label="Wing Name *" value={wingForm.wingName} fullWidth
            onChange={(e) => setWingForm({ ...wingForm, wingName: e.target.value })} />
          <TextField label="Total Floors" type="number" value={wingForm.totalFloors} fullWidth
            onChange={(e) => setWingForm({ ...wingForm, totalFloors: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWingDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateWing} disabled={!wingForm.wingName || saving}>
            {saving ? "Creating..." : "Create"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Flat Dialog */}
      <Dialog open={flatDialog} onClose={() => setFlatDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Add Flat</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, pt: 2 }}>
          <TextField label="Wing ID *" type="number" value={flatForm.wingId} fullWidth
            onChange={(e) => setFlatForm({ ...flatForm, wingId: e.target.value })}
            helperText="Enter the numeric Wing ID" />
          <TextField label="Flat Number *" value={flatForm.flatNumber} fullWidth
            onChange={(e) => setFlatForm({ ...flatForm, flatNumber: e.target.value })} />
          <TextField label="Floor Number" type="number" value={flatForm.floorNumber} fullWidth
            onChange={(e) => setFlatForm({ ...flatForm, floorNumber: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFlatDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateFlat}
            disabled={!flatForm.wingId || !flatForm.flatNumber || saving}>
            {saving ? "Creating..." : "Create"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
