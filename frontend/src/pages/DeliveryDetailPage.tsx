import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { deliveriesApi } from "@/api/deliveries";
import { fleetApi } from "@/api/fleet";
import { StatusBadge } from "@/components/StatusBadge";
import { format } from "date-fns";
import { FormEvent, useState } from "react";

export default function DeliveryDetailPage() {
  const { id } = useParams<{ id: string }>();
  const qc = useQueryClient();
  const navigate = useNavigate();
  const [assignModal, setAssignModal] = useState(false);
  const [driverId, setDriverId] = useState("");
  const [vehicleId, setVehicleId] = useState("");

  const { data: delivery, isLoading } = useQuery({
    queryKey: ["delivery", id],
    queryFn: () => deliveriesApi.get(id!),
    enabled: !!id,
  });

  const { data: tracking } = useQuery({
    queryKey: ["delivery-tracking", id],
    queryFn: () => deliveriesApi.tracking(id!),
    enabled: !!id,
    refetchInterval: 10_000,
  });

  const { data: drivers } = useQuery({
    queryKey: ["all-drivers"],
    queryFn: fleetApi.allDrivers,
    enabled: assignModal,
  });

  const { data: vehicles } = useQuery({
    queryKey: ["all-vehicles"],
    queryFn: fleetApi.allVehicles,
    enabled: assignModal,
  });

  const assignMutation = useMutation({
    mutationFn: () => deliveriesApi.assign(id!, driverId, vehicleId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["delivery", id] });
      setAssignModal(false);
    },
  });

  if (isLoading) return <div className="page-content"><div className="spinner" /></div>;
  if (!delivery) return null;

  const factors = delivery.riskCategory
    ? JSON.parse((delivery as any).delayFactors ?? "{}")
    : null;

  return (
    <>
      <div className="topbar">
        <div className="flex items-center gap-4">
          <button className="btn btn-outline" style={{ padding: ".3rem .7rem" }} onClick={() => navigate(-1)}>
            ← Back
          </button>
          <span className="topbar-title">
            {delivery.deliveryNumber}
          </span>
          <StatusBadge value={delivery.status} />
        </div>
        {delivery.status === "PENDING" && (
          <button className="btn btn-primary" onClick={() => setAssignModal(true)}>
            Assign Driver &amp; Vehicle
          </button>
        )}
      </div>

      <div className="page-content">
        <div className="grid-2 mb-6">
          {/* Delivery info */}
          <div className="card card-body">
            <h3 style={{ marginBottom: ".75rem", fontWeight: 600 }}>Delivery Info</h3>
            <dl style={{ display: "grid", gridTemplateColumns: "120px 1fr", gap: ".4rem .75rem", fontSize: ".875rem" }}>
              <dt className="text-muted">Customer</dt><dd>{delivery.customerName}</dd>
              <dt className="text-muted">Driver</dt><dd>{delivery.driverName ?? "—"}</dd>
              <dt className="text-muted">Vehicle</dt><dd>{delivery.vehicleRegistration ?? "—"}</dd>
              <dt className="text-muted">Pickup</dt><dd>{delivery.pickupAddress}</dd>
              <dt className="text-muted">Delivery</dt><dd>{delivery.deliveryAddress}</dd>
              <dt className="text-muted">Scheduled</dt><dd>{format(new Date(delivery.scheduledAt), "dd MMM yyyy HH:mm")}</dd>
              {delivery.eta && <><dt className="text-muted">ETA</dt><dd>{format(new Date(delivery.eta), "dd MMM yyyy HH:mm")}</dd></>}
              {delivery.completedAt && <><dt className="text-muted">Completed</dt><dd>{format(new Date(delivery.completedAt), "dd MMM yyyy HH:mm")}</dd></>}
              {delivery.failureReason && <><dt className="text-muted">Failure reason</dt><dd className="text-danger">{delivery.failureReason}</dd></>}
              {delivery.proofOfDeliveryUrl && (
                <><dt className="text-muted">Proof</dt><dd><a href={delivery.proofOfDeliveryUrl} target="_blank" rel="noreferrer" className="btn btn-outline" style={{ padding: ".2rem .5rem", fontSize: ".8rem" }}>View POD</a></dd></>
              )}
            </dl>
          </div>

          {/* AI prediction */}
          <div className="card card-body">
            <h3 style={{ marginBottom: ".75rem", fontWeight: 600 }}>AI Delay Prediction</h3>
            {delivery.delayProbability == null ? (
              <p className="text-sm text-muted">No prediction available yet.</p>
            ) : (
              <>
                <div className="stats-grid" style={{ marginBottom: ".75rem" }}>
                  <div className="stat-card" style={{ padding: ".75rem 1rem" }}>
                    <div className="stat-label">Late Probability</div>
                    <div className="stat-value" style={{ fontSize: "1.5rem" }}>
                      {(delivery.delayProbability * 100).toFixed(0)}%
                    </div>
                  </div>
                  <div className="stat-card warning" style={{ padding: ".75rem 1rem" }}>
                    <div className="stat-label">Est. Delay</div>
                    <div className="stat-value" style={{ fontSize: "1.5rem" }}>
                      {delivery.delayMinutes ?? 0} min
                    </div>
                  </div>
                  <div className="stat-card" style={{ padding: ".75rem 1rem" }}>
                    <div className="stat-label">Risk</div>
                    <div style={{ marginTop: ".35rem" }}>
                      {delivery.riskCategory && <StatusBadge value={delivery.riskCategory} />}
                    </div>
                  </div>
                </div>
                {factors && Object.keys(factors).length > 0 && (
                  <div>
                    <p className="text-sm" style={{ fontWeight: 600, marginBottom: ".4rem" }}>Contributing factors</p>
                    {Object.entries<number>(factors)
                      .sort(([, a], [, b]) => b - a)
                      .slice(0, 5)
                      .map(([key, val]) => (
                        <div key={key} style={{ display: "flex", alignItems: "center", gap: ".5rem", marginBottom: ".3rem" }}>
                          <span className="text-sm" style={{ width: 180 }}>{key.replace(/_/g, " ")}</span>
                          <div style={{ flex: 1, background: "var(--color-border)", borderRadius: 4, height: 8 }}>
                            <div style={{ width: `${(val * 100).toFixed(1)}%`, background: "var(--color-primary)", height: "100%", borderRadius: 4 }} />
                          </div>
                          <span className="text-sm text-muted" style={{ width: 40, textAlign: "right" }}>
                            {(val * 100).toFixed(1)}%
                          </span>
                        </div>
                      ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* Tracking timeline */}
        <div className="card">
          <div className="card-header">Tracking History</div>
          <div className="card-body">
            {!tracking?.length ? (
              <p className="text-sm text-muted">No tracking events yet.</p>
            ) : (
              <ol style={{ listStyle: "none", paddingLeft: 0 }}>
                {tracking.map((t, i) => (
                  <li key={t.id} style={{ display: "flex", gap: "1rem", marginBottom: ".75rem" }}>
                    <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                      <div style={{ width: 10, height: 10, borderRadius: "50%", background: "var(--color-primary)", marginTop: 4 }} />
                      {i < tracking.length - 1 && (
                        <div style={{ width: 2, flex: 1, background: "var(--color-border)", margin: ".2rem 0" }} />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2" style={{ marginBottom: ".15rem" }}>
                        <StatusBadge value={t.status} />
                        <span className="text-sm text-muted">{format(new Date(t.createdAt), "dd MMM HH:mm")}</span>
                      </div>
                      {t.note && <p className="text-sm">{t.note}</p>}
                      {t.reportedBy && (
                        <p className="text-sm text-muted">by {t.reportedBy}</p>
                      )}
                    </div>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      </div>

      {/* Assign modal */}
      {assignModal && (
        <div
          style={{
            position: "fixed", inset: 0, background: "rgba(0,0,0,.4)",
            display: "flex", alignItems: "center", justifyContent: "center", zIndex: 999,
          }}
          onClick={() => setAssignModal(false)}
        >
          <div
            className="card"
            style={{ width: 400, padding: "1.5rem" }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ marginBottom: "1rem" }}>Assign Driver &amp; Vehicle</h3>
            <form
              onSubmit={(e: FormEvent) => {
                e.preventDefault();
                assignMutation.mutate();
              }}
            >
              <div className="form-group">
                <label htmlFor="driver-select">Driver</label>
                <select id="driver-select" value={driverId} onChange={(e) => setDriverId(e.target.value)} required>
                  <option value="">Select driver…</option>
                  {drivers?.filter((d) => d.status === "AVAILABLE").map((d) => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="vehicle-select">Vehicle</label>
                <select id="vehicle-select" value={vehicleId} onChange={(e) => setVehicleId(e.target.value)} required>
                  <option value="">Select vehicle…</option>
                  {vehicles?.filter((v) => v.status === "AVAILABLE").map((v) => (
                    <option key={v.id} value={v.id}>{v.registrationNumber} — {v.makeModel}</option>
                  ))}
                </select>
              </div>
              <div className="flex gap-2" style={{ marginTop: "1rem" }}>
                <button type="submit" className="btn btn-primary" disabled={assignMutation.isPending}>
                  {assignMutation.isPending ? "Assigning…" : "Assign"}
                </button>
                <button type="button" className="btn btn-outline" onClick={() => setAssignModal(false)}>Cancel</button>
              </div>
              {assignMutation.isError && (
                <div className="alert alert-error" style={{ marginTop: ".75rem" }}>
                  Assignment failed. Please try again.
                </div>
              )}
            </form>
          </div>
        </div>
      )}
    </>
  );
}
