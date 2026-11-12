package com.freightflow.common.idempotency;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores the idempotency key supplied by a client along with the original request
 * hash and the response. A repeat request with the same key returns the stored
 * response instead of reprocessing, preventing duplicate side effects.
 */
@Entity
@Table(name = "idempotency_key",
        indexes = @Index(name = "idx_idempotency_key", columnList = "key", unique = true))
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyKey {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @Column(nullable = false, columnDefinition = "text")
    private String responseBody;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String key, String tenantId, String requestHash, int responseStatus, String responseBody, Instant expiresAt) {
        this.key = key;
        this.tenantId = tenantId;
        this.requestHash = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.expiresAt = expiresAt;
    }

    public String getKey() { return key; }
    public String getTenantId() { return tenantId; }
    public String getRequestHash() { return requestHash; }
    public int getResponseStatus() { return responseStatus; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
