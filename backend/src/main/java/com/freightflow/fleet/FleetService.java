package com.freightflow.fleet;

import com.freightflow.common.audit.AuditService;
import com.freightflow.common.exception.BusinessException;
import com.freightflow.common.exception.ResourceNotFoundException;
import com.freightflow.common.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FleetService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AuditService auditService;

    public FleetService(VehicleRepository vehicleRepository, DriverRepository driverRepository, AuditService auditService) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.auditService = auditService;
    }

    // --- Vehicles ---

    @Transactional
    public Vehicle createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByRegistrationNumber(request.registrationNumber())) {
            throw new BusinessException("DUPLICATE_REGISTRATION", "A vehicle with this registration already exists");
        }
        String tenantId = SecurityUtils.currentTenantId();
        Vehicle vehicle = new Vehicle(tenantId, request.registrationNumber(), request.type(), request.capacityKg(), request.makeModel());
        vehicle = vehicleRepository.save(vehicle);
        auditService.record("CREATE", "Vehicle", vehicle.getId().toString(), null, VehicleSummary.from(vehicle));
        return vehicle;
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> listVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> allVehicles() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(UUID id) {
        return vehicleRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }

    // --- Drivers ---

    @Transactional
    public Driver createDriver(DriverRequest request) {
        String tenantId = SecurityUtils.currentTenantId();
        Driver driver = new Driver(tenantId, request.name(), request.phoneNumber(), request.licenseNumber());
        driver = driverRepository.save(driver);
        auditService.record("CREATE", "Driver", driver.getId().toString(), null, DriverSummary.from(driver));
        return driver;
    }

    @Transactional(readOnly = true)
    public Page<Driver> listDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Driver> allDrivers() {
        return driverRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Driver getDriver(UUID id) {
        return driverRepository.findByIdAndTenantId(id, SecurityUtils.currentTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }

    @Transactional
    public Driver assignVehicle(UUID driverId, UUID vehicleId) {
        Driver driver = getDriver(driverId);
        Vehicle vehicle = getVehicle(vehicleId);
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || vehicle.getStatus() == VehicleStatus.RETIRED) {
            throw new BusinessException("VEHICLE_UNAVAILABLE", "Vehicle is not available for assignment");
        }
        driver.setAssignedVehicle(vehicle);
        auditService.record("ASSIGN_VEHICLE", "Driver", driverId.toString(), null, vehicle.getRegistrationNumber());
        return driver;
    }
}
