package com.uber.driver.kafka.producer;

import com.uber.driver.dto.RideEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer for publishing ride started events
 */
@Component
@Slf4j
public class RideStartedProducer {

    private static final String TOPIC = "driver-events";

    @Autowired
    private KafkaTemplate<String, RideEventDTO> kafkaTemplate;

    /**
     * Publish event when driver starts the ride (picked up passenger)
     */
    public void publishRideStarted(Long rideId, Long driverId) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("RIDE_STARTED")
                    .status("IN_PROGRESS")
                    .message("Driver has picked up the passenger and started the ride")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published RIDE_STARTED event - Ride: {}, Driver: {}", rideId, driverId);
            
        } catch (Exception e) {
            log.error("Error publishing RIDE_STARTED event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    /**
     * Publish event when driver is on the way to pickup location
     */
    public void publishDriverOnTheWay(Long rideId, Long driverId, Double currentLat, Double currentLng) {
        try {
            RideEventDTO event = RideEventDTO.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType("DRIVER_ON_THE_WAY")
                    .status("DRIVER_ON_THE_WAY")
                    .message("Driver is on the way to pickup location")
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(TOPIC, String.valueOf(rideId), event);
            log.info("Published DRIVER_ON_THE_WAY event - Ride: {}, Driver: {}", rideId, driverId);
            
        } catch (Exception e) {
            log.error("Error publishing DRIVER_ON_THE_WAY event for ride {}: {}", rideId, e.getMessage(), e);
        }
    }
}
