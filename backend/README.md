# FreightFlow Backend

Modular Spring Boot (Java 21) backend.

## Running locally

1. Start PostgreSQL and Redis:

```bash
docker compose up -d postgres redis
```

The database runs on port **5433** (to avoid conflicts with other local PostgreSQL instances).

2. Copy `.env.example` to `.env` at the project root and adjust as needed.

3. Run the application:

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`  
Swagger UI at `http://localhost:8080/swagger-ui.html`

## Module structure

```
com.freightflow
├── FreightFlowApplication.java
├── common/
│   ├── config/        # JPA auditing, Redis, OpenAPI config
│   ├── security/      # JWT, RBAC, security filter chain
│   ├── tenant/        # Thread-local tenant context
│   ├── audit/         # Append-only audit log
│   ├── outbox/        # Transactional outbox + Kafka relay
│   ├── idempotency/   # Idempotency key store
│   └── exception/     # Global exception handler, error responses
├── users/             # User management, auth (login/register)
├── customers/         # Customer CRUD
├── fleet/             # Vehicles and drivers
├── deliveries/        # Delivery lifecycle, tracking updates
├── incidents/         # Exception management, Kafka consumer
├── reporting/         # Dashboard metrics (cached)
└── prediction/        # Python prediction service client + scheduler
```

## Engineering patterns

| Pattern | Location |
|---------|----------|
| Transactional outbox | `common/outbox/` |
| Idempotency keys | `common/idempotency/` + `DeliveryController` |
| Audit logs | `common/audit/` |
| Tenant isolation | `common/tenant/TenantContext` + service layer checks |
| RBAC | `common/security/SecurityConfig` + `@PreAuthorize` |
| Caching | `reporting/ReportingService` + Redis |
| Async events | Kafka via outbox relay |
| Dead-letter queue | `common/outbox/OutboxRelay.MAX_RETRIES` |

## Running tests

```bash
./mvnw test
```
