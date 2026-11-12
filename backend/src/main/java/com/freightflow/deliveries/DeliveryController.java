package com.freightflow.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.common.idempotency.IdempotencyKey;
import com.freightflow.common.idempotency.IdempotencyService;
import com.freightflow.common.security.AuthenticatedUser;
import com.freightflow.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Deliveries")
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final DeliveryService service;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public DeliveryController(DeliveryService service, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.service = service;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @Operation(summary = "List deliveries")
    public Page<DeliveryResponse> list(Pageable pageable) {
        return service.listDeliveries(pageable).map(DeliveryResponse::from);
    }

    @GetMapping("/active")
    @Operation(summary = "List active (in-progress) deliveries for the live tracking view")
    public List<DeliveryResponse> active() {
        return service.activeDeliveries().stream().map(DeliveryResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DeliveryResponse get(@PathVariable UUID id) {
        return DeliveryResponse.from(service.get(id));
    }

    @GetMapping("/{id}/tracking")
    public List<DeliveryUpdate> trackingHistory(@PathVariable UUID id) {
        return service.trackingHistory(id);
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody CreateDeliveryRequest request) {
        Delivery delivery = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryResponse.from(delivery));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign a driver and vehicle to a delivery")
    public DeliveryResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest request) {
        return DeliveryResponse.from(service.assign(id, request));
    }

    /**
     * Driver-facing tracking update. Idempotent: a repeat request carrying the same
     * Idempotency-Key returns the stored response and does not double-apply the update.
     */
    @PostMapping("/{id}/updates")
    public ResponseEntity<String> update(@PathVariable UUID id,
                                          @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
                                          @Valid @RequestBody DeliveryUpdateRequest request,
                                          HttpServletRequest httpRequest) throws Exception {
        AuthenticatedUser user = SecurityUtils.currentUser().orElse(null);
        String reportedBy = user != null ? user.email() : "driver";

        if (idempotencyKey != null) {
            IdempotencyKey existing = idempotencyService.findExisting(idempotencyKey).orElse(null);
            if (existing != null) {
                String body = existing.getResponseBody();
                if (!idempotencyService.requestMatches(existing, objectMapper.writeValueAsString(request))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("{\"error\":\"Idempotency key reused with different payload\"}");
                }
                return ResponseEntity.status(existing.getResponseStatus()).body(body);
            }
        }

        Delivery delivery = service.applyUpdate(id, request, reportedBy);
        String responseBody = objectMapper.writeValueAsString(DeliveryResponse.from(delivery));
        int status = HttpStatus.OK.value();

        if (idempotencyKey != null) {
            idempotencyService.record(idempotencyKey, objectMapper.writeValueAsString(request), status, responseBody);
        }
        return ResponseEntity.status(status).body(responseBody);
    }
}
