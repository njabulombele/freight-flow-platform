package com.freightflow.incidents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumes delivery events from the outbox relay and auto-opens an incident when
 * a delivery fails. This is the async event-processing side of the architecture.
 *
 * The listener is only active when Kafka is enabled; when the profile is not
 * present Spring Boot auto-configures the listener conditionally via the
 * container factory. Errors are logged and the message is acknowledged to avoid
 * an infinite retry loop — failed events remain in the outbox table for replay.
 */
@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final IncidentService incidentService;
    private final ObjectMapper objectMapper;
    private final boolean kafkaEnabled;

    public DeliveryEventConsumer(IncidentService incidentService, ObjectMapper objectMapper,
                                 @Value("${app.messaging.kafka-enabled:true}") boolean kafkaEnabled) {
        this.incidentService = incidentService;
        this.objectMapper = objectMapper;
        this.kafkaEnabled = kafkaEnabled;
    }

    @KafkaListener(topics = "freightflow.events", groupId = "freightflow-incident",
            autoStartup = "${app.messaging.kafka-enabled:true}")
    public void onDeliveryEvent(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                Acknowledgment ack) {
        if (!kafkaEnabled) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.path("type").asText();
            String tenantId = node.path("tenantId").asText();
            TenantContext.setTenantId(tenantId);

            if ("delivery.failed".equals(type)) {
                String deliveryNumber = node.path("deliveryNumber").asText();
                String note = node.has("note") ? node.path("note").asText() : "Delivery failed";
                UUID deliveryId = node.has("deliveryId") ? UUID.fromString(node.path("deliveryId").asText()) : null;
                IncidentRequest request = new IncidentRequest(deliveryId, IncidentType.FAILED_DELIVERY,
                        "Auto-opened for delivery " + deliveryNumber + ": " + note, 2);
                incidentService.create(request, "system");
                log.info("Auto-opened incident for failed delivery {}", deliveryNumber);
            }
        } catch (Exception e) {
            log.error("Failed to process delivery event {}: {}", key, e.getMessage(), e);
        } finally {
            TenantContext.clear();
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
