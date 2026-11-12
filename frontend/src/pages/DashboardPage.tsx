import { useQuery } from "@tanstack/react-query";
import { reportingApi } from "@/api/reporting";
import { incidentsApi } from "@/api/incidents";
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
} from "recharts";
import { StatusBadge } from "@/components/StatusBadge";
import { format } from "date-fns";

const STATUS_COLORS: Record<string, string> = {
  PENDING: "#94a3b8",
  ASSIGNED: "#3b82f6",
  IN_TRANSIT: "#f59e0b",
  DELIVERED: "#22c55e",
  FAILED: "#ef4444",
  CANCELLED: "#cbd5e1",
};

export default function DashboardPage() {
  const { data: summary, isLoading } = useQuery({
    queryKey: ["operations-summary"],
    queryFn: reportingApi.operationsSummary,
    refetchInterval: 30_000,
  });

  const { data: openIncidents } = useQuery({
    queryKey: ["incidents-open"],
    queryFn: incidentsApi.open,
    refetchInterval: 30_000,
  });

  if (isLoading) return <div className="page-content"><div className="spinner" /></div>;
  if (!summary) return null;

  const statusChartData = Object.entries(summary.byStatus ?? {}).map(([status, count]) => ({
    status,
    count,
  }));

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Operations Dashboard</span>
        <span className="text-sm text-muted">{format(new Date(), "EEE d MMM yyyy")}</span>
      </div>

      <div className="page-content">
        <div className="stats-grid mb-6">
          <div className="stat-card">
            <div className="stat-label">Total Deliveries</div>
            <div className="stat-value">{summary.totalDeliveries}</div>
          </div>
          <div className="stat-card success">
            <div className="stat-label">Completed</div>
            <div className="stat-value">{summary.completedDeliveries}</div>
          </div>
          <div className="stat-card danger">
            <div className="stat-label">Failed</div>
            <div className="stat-value">{summary.failedDeliveries}</div>
          </div>
          <div className="stat-card warning">
            <div className="stat-label">Delayed</div>
            <div className="stat-value">{summary.delayedShipments}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Active Drivers</div>
            <div className="stat-value">{summary.activeDrivers}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">On-Time Rate</div>
            <div className="stat-value">{(summary.onTimeRate * 100).toFixed(0)}%</div>
          </div>
          <div className="stat-card danger">
            <div className="stat-label">High Risk</div>
            <div className="stat-value">{summary.highRiskDeliveries}</div>
          </div>
        </div>

        <div className="grid-2 mb-6">
          {/* Status distribution chart */}
          <div className="card">
            <div className="card-header">Deliveries by Status</div>
            <div className="card-body">
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={statusChartData} margin={{ top: 4, right: 8, bottom: 4, left: 0 }}>
                  <XAxis dataKey="status" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                    {statusChartData.map((entry) => (
                      <Cell key={entry.status} fill={STATUS_COLORS[entry.status] ?? "#3b82f6"} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Open incidents */}
          <div className="card">
            <div className="card-header flex justify-between items-center">
              Open Incidents
              <span className="badge badge-open">{openIncidents?.length ?? 0}</span>
            </div>
            <div className="card-body" style={{ padding: 0 }}>
              {!openIncidents?.length ? (
                <div className="card-body text-muted text-sm">No open incidents.</div>
              ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Type</th>
                        <th>Priority</th>
                        <th>Reported</th>
                      </tr>
                    </thead>
                    <tbody>
                      {openIncidents.slice(0, 6).map((inc) => (
                        <tr key={inc.id}>
                          <td className="text-sm">{inc.type.replace("_", " ")}</td>
                          <td>
                            <span className={`badge ${inc.priority <= 2 ? "badge-high" : inc.priority === 3 ? "badge-medium" : "badge-low"}`}>
                              P{inc.priority}
                            </span>
                          </td>
                          <td className="text-sm text-muted">
                            {format(new Date(inc.createdAt), "HH:mm dd/MM")}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
