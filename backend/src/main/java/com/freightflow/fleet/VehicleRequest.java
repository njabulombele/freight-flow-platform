package com.freightflow.fleet;

import jakarta.validation.constraints.NotBlank;

public record VehicleRequest(
        @NotBlank String registrationNumber,
        @NotBlank String type,
        Double capacityKg,
        String makeModel
) {
}
