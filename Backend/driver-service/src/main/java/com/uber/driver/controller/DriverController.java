package com.uber.driver.controller;

import com.uber.driver.dto.DriverResponseDTO;
import com.uber.driver.model.Driver;
import com.uber.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
@Slf4j
public class DriverController {

    @Autowired
    private DriverService driverService;

    /**
     * Create a new driver
     */
    @PostMapping
    public ResponseEntity<DriverResponseDTO> createDriver(@RequestBody Driver driver) {
        log.info("Creating new driver: {}", driver.getName());
        Driver created = driverService.createDriver(driver);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DriverResponseDTO.fromEntity(created));
    }

    /**
     * Get driver by ID
     */
    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponseDTO> getDriver(@PathVariable Long driverId) {
        log.info("Fetching driver: {}", driverId);
        return driverService.getDriverById(driverId)
                .map(driver -> ResponseEntity.ok(DriverResponseDTO.fromEntity(driver)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all drivers
     */
    @GetMapping
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        log.info("Fetching all drivers");
        List<DriverResponseDTO> drivers = driverService.getAllDrivers().stream()
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get available drivers
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponseDTO>> getAvailableDrivers(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        
        log.info("Fetching available drivers near location: ({}, {})", latitude, longitude);
        
        List<Driver> drivers;
        if (latitude != null && longitude != null) {
            drivers = driverService.getAvailableDriversNearLocation(latitude, longitude, radiusKm);
        } else {
            drivers = driverService.getAvailableDrivers();
        }
        
        List<DriverResponseDTO> response = drivers.stream()
                .map(DriverResponseDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Accept ride
     */
    @PostMapping("/{driverId}/accept-ride")
    public ResponseEntity<DriverResponseDTO> acceptRide(
            @PathVariable Long driverId,
            @RequestBody Map<String, Long> request) {
        
        Long rideId = request.get("rideId");
        log.info("Driver {} accepting ride {}", driverId, rideId);
        
        try {
            Driver driver = driverService.acceptRide(driverId, rideId);
            return ResponseEntity.ok(DriverResponseDTO.fromEntity(driver));
        } catch (RuntimeException e) {
            log.error("Error accepting ride: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Start ride
     */
    @PostMapping("/{driverId}/start-ride")
    public ResponseEntity<DriverResponseDTO> startRide(
            @PathVariable Long driverId,
            @RequestBody Map<String, Long> request) {
        
        Long rideId = request.get("rideId");
        log.info("Driver {} starting ride {}", driverId, rideId);
        
        try {
            Driver driver = driverService.startRide(driverId, rideId);
            return ResponseEntity.ok(DriverResponseDTO.fromEntity(driver));
        } catch (RuntimeException e) {
            log.error("Error starting ride: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Complete ride
     */
    @PostMapping("/{driverId}/complete-ride")
    public ResponseEntity<DriverResponseDTO> completeRide(
            @PathVariable Long driverId,
            @RequestBody Map<String, Long> request) {
        
        Long rideId = request.get("rideId");
        log.info("Driver {} completing ride {}", driverId, rideId);
        
        try {
            Driver driver = driverService.completeRide(driverId, rideId);
            return ResponseEntity.ok(DriverResponseDTO.fromEntity(driver));
        } catch (RuntimeException e) {
            log.error("Error completing ride: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update driver location
     */
    @PutMapping("/{driverId}/location")
    public ResponseEntity<DriverResponseDTO> updateLocation(
            @PathVariable Long driverId,
            @RequestBody Map<String, Double> location) {
        
        Double latitude = location.get("latitude");
        Double longitude = location.get("longitude");
        
        log.debug("Updating location for driver {}", driverId);
        
        try {
            Driver driver = driverService.updateLocation(driverId, latitude, longitude);
            return ResponseEntity.ok(DriverResponseDTO.fromEntity(driver));
        } catch (RuntimeException e) {
            log.error("Error updating location: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update driver status
     */
    @PutMapping("/{driverId}/status")
    public ResponseEntity<DriverResponseDTO> updateStatus(
            @PathVariable Long driverId,
            @RequestBody Map<String, String> request) {
        
        String statusStr = request.get("status");
        log.info("Updating driver {} status to {}", driverId, statusStr);
        
        try {
            Driver.DriverStatus status = Driver.DriverStatus.valueOf(statusStr);
            Driver driver = driverService.updateStatus(driverId, status);
            return ResponseEntity.ok(DriverResponseDTO.fromEntity(driver));
        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
