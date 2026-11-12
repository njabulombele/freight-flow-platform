package com.freightflow.deliveries;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbox event payload published when a delivery changes. Downstream consumers
 * (incident management, notification, analytics) react to these events.
 */
public record DeliveryEvent(
        UUID deliveryId,
        String deliveryNumber,
        String tenantId,
        DeliveryStatus previousStatus,
        DeliveryStatus newStatus,
        UUID driverId,
        Double latitude,
        Double longitude,
        String note,
        Instant occurredAt
) {
    public static DeliveryEvent of(Delivery delivery, DeliveryStatus previous, String note) {
        return new DeliveryEvent(
                delivery.getId(),
                delivery.getDeliveryNumber(),
                delivery.getTenantId(),
                previous,
                delivery.getStatus(),
                delivery.getDriver() != null ? delivery.getDriver().getId() : null,
                delivery.getLatitude(),
                delivery.getLongitude(),
                note,
                Instant.now()
        );
    }
}
