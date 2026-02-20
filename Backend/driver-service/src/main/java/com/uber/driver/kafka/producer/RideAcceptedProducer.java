package com.uber.driver.kafka.producer;

import com.uber.driver.dto.RideEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for publishing ride accepted events
 */
@Component
@Slf4j
public class RideAcceptedProducer {

    private static final String TOPIC = "ride-events";

    @Autowired
    private KafkaTemplate<String, RideEventDTO> kafkaTemplate;

    /**
     * Publish event when driver accepts a ride
     */
    public void publishRideAccepted(Long rideId, Long driverId) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("RIDE_ACCEPTED")
                    .status("ACCEPTED")
                    .message("Driver accepted the ride")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published RIDE_ACCEPTED event - Ride: {}, Driver: {}", rideId, driverId);
            
        } catch (Exception e) {
            log.error("Error publishing RIDE_ACCEPTED event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    /**
     * Publish event when driver rejects a ride
     */
    public void publishRideRejected(Long rideId, Long driverId, String reason) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("RIDE_REJECTED")
                    .status("PENDING")
                    .message(reason != null ? reason : "Driver rejected the ride")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published RIDE_REJECTED event - Ride: {}, Driver: {}", rideId, driverId);
            
        } catch (Exception e) {
            log.error("Error publishing RIDE_REJECTED event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }
}
