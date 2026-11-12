package com.freightflow.fleet;

import java.util.UUID;

public record DriverSummary(
        UUID id,
        String name,
        String phoneNumber,
        String licenseNumber,
        DriverStatus status
) {
    public static DriverSummary from(Driver d) {
        return new DriverSummary(d.getId(), d.getName(), d.getPhoneNumber(), d.getLicenseNumber(), d.getStatus());
    }
}
