package com.freightflow.reporting;

import com.freightflow.deliveries.Delivery;
import com.freightflow.deliveries.DeliveryRepository;
import com.freightflow.deliveries.DeliveryStatus;
import com.freightflow.fleet.DriverRepository;
import com.freightflow.fleet.DriverStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes operations dashboard metrics. Results are cached (Redis) for a short
 * window to avoid recomputing heavy aggregations on every dashboard poll.
 */
@Service
public class ReportingService {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;

    public ReportingService(DeliveryRepository deliveryRepository, DriverRepository driverRepository) {
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
    }

    @Cacheable(value = "operations-summary")
    @Transactional(readOnly = true)
    public OperationsSummary summary(String tenantId) {
        Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant dayEnd = Instant.now();

        List<Delivery> active = deliveryRepository.findByTenantAndStatuses(tenantId,
                List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT));
        List<Delivery> allToday = deliveryRepository.findByTenantAndStatuses(tenantId,
                List.of(DeliveryStatus.PENDING, DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT,
                        DeliveryStatus.DELIVERED, DeliveryStatus.FAILED, DeliveryStatus.CANCELLED));

        long completed = allToday.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count();
        long failed = allToday.stream().filter(d -> d.getStatus() == DeliveryStatus.FAILED).count();
        long delayed = active.stream().filter(d -> d.getDelayMinutes() != null && d.getDelayMinutes() > 0).count();
        long highRisk = active.stream().filter(d -> d.getRiskCategory() == Delivery.RiskCategory.HIGH).count();
        long activeDrivers = driverRepository.findAll().stream()
                .filter(d -> d.getStatus() == DriverStatus.ON_DELIVERY).count();

        Map<DeliveryStatus, Long> byStatus = new LinkedHashMap<>();
        for (DeliveryStatus s : DeliveryStatus.values()) {
            byStatus.put(s, allToday.stream().filter(d -> d.getStatus() == s).count());
        }

        double onTime = completed == 0 ? 0 :
                (double) allToday.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED
                        && (d.getDelayMinutes() == null || d.getDelayMinutes() == 0)).count() / completed;
        double failureRate = allToday.isEmpty() ? 0 : (double) failed / allToday.size();

        return new OperationsSummary(
                allToday.size(), completed, failed, delayed, activeDrivers,
                onTime, failureRate, highRisk, byStatus);
    }
}
