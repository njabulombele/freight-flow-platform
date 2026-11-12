package com.freightflow.prediction;

import java.util.UUID;

/**
 * Payload sent to the Python prediction service.
 */
public record PredictionRequest(
        UUID deliveryId,
        double distanceKm,
        int plannedDurationMin,
        int hourOfDay,
        int dayOfWeek,
        String city,
        double historicalDelayRate,
        Integer previousDelayMinutes
) {
}
