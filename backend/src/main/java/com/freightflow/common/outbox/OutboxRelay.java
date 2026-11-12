package com.freightflow.common.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Polls the outbox table and publishes pending events to Kafka.
 *
 * Reliability features:
 * - Event is only marked PUBLISHED after the broker acknowledges it.
 * - Failed sends increment retry_count; after max retries the event is FAILED
 *   (the dead-letter path) and stays in the table for manual inspection.
 *
 * When Kafka is unavailable locally the relay logs a warning and retries on the
 * next tick, so the local dev experience does not break.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private static final String TOPIC = "freightflow.events";
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;
    private final boolean kafkaEnabled;

    public OutboxRelay(OutboxEventRepository repository,
                       KafkaTemplate<String, String> kafkaTemplate,
                       TransactionTemplate transactionTemplate,
                       @Value("${app.messaging.kafka-enabled:true}") boolean kafkaEnabled) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Scheduled(fixedDelay = 5000)
    public void drain() {
        List<OutboxEvent> pending = repository.findPending(PageRequest.of(0, BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }
        log.debug("Relaying {} outbox events", pending.size());
        for (OutboxEvent event : pending) {
            relayOne(event);
        }
    }

    private void relayOne(OutboxEvent event) {
        if (!kafkaEnabled) {
            return;
        }
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    TOPIC, event.getAggregateId(), event.getPayload());
            future.whenComplete((result, ex) -> transactionTemplate.executeWithoutResult(status -> {
                OutboxEvent managed = repository.findById(event.getId()).orElseThrow();
                if (ex == null) {
                    managed.markPublished();
                    log.info("Published outbox event {} {} to Kafka", event.getType(), event.getAggregateId());
                } else {
                    handleFailure(managed, ex);
                }
                repository.save(managed);
            }));
        } catch (Exception ex) {
            transactionTemplate.executeWithoutResult(status -> {
                OutboxEvent managed = repository.findById(event.getId()).orElseThrow();
                handleFailure(managed, ex);
                repository.save(managed);
            });
        }
    }

    private void handleFailure(OutboxEvent event, Throwable ex) {
        event.incrementRetry();
        log.warn("Failed to publish outbox event {} (retry {}): {}", event.getType(), event.getRetryCount(), ex.getMessage());
        if (event.getRetryCount() >= MAX_RETRIES) {
            event.markFailed();
            // Dead-letter: the FAILED event remains in the table for inspection/replay.
            log.error("Outbox event {} {} moved to FAILED state (dead-letter)", event.getType(), event.getAggregateId());
        }
    }
}
