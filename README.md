# FreightFlow

AI-powered logistics operations, delivery visibility and exception management platform.

FreightFlow gives a distribution company one central platform to manage deliveries,
drivers, vehicles, customers and operational incidents. It answers five questions that
matter to logistics operations:

- Where is my delivery?
- Will it arrive on time?
- Why is it delayed?
- What should the dispatcher do?
- How much is the delay costing the business?

## Architecture

A modular Spring Boot backend (modular monolith), a React + TypeScript frontend
(dispatcher dashboard, admin portal, driver PWA), and a separate Python FastAPI
prediction service. Asynchronous events flow through a transactional outbox to Kafka.
We start lightweight with Docker Compose + PostgreSQL and introduce Kafka, Redis and
cloud infrastructure incrementally.

```
freight-flow-platform/
├── docker-compose.yml          # Local dev infrastructure
├── backend/                    # Spring Boot modular monolith (Java 21)
│   └── src/main/java/com/freightflow/
│       ├── common/              # config, security, audit, outbox, exceptions
│       ├── deliveries/         # delivery lifecycle, tracking, status updates
│       ├── fleet/               # vehicles and drivers
│       ├── customers/          # customers and delivery addresses
│       ├── incidents/           # exception management
│       ├── reporting/           # operations dashboard metrics
│       └── prediction/         # integration with the Python service
├── frontend/                   # React + TypeScript (Vite)
│   └── src/
│       ├── pages/              # dashboard, live tracking, incidents, driver app
│       ├── components/
│       ├── api/
│       ├── auth/
│       └── store/
├── prediction-service/         # Python FastAPI + scikit-learn
│   ├── app/
│   │   ├── main.py
│   │   ├── model.py
│   │   └── schemas.py
│   └── data/
└── .github/workflows/          # CI/CD
```

## Engineering challenges addressed

| Challenge | Solution |
|-----------|----------|
| Duplicate delivery updates | Idempotency keys |
| Lost events | Transactional outbox |
| Failed message processing | Retries and dead-letter queue |
| Slow API requests | Database optimisation and caching |
| Unauthorised access | RBAC and tenant isolation |
| System failures | Health checks and monitoring |
| Untraceable errors | Structured logs and distributed tracing |
| Unreliable delivery estimates | Prediction model and evaluation |
| Data tampering | Audit logs |
| Poor deployment reliability | Automated CI/CD and rollback strategy |

## Local development

Start the infrastructure with Docker Compose:

```bash
docker compose up -d postgres redis
```

Then run each service. See the `README.md` inside `backend/`, `frontend/` and
`prediction-service/` for service-specific instructions.

## Tech stack

| Area | Technology |
|------|-----------|
| Backend | Java 21, Spring Boot |
| Frontend | React, TypeScript, Vite |
| Database | PostgreSQL, PostGIS |
| Security | Spring Security, JWT, RBAC |
| Messaging | Apache Kafka |
| AI/ML | Python, FastAPI, scikit-learn |
| API documentation | OpenAPI / Swagger |
| Testing | JUnit 5, Mockito, Testcontainers, Playwright |
| Containers | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Monitoring | Prometheus, Grafana, OpenTelemetry |
| Maps | OpenStreetMap, routing integration |
