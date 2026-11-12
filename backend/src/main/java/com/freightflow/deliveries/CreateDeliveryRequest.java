package com.freightflow.deliveries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public record CreateDeliveryRequest(
        @NotNull UUID customerId,
        @NotBlank String pickupAddress,
        @NotBlank String deliveryAddress,
        Double latitude,
        Double longitude,
        @Positive Double plannedDistanceKm,
        @Positive Integer plannedDurationMin,
        @NotNull Instant scheduledAt
) {
}
