package com.freightflow.incidents;

import com.freightflow.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Incidents")
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @GetMapping
    public Page<IncidentResponse> list(Pageable pageable) {
        return service.list(pageable).map(IncidentResponse::from);
    }

    @GetMapping("/open")
    public List<IncidentResponse> openIncidents() {
        return service.openIncidents().stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable UUID id) {
        return IncidentResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest request) {
        String reporter = SecurityUtils.currentUser().map(u -> u.email()).orElse("system");
        return ResponseEntity.status(HttpStatus.CREATED).body(IncidentResponse.from(service.create(request, reporter)));
    }

    @PostMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable UUID id, @Valid @RequestBody ResolveRequest request) {
        return IncidentResponse.from(service.resolve(id, request));
    }

    @PostMapping("/{id}/close")
    public IncidentResponse close(@PathVariable UUID id) {
        return IncidentResponse.from(service.close(id));
    }
}
