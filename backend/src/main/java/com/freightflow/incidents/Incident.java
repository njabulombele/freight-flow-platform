package com.freightflow.incidents;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@EntityListeners(AuditingEntityListener.class)
public class Incident {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "priority", nullable = false)
    private Integer priority = 3;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "reported_by")
    private String reportedBy;

    @Column(name = "resolution", columnDefinition = "text")
    private String resolution;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Incident() {
    }

    public Incident(String tenantId, IncidentType type, String description, String reportedBy) {
        this.tenantId = tenantId;
        this.type = type;
        this.description = description;
        this.reportedBy = reportedBy;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public UUID getDeliveryId() { return deliveryId; }
    public IncidentType getType() { return type; }
    public IncidentStatus getStatus() { return status; }
    public Integer getPriority() { return priority; }
    public String getDescription() { return description; }
    public String getReportedBy() { return reportedBy; }
    public String getResolution() { return resolution; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    public void resolve(String resolution) {
        this.status = IncidentStatus.RESOLVED;
        this.resolution = resolution;
        this.resolvedAt = Instant.now();
    }

    public void close() {
        this.status = IncidentStatus.CLOSED;
    }
}
