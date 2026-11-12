import client from "./client";
import type { Delivery, Page, TrackingUpdate } from "@/types";

export const deliveriesApi = {
  list: (page = 0, size = 20) =>
    client.get<Page<Delivery>>("/deliveries", { params: { page, size } }).then((r) => r.data),

  active: () => client.get<Delivery[]>("/deliveries/active").then((r) => r.data),

  get: (id: string) => client.get<Delivery>(`/deliveries/${id}`).then((r) => r.data),

  tracking: (id: string) =>
    client.get<TrackingUpdate[]>(`/deliveries/${id}/tracking`).then((r) => r.data),

  create: (data: {
    customerId: string;
    pickupAddress: string;
    deliveryAddress: string;
    latitude?: number;
    longitude?: number;
    plannedDistanceKm?: number;
    plannedDurationMin?: number;
    scheduledAt: string;
  }) => client.post<Delivery>("/deliveries", data).then((r) => r.data),

  assign: (id: string, driverId: string, vehicleId: string) =>
    client.post<Delivery>(`/deliveries/${id}/assign`, { driverId, vehicleId }).then((r) => r.data),

  update: (
    id: string,
    data: { status: string; latitude?: number; longitude?: number; note?: string; proofOfDeliveryUrl?: string },
    idempotencyKey?: string
  ) =>
    client
      .post<Delivery>(`/deliveries/${id}/updates`, data, {
        headers: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : {},
      })
      .then((r) => r.data),
};
