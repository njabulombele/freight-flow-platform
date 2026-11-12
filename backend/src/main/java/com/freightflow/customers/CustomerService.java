package com.freightflow.customers;

import com.freightflow.common.audit.AuditService;
import com.freightflow.common.exception.ResourceNotFoundException;
import com.freightflow.common.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final AuditService auditService;

    public CustomerService(CustomerRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        String tenantId = SecurityUtils.currentTenantId();
        Customer customer = new Customer(tenantId, request.name(), request.contactName(), request.phone(),
                request.email(), request.addressLine1(), request.addressLine2(), request.city(),
                request.latitude(), request.longitude());
        customer = repository.save(customer);
        auditService.record("CREATE", "Customer", customer.getId().toString(), null, CustomerSummary.from(customer));
        return customer;
    }

    @Transactional
    public Customer update(UUID id, CustomerRequest request) {
        Customer customer = findForTenant(id);
        CustomerSummary before = CustomerSummary.from(customer);
        customer.update(request.name(), request.contactName(), request.phone(), request.email(),
                request.addressLine1(), request.addressLine2(), request.city(), request.latitude(), request.longitude());
        auditService.record("UPDATE", "Customer", id.toString(), before, CustomerSummary.from(customer));
        return customer;
    }

    @Transactional(readOnly = true)
    public Page<CustomerSummary> list(Pageable pageable) {
        return repository.findByTenantId(SecurityUtils.currentTenantId(), pageable).map(CustomerSummary::from);
    }

    @Transactional(readOnly = true)
    public CustomerSummary get(UUID id) {
        return CustomerSummary.from(findForTenant(id));
    }

    private Customer findForTenant(UUID id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        String tenantId = SecurityUtils.currentTenantId();
        if (!customer.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        return customer;
    }
}
