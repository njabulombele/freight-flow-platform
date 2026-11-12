package com.freightflow.customers;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * A customer receiving deliveries. Has delivery address with coordinates for routing.
 */
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_name")
    private String contactName;

    private String phone;

    private String email;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(nullable = false)
    private String city;

    private Double latitude;

    private Double longitude;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Customer() {
    }

    public Customer(String tenantId, String name, String contactName, String phone, String email,
                    String addressLine1, String addressLine2, String city, Double latitude, Double longitude) {
        this.tenantId = tenantId;
        this.name = name;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getContactName() { return contactName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Instant getCreatedAt() { return createdAt; }

    public void update(String name, String contactName, String phone, String email,
                        String addressLine1, String addressLine2, String city, Double latitude, Double longitude) {
        this.name = name;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
