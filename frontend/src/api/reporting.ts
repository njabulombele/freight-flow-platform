import client from "./client";
import type { OperationsSummary } from "@/types";

export const reportingApi = {
  operationsSummary: () =>
    client.get<OperationsSummary>("/reporting/operations-summary").then((r) => r.data),
};
