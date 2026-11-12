package com.freightflow.deliveries;

import jakarta.validation.constraints.NotNull;

/**
 * Driver-side tracking update. Carried with an Idempotency-Key header so
 * duplicate submissions (common on flaky mobile networks) do not double-apply.
 */
public record DeliveryUpdateRequest(
        @NotNull DeliveryStatus status,
        Double latitude,
        Double longitude,
        String note,
        String proofOfDeliveryUrl
) {
}
