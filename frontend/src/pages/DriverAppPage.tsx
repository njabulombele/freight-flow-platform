import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { deliveriesApi } from "@/api/deliveries";
import { StatusBadge } from "@/components/StatusBadge";
import { format } from "date-fns";
import type { Delivery, DeliveryStatus } from "@/types";
import { v4 as uuidv4 } from "@/utils/uuid";

const DRIVER_TRANSITIONS: Record<DeliveryStatus, DeliveryStatus[]> = {
  ASSIGNED: ["IN_TRANSIT"],
  IN_TRANSIT: ["DELIVERED", "FAILED"],
  PENDING: [],
  DELIVERED: [],
  FAILED: [],
  CANCELLED: [],
};

export default function DriverAppPage() {
  const qc = useQueryClient();
  const [updating, setUpdating] = useState<Delivery | null>(null);
  const [form, setForm] = useState({ status: "" as DeliveryStatus, note: "", proofUrl: "" });
  const [submitError, setSubmitError] = useState("");

  const { data: deliveries, isLoading } = useQuery({
    queryKey: ["deliveries-active"],
    queryFn: deliveriesApi.active,
    refetchInterval: 15_000,
  });

  const updateMutation = useMutation({
    mutationFn: () => {
      const idempotencyKey = uuidv4();
      return deliveriesApi.update(
        updating!.id,
        {
          status: form.status,
          note: form.note || undefined,
          proofOfDeliveryUrl: form.proofUrl || undefined,
        },
        idempotencyKey
      );
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["deliveries-active"] });
      setUpdating(null);
      setSubmitError("");
    },
    onError: () => {
      setSubmitError("Update failed. Check your connection and try again.");
    },
  });

  function openUpdate(delivery: Delivery) {
    const transitions = DRIVER_TRANSITIONS[delivery.status];
    setForm({ status: transitions[0] ?? ("" as DeliveryStatus), note: "", proofUrl: "" });
    setUpdating(delivery);
    setSubmitError("");
  }

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Driver App</span>
        <span className="text-sm text-muted">
          Update delivery status · submit proof of delivery
        </span>
      </div>

      <div className="page-content" style={{ maxWidth: 640 }}>
        {isLoading && <div className="spinner" />}

        {!isLoading && !deliveries?.length && (
          <div className="alert alert-info">No active deliveries assigned.</div>
        )}

        {deliveries?.map((d) => {
          const transitions = DRIVER_TRANSITIONS[d.status];
          return (
            <div key={d.id} className="card mb-4" style={{ marginBottom: "1rem" }}>
              <div className="card-header flex justify-between items-center">
                <span>{d.deliveryNumber}</span>
                <StatusBadge value={d.status} />
              </div>
              <div className="card-body">
                <div className="text-sm mb-4" style={{ marginBottom: ".75rem" }}>
                  <strong>Customer:</strong> {d.customerName}
                </div>
                <div className="text-sm mb-4" style={{ marginBottom: ".75rem" }}>
                  <strong>Deliver to:</strong> {d.deliveryAddress}
                </div>
                <div className="text-sm text-muted mb-4" style={{ marginBottom: ".75rem" }}>
                  Scheduled: {format(new Date(d.scheduledAt), "dd MMM HH:mm")}
                </div>
                {d.riskCategory && d.riskCategory !== "LOW" && (
                  <div className="alert alert-info" style={{ marginBottom: ".75rem" }}>
                    ⚠️ This delivery is flagged as <strong>{d.riskCategory} RISK</strong>
                    {d.delayProbability != null && ` (${(d.delayProbability * 100).toFixed(0)}% chance of delay)`}.
                  </div>
                )}
                {transitions.length > 0 && (
                  <button className="btn btn-primary" onClick={() => openUpdate(d)}>
                    Update Status
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Update modal */}
      {updating && (
        <div
          style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,.45)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 999 }}
          onClick={() => setUpdating(null)}
        >
          <div className="card" style={{ width: 420, padding: "1.5rem" }} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ marginBottom: "1rem" }}>Update: {updating.deliveryNumber}</h3>

            <form onSubmit={(e) => { e.preventDefault(); updateMutation.mutate(); }}>
              <div className="form-group">
                <label htmlFor="status-sel">New Status</label>
                <select
                  id="status-sel"
                  value={form.status}
                  onChange={(e) => setForm((f) => ({ ...f, status: e.target.value as DeliveryStatus }))}
                  required
                >
                  {DRIVER_TRANSITIONS[updating.status].map((s) => (
                    <option key={s} value={s}>{s.replace("_", " ")}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="note">Note (required for FAILED)</label>
                <textarea
                  id="note"
                  rows={2}
                  value={form.note}
                  onChange={(e) => setForm((f) => ({ ...f, note: e.target.value }))}
                  required={form.status === "FAILED"}
                />
              </div>
              <div className="form-group">
                <label htmlFor="proof-url">Proof of Delivery URL</label>
                <input
                  id="proof-url"
                  type="url"
                  value={form.proofUrl}
                  onChange={(e) => setForm((f) => ({ ...f, proofUrl: e.target.value }))}
                  placeholder="https://…"
                  required={form.status === "DELIVERED"}
                />
              </div>

              {submitError && <div className="alert alert-error">{submitError}</div>}

              <div className="flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? "Submitting…" : "Submit"}
                </button>
                <button type="button" className="btn btn-outline" onClick={() => setUpdating(null)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
