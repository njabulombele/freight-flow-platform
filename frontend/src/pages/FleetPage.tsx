import { useQuery } from "@tanstack/react-query";
import { fleetApi } from "@/api/fleet";
import { StatusBadge } from "@/components/StatusBadge";

export default function FleetPage() {
  const { data: drivers, isLoading: driversLoading } = useQuery({
    queryKey: ["all-drivers"],
    queryFn: fleetApi.allDrivers,
  });
  const { data: vehicles, isLoading: vehiclesLoading } = useQuery({
    queryKey: ["all-vehicles"],
    queryFn: fleetApi.allVehicles,
  });

  return (
    <>
      <div className="topbar">
        <span className="topbar-title">Fleet</span>
      </div>

      <div className="page-content">
        <div className="stats-grid mb-6">
          <div className="stat-card">
            <div className="stat-label">Total Drivers</div>
            <div className="stat-value">{drivers?.length ?? "—"}</div>
          </div>
          <div className="stat-card success">
            <div className="stat-label">Available Drivers</div>
            <div className="stat-value">{drivers?.filter((d) => d.status === "AVAILABLE").length ?? "—"}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Vehicles</div>
            <div className="stat-value">{vehicles?.length ?? "—"}</div>
          </div>
          <div className="stat-card warning">
            <div className="stat-label">In Maintenance</div>
            <div className="stat-value">{vehicles?.filter((v) => v.status === "MAINTENANCE").length ?? "—"}</div>
          </div>
        </div>

        <div className="grid-2">
          {/* Drivers */}
          <div className="card">
            <div className="card-header">Drivers</div>
            <div className="table-wrap">
              {driversLoading ? <div className="spinner" /> : (
                <table>
                  <thead>
                    <tr><th>Name</th><th>Phone</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {drivers?.map((d) => (
                      <tr key={d.id}>
                        <td className="text-sm font-bold">{d.name}</td>
                        <td className="text-sm text-muted">{d.phoneNumber ?? "—"}</td>
                        <td><StatusBadge value={d.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          {/* Vehicles */}
          <div className="card">
            <div className="card-header">Vehicles</div>
            <div className="table-wrap">
              {vehiclesLoading ? <div className="spinner" /> : (
                <table>
                  <thead>
                    <tr><th>Reg</th><th>Type</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {vehicles?.map((v) => (
                      <tr key={v.id}>
                        <td className="text-sm font-bold">{v.registrationNumber}</td>
                        <td className="text-sm">{v.makeModel ?? v.type}</td>
                        <td><StatusBadge value={v.status} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
