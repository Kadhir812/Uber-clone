package com.uber.matching.service;

import com.uber.matching.dto.RideEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class MatchingService {

    private final Random random = new Random();
    
    // Simulated pool of available drivers
    private final ConcurrentMap<Long, DriverInfo> availableDrivers = new ConcurrentHashMap<>();
    
    public MatchingService() {
        // Initialize with some mock drivers
        initializeMockDrivers();
    }
    
    private void initializeMockDrivers() {
        for (long i = 1; i <= 3; i++) {
            availableDrivers.put(i, new DriverInfo(i, "Driver-" + i, true, 0.0, 0.0));
        }
        log.info("Initialized {} mock drivers", availableDrivers.size());
    }

    /**
     * Find an available driver for the ride request
     * In production, this would:
     * 1. Query driver-service for available drivers near pickup location
     * 2. Use sophisticated matching algorithm (distance, rating, availability)
     * 3. Consider driver preferences and surge pricing
     */
    public RideEventDTO findDriverForRide(RideEventDTO rideRequest) {
        log.info("Finding driver for ride: {}", rideRequest.getRideId());
        
        // Find available driver (simplified logic)
        Long assignedDriverId = findNearestAvailableDriver(
            rideRequest.getPickupLocation()
        );
        
        if (assignedDriverId != null) {
            log.info("Found driver {} for ride {}", assignedDriverId, rideRequest.getRideId());
            
            // Mark driver as busy
            DriverInfo driver = availableDrivers.get(assignedDriverId);
            if (driver != null) {
                driver.setAvailable(false);
            }
            
            // Create driver assignment event
            return RideEventDTO.builder()
                    .rideId(rideRequest.getRideId())
                    .userId(rideRequest.getUserId())
                    .driverId(assignedDriverId)
                    .eventType("DRIVER_ASSIGNED")
                    .status("ASSIGNED")
                    .pickupLocation(rideRequest.getPickupLocation())
                    .dropoffLocation(rideRequest.getDropoffLocation())
                    .fare(rideRequest.getFare())
                    .message("Driver " + assignedDriverId + " assigned to ride " + rideRequest.getRideId())
                    .timestamp(System.currentTimeMillis())
                    .build();
        } else {
            log.warn("No available driver found for ride {}", rideRequest.getRideId());
            
            // Create no driver available event
            return RideEventDTO.builder()
                    .rideId(rideRequest.getRideId())
                    .userId(rideRequest.getUserId())
                    .eventType("NO_DRIVER_AVAILABLE")
                    .status("REQUESTED")
                    .message("No drivers available. Please try again later.")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Find nearest available driver
     * In production: Calculate distance using lat/lng coordinates
     */
    private Long findNearestAvailableDriver(String pickupLocation) {
        List<Long> available = new ArrayList<>();
        
        for (DriverInfo driver : availableDrivers.values()) {
            if (driver.isAvailable()) {
                available.add(driver.getDriverId());
            }
        }
        
        if (available.isEmpty()) {
            return null;
        }
        
        // Simulate random selection (in production: distance-based)
        int index = random.nextInt(available.size());
        return available.get(index);
    }

    /**
     * Release driver when ride is completed
     */
    public void releaseDriver(Long driverId) {
        DriverInfo driver = availableDrivers.get(driverId);
        if (driver != null) {
            driver.setAvailable(true);
            log.info("Driver {} is now available", driverId);
        }
    }

    /**
     * Handle ride cancellation
     */
    public void handleRideCancellation(Long rideId, Long driverId) {
        if (driverId != null && driverId > 0) {
            releaseDriver(driverId);
            log.info("Released driver {} due to ride {} cancellation", driverId, rideId);
        }
    }

    /**
     * Get available driver count
     */
    public long getAvailableDriverCount() {
        return availableDrivers.values().stream()
                .filter(DriverInfo::isAvailable)
                .count();
    }

    // Inner class to represent driver info
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    private static class DriverInfo {
        private Long driverId;
        private String name;
        private boolean available;
        private Double latitude;
        private Double longitude;
    }
}
