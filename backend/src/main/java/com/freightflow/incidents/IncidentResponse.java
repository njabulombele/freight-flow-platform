package com.freightflow.incidents;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID deliveryId,
        IncidentType type,
        IncidentStatus status,
        Integer priority,
        String description,
        String reportedBy,
        String resolution,
        Instant createdAt,
        Instant resolvedAt
) {
    public static IncidentResponse from(Incident i) {
        return new IncidentResponse(i.getId(), i.getDeliveryId(), i.getType(), i.getStatus(),
                i.getPriority(), i.getDescription(), i.getReportedBy(), i.getResolution(),
                i.getCreatedAt(), i.getResolvedAt());
    }
}
