import client from "./client";
import type { AuthUser } from "@/types";

export const authApi = {
  login: (email: string, password: string) =>
    client
      .post<AuthUser>("/auth/login", { email, password })
      .then((r) => r.data),
};
