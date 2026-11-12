import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { incidentsApi } from "@/api/incidents";
import { StatusBadge } from "@/components/StatusBadge";
import { format } from "date-fns";

const INCIDENT_TYPES = [
  "VEHICLE_BREAKDOWN",
  "FAILED_DELIVERY",
  "ROUTE_DISRUPTION",
  "TRAFFIC_DELAY",
  "SECURITY_INCIDENT",
  "OTHER",
];

export default function IncidentsPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ type: "OTHER", description: "", priority: 3 });
  const [resolving, setResolving] = useState<string | null>(null);
  const [resolution, setResolution] = useState("");

  const { data, isLoading } = useQuery({
    queryKey: ["incidents", page],
    queryFn: () => incidentsApi.list(page, 20),
  });

  const createMutation = useMutation({
    mutationFn: () => incidentsApi.create(form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["incidents"] });
      setCreating(false);
      setForm({ type: "OTHER", description: "", priority: 3 });
    },
  });

  const resolveMutation = useMutation({
    mutationFn: (id: string) => incidentsApi.resolve(id, resolution),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["incidents"] });
      setResolving(null);
      setResolution("");
    },
  });

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Incident Management</span>
        <button className="btn btn-primary" onClick={() => setCreating(true)}>
          + Report Incident
        </button>
      </div>

      <div className="page-content">
        <div className="card">
          <div className="table-wrap">
            {isLoading ? (
              <div className="spinner" />
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Description</th>
                    <th>Reported by</th>
                    <th>Created</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {data?.content.map((inc) => (
                    <tr key={inc.id}>
                      <td className="text-sm">{inc.type.replace(/_/g, " ")}</td>
                      <td><StatusBadge value={inc.status} /></td>
                      <td>
                        <span className={`badge ${inc.priority <= 2 ? "badge-high" : inc.priority === 3 ? "badge-medium" : "badge-low"}`}>
                          P{inc.priority}
                        </span>
                      </td>
                      <td className="text-sm" style={{ maxWidth: 300 }}>{inc.description}</td>
                      <td className="text-sm text-muted">{inc.reportedBy}</td>
                      <td className="text-sm text-muted">{format(new Date(inc.createdAt), "dd/MM HH:mm")}</td>
                      <td>
                        {inc.status === "OPEN" || inc.status === "INVESTIGATING" ? (
                          <button
                            className="btn btn-outline"
                            style={{ padding: ".3rem .6rem", fontSize: ".8rem" }}
                            onClick={() => setResolving(inc.id)}
                          >
                            Resolve
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex gap-2" style={{ padding: ".75rem 1rem", borderTop: "1px solid var(--color-border)" }}>
              <button className="btn btn-outline" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>← Prev</button>
              <span className="text-sm text-muted" style={{ lineHeight: "2.2" }}>Page {page + 1} of {data.totalPages}</span>
              <button className="btn btn-outline" disabled={page >= data.totalPages - 1} onClick={() => setPage((p) => p + 1)}>Next →</button>
            </div>
          )}
        </div>
      </div>

      {/* Create incident modal */}
      {creating && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,.4)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 999 }} onClick={() => setCreating(false)}>
          <div className="card" style={{ width: 420, padding: "1.5rem" }} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ marginBottom: "1rem" }}>Report Incident</h3>
            <form onSubmit={(e) => { e.preventDefault(); createMutation.mutate(); }}>
              <div className="form-group">
                <label htmlFor="inc-type">Type</label>
                <select id="inc-type" value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value }))}>
                  {INCIDENT_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, " ")}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="inc-priority">Priority (1=highest, 5=lowest)</label>
                <select id="inc-priority" value={form.priority} onChange={(e) => setForm((f) => ({ ...f, priority: Number(e.target.value) }))}>
                  {[1, 2, 3, 4, 5].map((p) => <option key={p} value={p}>P{p}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="inc-desc">Description</label>
                <textarea id="inc-desc" rows={3} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} required />
              </div>
              <div className="flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={createMutation.isPending}>{createMutation.isPending ? "Saving…" : "Save"}</button>
                <button type="button" className="btn btn-outline" onClick={() => setCreating(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Resolve modal */}
      {resolving && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,.4)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 999 }} onClick={() => setResolving(null)}>
          <div className="card" style={{ width: 400, padding: "1.5rem" }} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ marginBottom: "1rem" }}>Resolve Incident</h3>
            <form onSubmit={(e) => { e.preventDefault(); resolveMutation.mutate(resolving); }}>
              <div className="form-group">
                <label htmlFor="resolution">Resolution notes</label>
                <textarea id="resolution" rows={3} value={resolution} onChange={(e) => setResolution(e.target.value)} required />
              </div>
              <div className="flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={resolveMutation.isPending}>{resolveMutation.isPending ? "Saving…" : "Mark Resolved"}</button>
                <button type="button" className="btn btn-outline" onClick={() => setResolving(null)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
