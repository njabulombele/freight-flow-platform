package com.freightflow.deliveries;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRequest(
        @NotNull UUID driverId,
        @NotNull UUID vehicleId
) {
}
