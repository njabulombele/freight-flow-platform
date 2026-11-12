package com.freightflow.fleet;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drivers")
@EntityListeners(AuditingEntityListener.class)
public class Driver {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "license_number")
    private String licenseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Driver() {
    }

    public Driver(String tenantId, String name, String phoneNumber, String licenseNumber) {
        this.tenantId = tenantId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getLicenseNumber() { return licenseNumber; }
    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public DriverStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public void setAssignedVehicle(Vehicle vehicle) { this.assignedVehicle = vehicle; }
    public void update(String name, String phoneNumber, String licenseNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.licenseNumber = licenseNumber;
    }
}
