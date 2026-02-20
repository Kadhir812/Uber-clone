package com.uber.matching.kafka.producer;

import com.uber.matching.dto.RideEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class RideAssignedProducer {

    private static final String DRIVER_EVENTS_TOPIC = "driver-events";

    @Autowired
    private KafkaTemplate<String, RideEventDTO> kafkaTemplate;

    /**
     * Publish driver assignment event to driver-events topic
     * This will be consumed by:
     * 1. Ride Service - to update ride with assigned driver
     * 2. Driver Service - to notify driver of new assignment
     * 3. Notification Service - to send push notifications
     */
    public void publishDriverAssignment(RideEventDTO event) {
        log.info("Publishing driver assignment event for ride: {} with driver: {}", 
                event.getRideId(), event.getDriverId());

        try {
            CompletableFuture<SendResult<String, RideEventDTO>> future = 
                    kafkaTemplate.send(DRIVER_EVENTS_TOPIC, event.getRideId().toString(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent driver assignment event for ride: {} to topic: {} with offset: {}",
                            event.getRideId(),
                            DRIVER_EVENTS_TOPIC,
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send driver assignment event for ride: {} to topic: {}",
                            event.getRideId(),
                            DRIVER_EVENTS_TOPIC,
                            ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing driver assignment for ride: {}", event.getRideId(), e);
        }
    }

    /**
     * Publish driver rejection event
     */
    public void publishDriverRejection(RideEventDTO event) {
        log.info("Publishing driver rejection event for ride: {}", event.getRideId());

        RideEventDTO rejectionEvent = RideEventDTO.builder()
                .rideId(event.getRideId())
                .userId(event.getUserId())
                .driverId(event.getDriverId())
                .eventType("DRIVER_REJECTED")
                .status("REQUESTED")
                .message("Driver rejected the ride. Finding another driver...")
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            kafkaTemplate.send(DRIVER_EVENTS_TOPIC, rejectionEvent.getRideId().toString(), rejectionEvent);
            log.info("Published driver rejection event for ride: {}", event.getRideId());
        } catch (Exception e) {
            log.error("Error publishing driver rejection for ride: {}", event.getRideId(), e);
        }
    }
}
