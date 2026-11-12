package com.freightflow.common.idempotency;

import com.freightflow.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Handles idempotent write operations. Callers pass a client-supplied key and the
 * request body hash; the service returns a cached response for repeats or records
 * a new one after the operation completes.
 *
 * A unique constraint on (key) plus the transactional insert makes the first request
 * win; concurrent duplicates collide on the constraint and fall back to the stored
 * response.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final Duration TTL = Duration.ofHours(24);

    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    public Optional<IdempotencyKey> findExisting(String key) {
        String tenantId = SecurityUtils.currentTenantId();
        return repository.findByKeyAndTenantId(key, tenantId);
    }

    @Transactional
    public IdempotencyKey record(String key, String requestBody, int responseStatus, String responseBody) {
        String tenantId = SecurityUtils.currentTenantId();
        String requestHash = hash(requestBody);
        IdempotencyKey record = new IdempotencyKey(key, tenantId, requestHash, responseStatus, responseBody, Instant.now().plus(TTL));
        try {
            return repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            // A concurrent duplicate won the race; return the stored response.
            return repository.findByKeyAndTenantId(key, tenantId)
                    .orElseThrow(() -> new IllegalStateException("Idempotency key collision could not be resolved", e));
        }
    }

    public boolean requestMatches(IdempotencyKey existing, String requestBody) {
        return existing.getRequestHash().equals(hash(requestBody));
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not compute request hash", e);
        }
    }

    @Transactional
    public void purgeExpired() {
        long deleted = repository.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.debug("Purged {} expired idempotency keys", deleted);
        }
    }
}
