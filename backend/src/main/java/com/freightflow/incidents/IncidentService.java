package com.freightflow.incidents;

import com.freightflow.common.audit.AuditService;
import com.freightflow.common.exception.ResourceNotFoundException;
import com.freightflow.common.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository repository;
    private final AuditService auditService;

    public IncidentService(IncidentRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public Incident create(IncidentRequest request, String reportedBy) {
        String tenantId = SecurityUtils.currentTenantId();
        Incident incident = new Incident(tenantId, request.type(), request.description(), reportedBy);
        if (request.deliveryId() != null) {
            incident.setDeliveryId(request.deliveryId());
        }
        if (request.priority() != null) {
            incident.setPriority(request.priority());
        }
        incident = repository.save(incident);
        auditService.record("CREATE", "Incident", incident.getId().toString(), null, IncidentResponse.from(incident));
        return incident;
    }

    @Transactional(readOnly = true)
    public Page<Incident> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Incident> openIncidents() {
        return repository.findByTenantIdAndStatus(SecurityUtils.currentTenantId(), IncidentStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public Incident get(UUID id) {
        return findForTenant(id);
    }

    @Transactional
    public Incident resolve(UUID id, ResolveRequest request) {
        Incident incident = findForTenant(id);
        IncidentStatus before = incident.getStatus();
        incident.resolve(request.resolution());
        auditService.record("RESOLVE", "Incident", id.toString(), before.name(), incident.getStatus().name());
        return incident;
    }

    @Transactional
    public Incident close(UUID id) {
        Incident incident = findForTenant(id);
        incident.close();
        auditService.record("CLOSE", "Incident", id.toString(), null, "CLOSED");
        return incident;
    }

    private Incident findForTenant(UUID id) {
        Incident incident = repository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return incident;
    }
}
