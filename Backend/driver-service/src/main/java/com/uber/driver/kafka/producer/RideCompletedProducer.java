package com.uber.driver.kafka.producer;

import com.uber.driver.dto.RideEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for publishing ride completed events
 */
@Component
@Slf4j
public class RideCompletedProducer {

    private static final String TOPIC = "driver-events";

    @Autowired
    private KafkaTemplate<String, RideEventDTO> kafkaTemplate;

    /**
     * Publish event when driver completes the ride
     */
    public void publishRideCompleted(Long rideId, Long driverId, Double fare) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("RIDE_COMPLETED")
                    .status("COMPLETED")
                    .fare(fare)
                    .message("Driver has completed the ride")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published RIDE_COMPLETED event - Ride: {}, Driver: {}, Fare: ${}", 
                    rideId, driverId, fare);
            
        } catch (Exception e) {
            log.error("Error publishing RIDE_COMPLETED event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    /**
     * Publish event when ride is cancelled by driver
     */
    public void publishRideCancelledByDriver(Long rideId, Long driverId, String reason) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("RIDE_CANCELLED")
                    .status("CANCELLED")
                    .message(reason != null ? reason : "Ride cancelled by driver")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published RIDE_CANCELLED event - Ride: {}, Driver: {}", rideId, driverId);
            
        } catch (Exception e) {
            log.error("Error publishing RIDE_CANCELLED event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }
}
