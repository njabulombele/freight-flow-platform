# FreightFlow Frontend

React + TypeScript + Vite dispatcher dashboard, admin portal, and driver PWA.

## Running locally

```bash
cd frontend
npm install
npm run dev
```

App at `http://localhost:5173`

The Vite dev server proxies `/api` to `http://localhost:8080` (the Spring Boot backend).

## Pages

| Route | Description |
|-------|-------------|
| `/dashboard` | Operations dashboard with stats and open incidents |
| `/tracking` | Live map with active deliveries |
| `/deliveries` | Paginated delivery list |
| `/deliveries/:id` | Delivery detail with AI prediction and tracking timeline |
| `/incidents` | Incident management |
| `/fleet` | Drivers and vehicles |
| `/driver` | Driver-facing status update app |

## Stack

- **React 18** + TypeScript
- **TanStack Query** — server state, caching, background refresh
- **Zustand** (persisted) — auth state
- **React Router 6** — routing
- **Recharts** — dashboard charts
- **React Leaflet + OpenStreetMap** — live map (no API key needed)
- **Axios** — HTTP client with JWT interceptor and 401 redirect
