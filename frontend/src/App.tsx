import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";
import Layout from "@/components/Layout";
import LoginPage from "@/pages/LoginPage";
import DashboardPage from "@/pages/DashboardPage";
import LiveTrackingPage from "@/pages/LiveTrackingPage";
import DeliveriesPage from "@/pages/DeliveriesPage";
import DeliveryDetailPage from "@/pages/DeliveryDetailPage";
import IncidentsPage from "@/pages/IncidentsPage";
import DriverAppPage from "@/pages/DriverAppPage";
import FleetPage from "@/pages/FleetPage";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="tracking" element={<LiveTrackingPage />} />
          <Route path="deliveries" element={<DeliveriesPage />} />
          <Route path="deliveries/:id" element={<DeliveryDetailPage />} />
          <Route path="incidents" element={<IncidentsPage />} />
          <Route path="fleet" element={<FleetPage />} />
          <Route path="driver" element={<DriverAppPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
