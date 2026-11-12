package com.freightflow.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves outbox events within the current transaction. Callers should invoke
 * {@link #publish} before committing the business change so the event row and
 * the change commit atomically.
 */
@Service
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String aggregateType, String aggregateId, String type, Object payload) {
        try {
            String tenantId = SecurityUtils.currentTenantId();
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxEvent(aggregateType, aggregateId, type, tenantId, json));
            log.debug("Staged outbox event {} for aggregate {} {}", type, aggregateType, aggregateId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stage outbox event", e);
        }
    }
}
