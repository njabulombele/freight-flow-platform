package com.freightflow.reporting;

import com.freightflow.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reporting")
@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/operations-summary")
    @Operation(summary = "Operations dashboard summary: totals, completions, failures, delays, active drivers, risk")
    public OperationsSummary operationsSummary() {
        return service.summary(SecurityUtils.currentTenantId());
    }
}
