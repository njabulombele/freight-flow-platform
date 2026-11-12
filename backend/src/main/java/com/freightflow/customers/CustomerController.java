package com.freightflow.customers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Customers")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CustomerSummary> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public CustomerSummary get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<CustomerSummary> create(@Valid @RequestBody CustomerRequest request) {
        CustomerSummary summary = CustomerSummary.from(service.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @PutMapping("/{id}")
    public CustomerSummary update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return CustomerSummary.from(service.update(id, request));
    }
}
