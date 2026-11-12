import client from "./client";
import type { Incident, Page } from "@/types";

export const incidentsApi = {
  list: (page = 0, size = 20) =>
    client.get<Page<Incident>>("/incidents", { params: { page, size } }).then((r) => r.data),

  open: () => client.get<Incident[]>("/incidents/open").then((r) => r.data),

  get: (id: string) => client.get<Incident>(`/incidents/${id}`).then((r) => r.data),

  create: (data: {
    type: string;
    description: string;
    deliveryId?: string;
    priority?: number;
  }) => client.post<Incident>("/incidents", data).then((r) => r.data),

  resolve: (id: string, resolution: string) =>
    client.post<Incident>(`/incidents/${id}/resolve`, { resolution }).then((r) => r.data),
};
