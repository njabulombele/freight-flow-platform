package com.freightflow.deliveries;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * A single tracking event from a driver (status change, location ping, note,
 * proof of delivery). Updates are idempotent via the Idempotency-Key header.
 */
@Entity
@Table(name = "delivery_updates")
@EntityListeners(AuditingEntityListener.class)
public class DeliveryUpdate {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private Double latitude;

    private Double longitude;

    private String note;

    @Column(name = "proof_url")
    private String proofUrl;

    @Column(name = "reported_by")
    private String reportedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected DeliveryUpdate() {
    }

    public DeliveryUpdate(Delivery delivery, DeliveryStatus status, Double latitude, Double longitude,
                          String note, String proofUrl, String reportedBy) {
        this.delivery = delivery;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
        this.proofUrl = proofUrl;
        this.reportedBy = reportedBy;
    }

    public UUID getId() { return id; }
    public Delivery getDelivery() { return delivery; }
    public DeliveryStatus getStatus() { return status; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getNote() { return note; }
    public String getProofUrl() { return proofUrl; }
    public String getReportedBy() { return reportedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
