import client from "./client";
import type { Driver, Vehicle } from "@/types";

export const fleetApi = {
  allDrivers: () => client.get<Driver[]>("/fleet/drivers/all").then((r) => r.data),
  allVehicles: () => client.get<Vehicle[]>("/fleet/vehicles/all").then((r) => r.data),
};
