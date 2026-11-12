import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { deliveriesApi } from "@/api/deliveries";
import { StatusBadge } from "@/components/StatusBadge";
import { format } from "date-fns";

export default function DeliveriesPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ["deliveries", page],
    queryFn: () => deliveriesApi.list(page, 20),
  });

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Deliveries</span>
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
                    <th>#</th>
                    <th>Customer</th>
                    <th>Driver</th>
                    <th>Status</th>
                    <th>Risk</th>
                    <th>Delay %</th>
                    <th>Scheduled</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {data?.content.map((d) => (
                    <tr key={d.id}>
                      <td className="font-bold text-sm">{d.deliveryNumber}</td>
                      <td className="text-sm">{d.customerName}</td>
                      <td className="text-sm">{d.driverName ?? "—"}</td>
                      <td>
                        <StatusBadge value={d.status} />
                      </td>
                      <td>
                        {d.riskCategory ? (
                          <StatusBadge value={d.riskCategory} />
                        ) : (
                          "—"
                        )}
                      </td>
                      <td className="text-sm">
                        {d.delayProbability != null
                          ? `${(d.delayProbability * 100).toFixed(0)}%`
                          : "—"}
                      </td>
                      <td className="text-sm text-muted">
                        {format(new Date(d.scheduledAt), "dd/MM HH:mm")}
                      </td>
                      <td>
                        <Link to={`/deliveries/${d.id}`} className="btn btn-outline" style={{ padding: ".3rem .7rem", fontSize: ".8rem" }}>
                          View
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex gap-2" style={{ padding: ".75rem 1rem", borderTop: "1px solid var(--color-border)" }}>
              <button
                className="btn btn-outline"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                ← Prev
              </button>
              <span className="text-sm text-muted" style={{ lineHeight: "2.2" }}>
                Page {page + 1} of {data.totalPages}
              </span>
              <button
                className="btn btn-outline"
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Next →
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
