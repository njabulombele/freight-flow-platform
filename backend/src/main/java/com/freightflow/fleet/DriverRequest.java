package com.freightflow.fleet;

import jakarta.validation.constraints.NotBlank;

public record DriverRequest(
        @NotBlank String name,
        String phoneNumber,
        String licenseNumber
) {
}
