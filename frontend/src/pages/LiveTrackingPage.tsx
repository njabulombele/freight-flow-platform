import { useQuery } from "@tanstack/react-query";
import { deliveriesApi } from "@/api/deliveries";
import { StatusBadge } from "@/components/StatusBadge";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import type { Delivery } from "@/types";
import { useState } from "react";
import { Link } from "react-router-dom";

const JHB: [number, number] = [-26.2041, 28.0473];

export default function LiveTrackingPage() {
  const [selected, setSelected] = useState<Delivery | null>(null);

  const { data: deliveries, isLoading } = useQuery({
    queryKey: ["deliveries-active"],
    queryFn: deliveriesApi.active,
    refetchInterval: 15_000,
  });

  const withCoords = (deliveries ?? []).filter(
    (d) => d.latitude != null && d.longitude != null
  );

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Live Delivery Tracking</span>
        <span className="text-sm text-muted">
          {deliveries?.length ?? 0} active deliveries · auto-refreshes every 15 s
        </span>
      </div>

      <div className="page-content" style={{ padding: 0, display: "flex", height: "calc(100vh - 54px)" }}>
        {/* Sidebar list */}
        <div
          style={{
            width: 320,
            overflowY: "auto",
            borderRight: "1px solid var(--color-border)",
            background: "#fff",
          }}
        >
          {isLoading && <div className="spinner" />}
          {(deliveries ?? []).map((d) => (
            <div
              key={d.id}
              onClick={() => setSelected(d)}
              style={{
                padding: "1rem",
                borderBottom: "1px solid var(--color-border)",
                cursor: "pointer",
                background: selected?.id === d.id ? "#eff6ff" : "transparent",
              }}
            >
              <div className="flex justify-between items-center mb-4" style={{ marginBottom: ".25rem" }}>
                <span className="font-bold" style={{ fontSize: ".875rem" }}>
                  {d.deliveryNumber}
                </span>
                <StatusBadge value={d.status} />
              </div>
              <div className="text-sm text-muted">{d.customerName}</div>
              <div className="text-sm" style={{ marginTop: ".25rem" }}>
                📍 {d.deliveryAddress}
              </div>
              {d.driverName && (
                <div className="text-sm text-muted">👤 {d.driverName}</div>
              )}
              {d.riskCategory && d.riskCategory !== "LOW" && (
                <div style={{ marginTop: ".35rem" }}>
                  <StatusBadge value={d.riskCategory} />
                  {d.delayProbability != null && (
                    <span className="text-sm text-muted" style={{ marginLeft: ".4rem" }}>
                      {(d.delayProbability * 100).toFixed(0)}% late risk
                    </span>
                  )}
                </div>
              )}
            </div>
          ))}
          {!isLoading && deliveries?.length === 0 && (
            <div className="card-body text-muted text-sm">No active deliveries.</div>
          )}
        </div>

        {/* Map */}
        <div style={{ flex: 1 }}>
          <MapContainer center={JHB} zoom={10} style={{ height: "100%", width: "100%" }}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {withCoords.map((d) => (
              <Marker key={d.id} position={[d.latitude!, d.longitude!]}>
                <Popup>
                  <strong>{d.deliveryNumber}</strong>
                  <br />
                  {d.customerName}
                  <br />
                  <StatusBadge value={d.status} />
                  {d.riskCategory && (
                    <>
                      <br />
                      Risk: <StatusBadge value={d.riskCategory} />
                    </>
                  )}
                  <br />
                  <Link to={`/deliveries/${d.id}`}>View details →</Link>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
    </>
  );
}
