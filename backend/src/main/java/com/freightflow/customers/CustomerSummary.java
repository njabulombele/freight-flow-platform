package com.freightflow.customers;

import java.time.Instant;
import java.util.UUID;

public record CustomerSummary(
        UUID id,
        String name,
        String contactName,
        String phone,
        String email,
        String city,
        Instant createdAt
) {
    public static CustomerSummary from(Customer c) {
        return new CustomerSummary(c.getId(), c.getName(), c.getContactName(), c.getPhone(),
                c.getEmail(), c.getCity(), c.getCreatedAt());
    }
}
