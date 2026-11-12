package com.freightflow.deliveries;

import com.freightflow.customers.Customer;
import com.freightflow.fleet.Driver;
import com.freightflow.fleet.Vehicle;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * A delivery job. Tracks the customer, assigned driver/vehicle, the planned
 * schedule and live progress, plus the AI prediction fields that the prediction
 * service updates.
 */
@Entity
@Table(name = "deliveries")
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "delivery_number", nullable = false, unique = true)
    private String deliveryNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    private Double latitude;

    private Double longitude;

    @Column(name = "planned_distance_km")
    private Double plannedDistanceKm;

    @Column(name = "planned_duration_min")
    private Integer plannedDurationMin;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "eta")
    private Instant eta;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(name = "delay_probability")
    private Double delayProbability;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category")
    private RiskCategory riskCategory;

    @Column(name = "delay_factors", columnDefinition = "jsonb")
    private String delayFactors;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "proof_of_delivery_url")
    private String proofOfDeliveryUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum RiskCategory { LOW, MEDIUM, HIGH }

    protected Delivery() {
    }

    public Delivery(String tenantId, String deliveryNumber, Customer customer, String pickupAddress,
                     String deliveryAddress, Double latitude, Double longitude,
                     Double plannedDistanceKm, Integer plannedDurationMin, Instant scheduledAt) {
        this.tenantId = tenantId;
        this.deliveryNumber = deliveryNumber;
        this.customer = customer;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plannedDistanceKm = plannedDistanceKm;
        this.plannedDurationMin = plannedDurationMin;
        this.scheduledAt = scheduledAt;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public Customer getCustomer() { return customer; }
    public Driver getDriver() { return driver; }
    public Vehicle getVehicle() { return vehicle; }
    public DeliveryStatus getStatus() { return status; }
    public String getPickupAddress() { return pickupAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getPlannedDistanceKm() { return plannedDistanceKm; }
    public Integer getPlannedDurationMin() { return plannedDurationMin; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getEta() { return eta; }
    public Instant getCompletedAt() { return completedAt; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public Double getDelayProbability() { return delayProbability; }
    public RiskCategory getRiskCategory() { return riskCategory; }
    public String getDelayFactors() { return delayFactors; }
    public String getFailureReason() { return failureReason; }
    public String getProofOfDeliveryUrl() { return proofOfDeliveryUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void assign(Driver driver, Vehicle vehicle) {
        this.driver = driver;
        this.vehicle = vehicle;
    }

    public void transitionTo(DeliveryStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
        if (newStatus == DeliveryStatus.DELIVERED || newStatus == DeliveryStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void markFailed(String reason) {
        this.failureReason = reason;
    }

    public void setProofOfDeliveryUrl(String url) {
        this.proofOfDeliveryUrl = url;
    }

    public void setEta(Instant eta) {
        this.eta = eta;
    }

    public void applyPrediction(Double probability, Integer delayMinutes, RiskCategory riskCategory, String factors) {
        this.delayProbability = probability;
        this.delayMinutes = delayMinutes;
        this.riskCategory = riskCategory;
        this.delayFactors = factors;
    }
}
