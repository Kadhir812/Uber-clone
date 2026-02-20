package com.uber.driver.service;

import com.uber.driver.kafka.producer.RideCompletedProducer;
import com.uber.driver.kafka.producer.RideStartedProducer;
import com.uber.driver.model.Driver;
import com.uber.driver.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideStartedProducer rideStartedProducer;

    @Autowired
    private RideCompletedProducer rideCompletedProducer;

    /**
     * Create a new driver
     */
    @Transactional
    public Driver createDriver(Driver driver) {
        log.info("Creating new driver: {}", driver.getName());
        return driverRepository.save(driver);
    }

    /**
     * Get driver by ID
     */
    public Optional<Driver> getDriverById(Long driverId) {
        return driverRepository.findById(driverId);
    }

    /**
     * Get all drivers
     */
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    /**
     * Get available drivers
     */
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findAvailableDrivers();
    }

    /**
     * Get available drivers near a location
     */
    public List<Driver> getAvailableDriversNearLocation(Double latitude, Double longitude, Double radiusKm) {
        // Simple bounding box calculation (for production, use more sophisticated geo queries)
        Double latDelta = radiusKm / 111.0; // ~111km per degree latitude
        Double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        Double minLat = latitude - latDelta;
        Double maxLat = latitude + latDelta;
        Double minLng = longitude - lngDelta;
        Double maxLng = longitude + lngDelta;
        
        return driverRepository.findAvailableDriversNearLocation(minLat, maxLat, minLng, maxLng);
    }

    /**
     * Assign ride to driver
     */
    @Transactional
    public Driver assignRideToDriver(Long driverId, Long rideId) {
        log.info("Assigning ride {} to driver {}", rideId, driverId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        if (driver.getStatus() != Driver.DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver is not available. Current status: " + driver.getStatus());
        }
        
        driver.setCurrentRideId(rideId);
        driver.setStatus(Driver.DriverStatus.ON_THE_WAY);
        
        return driverRepository.save(driver);
    }

    /**
     * Driver accepts the ride
     */
    @Transactional
    public Driver acceptRide(Long driverId, Long rideId) {
        log.info("Driver {} accepting ride {}", driverId, rideId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        if (!rideId.equals(driver.getCurrentRideId())) {
            throw new RuntimeException("Driver is not assigned to this ride");
        }
        
        driver.setStatus(Driver.DriverStatus.ON_THE_WAY);
        return driverRepository.save(driver);
    }

    /**
     * Driver starts the ride
     */
    @Transactional
    public Driver startRide(Long driverId, Long rideId) {
        log.info("Driver {} starting ride {}", driverId, rideId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        if (!rideId.equals(driver.getCurrentRideId())) {
            throw new RuntimeException("Driver is not assigned to this ride");
        }
        
        driver.setStatus(Driver.DriverStatus.BUSY);
        Driver savedDriver = driverRepository.save(driver);
        
        // Publish Kafka event to update ride status to IN_PROGRESS
        rideStartedProducer.publishRideStarted(rideId, driverId);
        log.info("Published RIDE_STARTED event for ride {}", rideId);
        
        return savedDriver;
    }

    /**
     * Driver completes the ride
     */
    @Transactional
    public Driver completeRide(Long driverId, Long rideId) {
        log.info("Driver {} completing ride {}", driverId, rideId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        if (!rideId.equals(driver.getCurrentRideId())) {
            throw new RuntimeException("Driver is not assigned to this ride");
        }
        
        driver.setCurrentRideId(null);
        driver.setStatus(Driver.DriverStatus.AVAILABLE);
        driver.setTotalRides(driver.getTotalRides() + 1);
        
        Driver savedDriver = driverRepository.save(driver);
        
        // Publish Kafka event to update ride status to COMPLETED
        rideCompletedProducer.publishRideCompleted(rideId, driverId, null);
        log.info("Published RIDE_COMPLETED event for ride {}", rideId);
        
        return savedDriver;
    }

    /**
     * Update driver location
     */
    @Transactional
    public Driver updateLocation(Long driverId, Double latitude, Double longitude) {
        log.debug("Updating location for driver {}: ({}, {})", driverId, latitude, longitude);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        
        return driverRepository.save(driver);
    }

    /**
     * Update driver status
     */
    @Transactional
    public Driver updateStatus(Long driverId, Driver.DriverStatus status) {
        log.info("Updating driver {} status to {}", driverId, status);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        driver.setStatus(status);
        return driverRepository.save(driver);
    }

    /**
     * Cancel ride assignment
     */
    @Transactional
    public Driver cancelRideAssignment(Long driverId, Long rideId) {
        log.info("Driver {} cancelling ride assignment {}", driverId, rideId);
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
        
        if (rideId.equals(driver.getCurrentRideId())) {
            driver.setCurrentRideId(null);
            driver.setStatus(Driver.DriverStatus.AVAILABLE);
            return driverRepository.save(driver);
        }
        
        return driver;
    }
}
