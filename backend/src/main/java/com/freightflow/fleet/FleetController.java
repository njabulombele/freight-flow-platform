package com.freightflow.fleet;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fleet")
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    private final FleetService service;

    public FleetController(FleetService service) {
        this.service = service;
    }

    @GetMapping("/vehicles")
    public Page<VehicleSummary> vehicles(Pageable pageable) {
        return service.listVehicles(pageable).map(VehicleSummary::from);
    }

    @GetMapping("/vehicles/all")
    public List<VehicleSummary> allVehicles() {
        return service.allVehicles().stream().map(VehicleSummary::from).toList();
    }

    @GetMapping("/vehicles/{id}")
    public VehicleSummary vehicle(@PathVariable UUID id) {
        return VehicleSummary.from(service.getVehicle(id));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<VehicleSummary> createVehicle(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleSummary.from(service.createVehicle(request)));
    }

    @GetMapping("/drivers")
    public Page<DriverSummary> drivers(Pageable pageable) {
        return service.listDrivers(pageable).map(DriverSummary::from);
    }

    @GetMapping("/drivers/all")
    public List<DriverSummary> allDrivers() {
        return service.allDrivers().stream().map(DriverSummary::from).toList();
    }

    @GetMapping("/drivers/{id}")
    public DriverSummary driver(@PathVariable UUID id) {
        return DriverSummary.from(service.getDriver(id));
    }

    @PostMapping("/drivers")
    public ResponseEntity<DriverSummary> createDriver(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(DriverSummary.from(service.createDriver(request)));
    }

    @PostMapping("/drivers/{driverId}/assign-vehicle/{vehicleId}")
    public DriverSummary assignVehicle(@PathVariable UUID driverId, @PathVariable UUID vehicleId) {
        return DriverSummary.from(service.assignVehicle(driverId, vehicleId));
    }
}
