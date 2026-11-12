package com.freightflow.incidents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IncidentRequest(
        UUID deliveryId,
        @NotNull IncidentType type,
        @NotBlank String description,
        Integer priority
) {
}
