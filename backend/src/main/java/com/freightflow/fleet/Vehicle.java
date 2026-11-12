package com.freightflow.fleet;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String type;

    @Column(name = "capacity_kg")
    private Double capacityKg;

    @Column(name = "make_model")
    private String makeModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Vehicle() {
    }

    public Vehicle(String tenantId, String registrationNumber, String type, Double capacityKg, String makeModel) {
        this.tenantId = tenantId;
        this.registrationNumber = registrationNumber;
        this.type = type;
        this.capacityKg = capacityKg;
        this.makeModel = makeModel;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getType() { return type; }
    public Double getCapacityKg() { return capacityKg; }
    public String getMakeModel() { return makeModel; }
    public VehicleStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(VehicleStatus status) { this.status = status; }
    public void update(String registrationNumber, String type, Double capacityKg, String makeModel) {
        this.registrationNumber = registrationNumber;
        this.type = type;
        this.capacityKg = capacityKg;
        this.makeModel = makeModel;
    }
}
