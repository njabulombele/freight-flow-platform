package com.freightflow.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByIdAndTenantId(UUID id, String tenantId);

    List<Delivery> findByTenantIdAndStatus(String tenantId, DeliveryStatus status);

    @Query("select count(d) from Delivery d where d.tenantId = :tenantId and d.completedAt between :start and :end and d.status = :status")
    long countByTenantAndCompletedBetween(@Param("tenantId") String tenantId,
                                          @Param("start") Instant start,
                                          @Param("end") Instant end,
                                          @Param("status") DeliveryStatus status);

    @Query("select d from Delivery d where d.tenantId = :tenantId and d.status in :statuses order by d.scheduledAt")
    List<Delivery> findByTenantAndStatuses(@Param("tenantId") String tenantId,
                                           @Param("statuses") List<DeliveryStatus> statuses);

    @Query("select count(d) from Delivery d where d.tenantId = :tenantId and d.riskCategory = :risk")
    long countByTenantAndRisk(@Param("tenantId") String tenantId, @Param("risk") Delivery.RiskCategory risk);
}
