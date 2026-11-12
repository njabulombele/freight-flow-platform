package com.freightflow.deliveries;

import com.freightflow.fleet.Driver;
import com.freightflow.fleet.Vehicle;

import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
        UUID id,
        String deliveryNumber,
        String customerName,
        UUID driverId,
        String driverName,
        UUID vehicleId,
        String vehicleRegistration,
        DeliveryStatus status,
        String pickupAddress,
        String deliveryAddress,
        Double latitude,
        Double longitude,
        Double plannedDistanceKm,
        Integer plannedDurationMin,
        Instant scheduledAt,
        Instant eta,
        Instant completedAt,
        Integer delayMinutes,
        Double delayProbability,
        Delivery.RiskCategory riskCategory,
        String failureReason,
        String proofOfDeliveryUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static DeliveryResponse from(Delivery d) {
        Driver driver = d.getDriver();
        Vehicle vehicle = d.getVehicle();
        return new DeliveryResponse(
                d.getId(),
                d.getDeliveryNumber(),
                d.getCustomer().getName(),
                driver != null ? driver.getId() : null,
                driver != null ? driver.getName() : null,
                vehicle != null ? vehicle.getId() : null,
                vehicle != null ? vehicle.getRegistrationNumber() : null,
                d.getStatus(),
                d.getPickupAddress(),
                d.getDeliveryAddress(),
                d.getLatitude(),
                d.getLongitude(),
                d.getPlannedDistanceKm(),
                d.getPlannedDurationMin(),
                d.getScheduledAt(),
                d.getEta(),
                d.getCompletedAt(),
                d.getDelayMinutes(),
                d.getDelayProbability(),
                d.getRiskCategory(),
                d.getFailureReason(),
                d.getProofOfDeliveryUrl(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
