package com.freightflow.fleet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByIdAndTenantId(UUID id, String tenantId);
    boolean existsByRegistrationNumber(String registrationNumber);
}
