package com.freightflow.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.common.audit.AuditService;
import com.freightflow.common.exception.BusinessException;
import com.freightflow.common.exception.ResourceNotFoundException;
import com.freightflow.common.outbox.OutboxEventPublisher;
import com.freightflow.common.security.SecurityUtils;
import com.freightflow.customers.Customer;
import com.freightflow.customers.CustomerRepository;
import com.freightflow.fleet.Driver;
import com.freightflow.fleet.DriverStatus;
import com.freightflow.fleet.Vehicle;
import com.freightflow.fleet.VehicleStatus;
import com.freightflow.fleet.FleetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final SecureRandom RNG = new SecureRandom();

    private final DeliveryRepository deliveryRepository;
    private final DeliveryUpdateRepository updateRepository;
    private final CustomerRepository customerRepository;
    private final FleetService fleetService;
    private final OutboxEventPublisher outboxPublisher;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryUpdateRepository updateRepository,
                           CustomerRepository customerRepository,
                           FleetService fleetService,
                           OutboxEventPublisher outboxPublisher,
                           AuditService auditService,
                           ObjectMapper objectMapper) {
        this.deliveryRepository = deliveryRepository;
        this.updateRepository = updateRepository;
        this.customerRepository = customerRepository;
        this.fleetService = fleetService;
        this.outboxPublisher = outboxPublisher;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Delivery create(CreateDeliveryRequest request) {
        String tenantId = SecurityUtils.currentTenantId();
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        if (!customer.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }

        String deliveryNumber = generateDeliveryNumber();
        Delivery delivery = new Delivery(tenantId, deliveryNumber, customer, request.pickupAddress(),
                request.deliveryAddress(), request.latitude(), request.longitude(),
                request.plannedDistanceKm(), request.plannedDurationMin(), request.scheduledAt());
        delivery = deliveryRepository.save(delivery);

        outboxPublisher.publish("Delivery", delivery.getId().toString(), "delivery.created",
                DeliveryEvent.of(delivery, null, "Delivery created"));
        auditService.record("CREATE", "Delivery", delivery.getId().toString(), null, DeliveryResponse.from(delivery));
        return delivery;
    }

    @Transactional
    public Delivery assign(UUID deliveryId, AssignRequest request) {
        Delivery delivery = findForTenant(deliveryId);
        Driver driver = fleetService.getDriver(request.driverId());
        Vehicle vehicle = fleetService.getVehicle(request.vehicleId());

        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new BusinessException("VEHICLE_UNAVAILABLE", "Selected vehicle is not operational");
        }

        DeliveryStatus previous = delivery.getStatus();
        delivery.assign(driver, vehicle);
        delivery.transitionTo(DeliveryStatus.ASSIGNED);
        driver.setStatus(DriverStatus.ON_DELIVERY);

        outboxPublisher.publish("Delivery", delivery.getId().toString(), "delivery.assigned",
                DeliveryEvent.of(delivery, previous, "Assigned to " + driver.getName()));
        auditService.record("ASSIGN", "Delivery", delivery.getId().toString(), previous.name(), DeliveryResponse.from(delivery));
        return delivery;
    }

    /**
     * Applies a driver tracking update. The controller wraps this with idempotency
     * handling so a duplicate update with the same Idempotency-Key returns the cached
     * response and is not re-applied.
     */
    @Transactional
    public Delivery applyUpdate(UUID deliveryId, DeliveryUpdateRequest request, String reportedBy) {
        Delivery delivery = findForTenant(deliveryId);
        DeliveryStatus previous = delivery.getStatus();

        if (request.status() != previous && !previous.canTransitionTo(request.status())) {
            throw new BusinessException("INVALID_TRANSITION",
                    "Cannot transition delivery from " + previous + " to " + request.status());
        }

        if (request.latitude() != null && request.longitude() != null) {
            delivery.updateLocation(request.latitude(), request.longitude());
        }

        if (request.status() != previous) {
            delivery.transitionTo(request.status());
        }

        if (request.status() == DeliveryStatus.FAILED && request.note() != null) {
            delivery.markFailed(request.note());
        }
        if (request.proofOfDeliveryUrl() != null) {
            delivery.setProofOfDeliveryUrl(request.proofOfDeliveryUrl());
        }

        DeliveryUpdate update = new DeliveryUpdate(delivery, request.status(), request.latitude(),
                request.longitude(), request.note(), request.proofOfDeliveryUrl(), reportedBy);
        updateRepository.save(update);

        String eventType = switch (request.status()) {
            case IN_TRANSIT -> "delivery.in_transit";
            case DELIVERED -> "delivery.delivered";
            case FAILED -> "delivery.failed";
            default -> "delivery.updated";
        };
        outboxPublisher.publish("Delivery", delivery.getId().toString(), eventType,
                DeliveryEvent.of(delivery, previous, request.note()));
        auditService.record("UPDATE_STATUS", "Delivery", delivery.getId().toString(),
                previous.name(), request.status().name());

        if (delivery.getDriver() != null) {
            if (request.status() == DeliveryStatus.DELIVERED || request.status() == DeliveryStatus.FAILED) {
                delivery.getDriver().setStatus(DriverStatus.AVAILABLE);
            }
        }

        return delivery;
    }

    @Transactional(readOnly = true)
    public Delivery get(UUID id) {
        return findForTenant(id);
    }

    @Transactional(readOnly = true)
    public Page<Delivery> listDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Delivery> activeDeliveries() {
        String tenantId = SecurityUtils.currentTenantId();
        return deliveryRepository.findByTenantAndStatuses(tenantId,
                List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT));
    }

    @Transactional(readOnly = true)
    public List<DeliveryUpdate> trackingHistory(UUID deliveryId) {
        return updateRepository.findByDeliveryIdOrderByCreatedAtAsc(deliveryId);
    }

    @Transactional
    public void applyPrediction(UUID deliveryId, double probability, int delayMinutes,
                                Delivery.RiskCategory risk, String factorsJson) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", deliveryId));
        delivery.applyPrediction(probability, delayMinutes, risk, factorsJson);
    }

    private Delivery findForTenant(UUID id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
        String tenantId = SecurityUtils.currentTenantId();
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Delivery", id);
        }
        return delivery;
    }

    private String generateDeliveryNumber() {
        long number = Math.abs(RNG.nextLong() % 1_000_000L);
        return "FF-" + String.format("%06d", number);
    }
}
