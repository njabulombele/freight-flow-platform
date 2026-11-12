package com.freightflow.fleet;

import java.util.UUID;

public record VehicleSummary(
        UUID id,
        String registrationNumber,
        String type,
        Double capacityKg,
        String makeModel,
        VehicleStatus status
) {
    public static VehicleSummary from(Vehicle v) {
        return new VehicleSummary(v.getId(), v.getRegistrationNumber(), v.getType(),
                v.getCapacityKg(), v.getMakeModel(), v.getStatus());
    }
}
