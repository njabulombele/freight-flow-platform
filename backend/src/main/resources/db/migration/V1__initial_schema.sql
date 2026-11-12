-- FreightFlow initial schema
-- Uses PostgreSQL with the uuid-ossp extension for UUID generation.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- ============================================================
-- Users
-- ============================================================
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    role          VARCHAR(50)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by    VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_tenant ON users (tenant_id);
CREATE INDEX idx_users_email  ON users (email);

-- ============================================================
-- Customers
-- ============================================================
CREATE TABLE customers (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id     VARCHAR(100) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    contact_name  VARCHAR(255),
    phone         VARCHAR(50),
    email         VARCHAR(255),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city          VARCHAR(100) NOT NULL,
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_tenant ON customers (tenant_id);

-- ============================================================
-- Vehicles
-- ============================================================
CREATE TABLE vehicles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           VARCHAR(100) NOT NULL,
    registration_number VARCHAR(50)  NOT NULL UNIQUE,
    type                VARCHAR(100) NOT NULL,
    capacity_kg         DOUBLE PRECISION,
    make_model          VARCHAR(255),
    status              VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_tenant ON vehicles (tenant_id);

-- ============================================================
-- Drivers
-- ============================================================
CREATE TABLE drivers (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           VARCHAR(100) NOT NULL,
    user_id             UUID,
    name                VARCHAR(255) NOT NULL,
    phone_number        VARCHAR(50),
    license_number      VARCHAR(100),
    assigned_vehicle_id UUID REFERENCES vehicles(id),
    status              VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drivers_tenant ON drivers (tenant_id);

-- ============================================================
-- Deliveries
-- ============================================================
CREATE TABLE deliveries (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id            VARCHAR(100) NOT NULL,
    delivery_number      VARCHAR(50)  NOT NULL UNIQUE,
    customer_id          UUID         NOT NULL REFERENCES customers(id),
    driver_id            UUID         REFERENCES drivers(id),
    vehicle_id           UUID         REFERENCES vehicles(id),
    status               VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    pickup_address       VARCHAR(500) NOT NULL,
    delivery_address     VARCHAR(500) NOT NULL,
    latitude             DOUBLE PRECISION,
    longitude            DOUBLE PRECISION,
    planned_distance_km  DOUBLE PRECISION,
    planned_duration_min INTEGER,
    scheduled_at         TIMESTAMPTZ  NOT NULL,
    eta                  TIMESTAMPTZ,
    completed_at         TIMESTAMPTZ,
    delay_minutes        INTEGER,
    delay_probability    DOUBLE PRECISION,
    risk_category        VARCHAR(20),
    delay_factors        JSONB,
    failure_reason       TEXT,
    proof_of_delivery_url VARCHAR(1000),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deliveries_tenant         ON deliveries (tenant_id);
CREATE INDEX idx_deliveries_status         ON deliveries (tenant_id, status);
CREATE INDEX idx_deliveries_scheduled      ON deliveries (tenant_id, scheduled_at);
CREATE INDEX idx_deliveries_driver         ON deliveries (driver_id);
CREATE INDEX idx_deliveries_risk           ON deliveries (tenant_id, risk_category);

-- ============================================================
-- Delivery tracking updates
-- ============================================================
CREATE TABLE delivery_updates (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_id UUID        NOT NULL REFERENCES deliveries(id),
    status      VARCHAR(50) NOT NULL,
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    note        TEXT,
    proof_url   VARCHAR(1000),
    reported_by VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_updates_delivery ON delivery_updates (delivery_id, created_at);

-- ============================================================
-- Incidents
-- ============================================================
CREATE TABLE incidents (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   VARCHAR(100) NOT NULL,
    delivery_id UUID         REFERENCES deliveries(id),
    type        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    priority    INTEGER      NOT NULL DEFAULT 3,
    description TEXT         NOT NULL,
    reported_by VARCHAR(255),
    resolution  TEXT,
    resolved_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_incidents_tenant ON incidents (tenant_id);
CREATE INDEX idx_incidents_status ON incidents (tenant_id, status);

-- ============================================================
-- Outbox events (transactional outbox pattern)
-- ============================================================
CREATE TABLE outbox_event (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    type           VARCHAR(100) NOT NULL,
    tenant_id      VARCHAR(100) NOT NULL,
    payload        JSONB        NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count    INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_pending    ON outbox_event (status, created_at) WHERE status = 'PENDING';
CREATE INDEX idx_outbox_aggregate  ON outbox_event (aggregate_type, aggregate_id);

-- ============================================================
-- Idempotency keys
-- ============================================================
CREATE TABLE idempotency_key (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key            VARCHAR(255) NOT NULL UNIQUE,
    tenant_id      VARCHAR(100) NOT NULL,
    request_hash   VARCHAR(64)  NOT NULL,
    response_status INTEGER     NOT NULL,
    response_body  TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_idempotency_key    ON idempotency_key (key);
CREATE INDEX idx_idempotency_expiry ON idempotency_key (expires_at);

-- ============================================================
-- Audit log
-- ============================================================
CREATE TABLE audit_log (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id   VARCHAR(100) NOT NULL,
    actor       VARCHAR(255) NOT NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   VARCHAR(255),
    old_value   JSONB,
    new_value   JSONB,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tenant     ON audit_log (tenant_id);
CREATE INDEX idx_audit_entity     ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_log (created_at DESC);
