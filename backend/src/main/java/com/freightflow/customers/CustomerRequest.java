package com.freightflow.customers;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank String name,
        String contactName,
        String phone,
        String email,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        Double latitude,
        Double longitude
) {
}
