package com.freightflow.common.audit;

import com.freightflow.common.security.AuthenticatedUser;
import com.freightflow.common.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Writes audit entries asynchronously so the main request path is not slowed
 * and the audit record is decoupled from the business transaction.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Async
    public void record(String action, String entityType, String entityId, Object oldValue, Object newValue) {
        try {
            AuthenticatedUser user = SecurityUtils.currentUser().orElse(null);
            String tenant = user != null ? user.tenantId() : "system";
            String actor = user != null ? user.email() : "system";
            repository.save(new AuditLog(
                    tenant,
                    actor,
                    action,
                    entityType,
                    entityId,
                    toJson(oldValue),
                    toJson(newValue)));
        } catch (Exception e) {
            log.error("Failed to write audit log for {} {}: {}", entityType, entityId, e.getMessage());
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
