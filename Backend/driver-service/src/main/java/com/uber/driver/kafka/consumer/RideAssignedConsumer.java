package com.uber.driver.kafka.consumer;

import com.uber.driver.dto.RideEventDTO;
import com.uber.driver.model.Driver;
import com.uber.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer that listens to ride assignment events from matching-service
 */
@Component
@Slf4j
public class RideAssignedConsumer {

    @Autowired
    private DriverService driverService;

    @KafkaListener(topics = "driver-events", groupId = "driver-service-group")
    public void consumeRideAssignment(RideEventDTO event) {
        log.info("========================================");
        log.info("🚨 KAFKA MESSAGE RECEIVED!");
        log.info("Event Type: {}", event.getEventType());
        log.info("Driver ID: {}", event.getDriverId());
        log.info("Ride ID: {}", event.getRideId());
        log.info("========================================");

        try {
            if ("DRIVER_ASSIGNED".equals(event.getEventType())) {
                // Update driver status when ride is assigned
                Driver driver = driverService.assignRideToDriver(event.getDriverId(), event.getRideId());
                
                log.info("Driver {} assigned to ride {}. Status updated to: {}", 
                        driver.getId(), event.getRideId(), driver.getStatus());
            } else if ("RIDE_CANCELLED".equals(event.getEventType())) {
                // Release driver if ride is cancelled
                if (event.getDriverId() != null) {
                    Driver driver = driverService.cancelRideAssignment(event.getDriverId(), event.getRideId());
                    log.info("Driver {} released from cancelled ride {}", driver.getId(), event.getRideId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing ride assignment for driver {}: {}", 
                    event.getDriverId(), e.getMessage(), e);
        }
    }
}
