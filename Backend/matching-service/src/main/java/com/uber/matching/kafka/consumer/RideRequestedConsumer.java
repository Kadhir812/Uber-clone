package com.uber.matching.kafka.consumer;

import com.uber.matching.dto.RideEventDTO;
import com.uber.matching.kafka.producer.RideAssignedProducer;
import com.uber.matching.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RideRequestedConsumer {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private RideAssignedProducer rideAssignedProducer;

    /**
     * Listen to ride-events topic for RIDE_REQUESTED events
     * When a rider requests a ride, this consumer:
     * 1. Receives the ride request
     * 2. Finds an available driver
     * 3. Publishes driver assignment to driver-events topic
     */
    @KafkaListener(topics = "ride-events", groupId = "matching-service-group")
    public void consumeRideRequest(RideEventDTO rideEvent) {
        log.info("Received ride event: {} for ride ID: {}", 
                rideEvent.getEventType(), rideEvent.getRideId());

        try {
            // Only process RIDE_REQUESTED events
            if ("RIDE_REQUESTED".equals(rideEvent.getEventType())) {
                log.info("Processing ride request for ride ID: {}", rideEvent.getRideId());
                
                // Find driver for the ride
                RideEventDTO driverAssignment = matchingService.findDriverForRide(rideEvent);
                
                // Publish driver assignment event
                if (driverAssignment != null) {
                    rideAssignedProducer.publishDriverAssignment(driverAssignment);
                    log.info("Successfully assigned driver {} to ride {}", 
                            driverAssignment.getDriverId(), driverAssignment.getRideId());
                }
            } 
            // Handle ride completion/cancellation to release drivers
            else if ("RIDE_COMPLETED".equals(rideEvent.getEventType()) || 
                     "RIDE_CANCELLED".equals(rideEvent.getEventType())) {
                log.info("Releasing driver {} for completed/cancelled ride {}", 
                        rideEvent.getDriverId(), rideEvent.getRideId());
                
                if (rideEvent.getDriverId() != null && rideEvent.getDriverId() > 0) {
                    matchingService.releaseDriver(rideEvent.getDriverId());
                }
            }
        } catch (Exception e) {
            log.error("Error processing ride event for ride ID: {}", rideEvent.getRideId(), e);
        }
    }

    /**
     * Monitor available drivers count periodically
     */
    public void logAvailableDrivers() {
        long availableCount = matchingService.getAvailableDriverCount();
        log.debug("Available drivers: {}", availableCount);
    }
}
