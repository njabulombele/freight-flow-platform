// ---- Shared domain types ----

export type UserRole = "ADMIN" | "MANAGER" | "DISPATCHER" | "DRIVER";

export interface AuthUser {
  userId: string;
  email: string;
  tenantId: string;
  role: UserRole;
  firstName: string;
  lastName: string;
  token: string;
}

export type DeliveryStatus =
  | "PENDING"
  | "ASSIGNED"
  | "IN_TRANSIT"
  | "DELIVERED"
  | "FAILED"
  | "CANCELLED";

export type RiskCategory = "LOW" | "MEDIUM" | "HIGH";

export interface Delivery {
  id: string;
  deliveryNumber: string;
  customerName: string;
  driverId: string | null;
  driverName: string | null;
  vehicleId: string | null;
  vehicleRegistration: string | null;
  status: DeliveryStatus;
  pickupAddress: string;
  deliveryAddress: string;
  latitude: number | null;
  longitude: number | null;
  plannedDistanceKm: number | null;
  plannedDurationMin: number | null;
  scheduledAt: string;
  eta: string | null;
  completedAt: string | null;
  delayMinutes: number | null;
  delayProbability: number | null;
  riskCategory: RiskCategory | null;
  failureReason: string | null;
  proofOfDeliveryUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Customer {
  id: string;
  name: string;
  contactName: string | null;
  phone: string | null;
  email: string | null;
  city: string;
  createdAt: string;
}

export interface Driver {
  id: string;
  name: string;
  phoneNumber: string | null;
  licenseNumber: string | null;
  status: "AVAILABLE" | "ON_DELIVERY" | "OFF_DUTY";
}

export interface Vehicle {
  id: string;
  registrationNumber: string;
  type: string;
  capacityKg: number | null;
  makeModel: string | null;
  status: "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "RETIRED";
}

export interface Incident {
  id: string;
  deliveryId: string | null;
  type: string;
  status: "OPEN" | "INVESTIGATING" | "RESOLVED" | "CLOSED";
  priority: number;
  description: string;
  reportedBy: string;
  resolution: string | null;
  createdAt: string;
  resolvedAt: string | null;
}

export interface OperationsSummary {
  totalDeliveries: number;
  completedDeliveries: number;
  failedDeliveries: number;
  delayedShipments: number;
  activeDrivers: number;
  onTimeRate: number;
  failureRate: number;
  highRiskDeliveries: number;
  byStatus: Record<DeliveryStatus, number>;
}

export interface TrackingUpdate {
  id: string;
  status: DeliveryStatus;
  latitude: number | null;
  longitude: number | null;
  note: string | null;
  reportedBy: string;
  createdAt: string;
}
