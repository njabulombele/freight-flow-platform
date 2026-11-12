package com.freightflow.reporting;

import com.freightflow.deliveries.Delivery;
import com.freightflow.deliveries.DeliveryStatus;

import java.util.Map;

/**
 * Aggregated operations metrics for the operations dashboard.
 */
public record OperationsSummary(
        long totalDeliveries,
        long completedDeliveries,
        long failedDeliveries,
        long delayedShipments,
        long activeDrivers,
        double onTimeRate,
        double failureRate,
        long highRiskDeliveries,
        Map<DeliveryStatus, Long> byStatus
) {
}
