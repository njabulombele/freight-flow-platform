import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";

const navItems = [
  { to: "/dashboard", label: "Dashboard", icon: "📊" },
  { to: "/tracking", label: "Live Tracking", icon: "🗺️" },
  { to: "/deliveries", label: "Deliveries", icon: "📦" },
  { to: "/incidents", label: "Incidents", icon: "⚠️" },
  { to: "/fleet", label: "Fleet", icon: "🚛" },
  { to: "/driver", label: "Driver App", icon: "📱" },
];

export default function Layout() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <div className="shell">
      <aside className="sidebar" aria-label="Main navigation">
        <div className="sidebar-logo">
          Freight<span>Flow</span>
        </div>
        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-item${isActive ? " active" : ""}`}
            >
              <span aria-hidden="true">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="nav-item text-sm" style={{ color: "#94a3b8", marginBottom: ".25rem" }}>
            {user?.firstName} {user?.lastName}
          </div>
          <div className="nav-item text-sm text-muted" style={{ fontSize: ".75rem", marginBottom: ".5rem" }}>
            {user?.role}
          </div>
          <button className="btn btn-outline w-full" style={{ color: "#f8fafc", borderColor: "#334155" }} onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </aside>

      <div className="main-area">
        <Outlet />
      </div>
    </div>
  );
}
