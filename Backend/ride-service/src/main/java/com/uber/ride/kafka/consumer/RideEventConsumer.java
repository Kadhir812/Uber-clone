package com.uber.ride.kafka.consumer;

import com.uber.ride.dto.RideEventDTO;
import com.uber.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final RideService rideService;

    @KafkaListener(topics = "driver-events", groupId = "ride-service-group")
    public void consumeDriverEvent(
            @Payload RideEventDTO eventDTO,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Received driver event: {} for ride ID: {} from partition: {} with offset: {}", 
                eventDTO.getEventType(), eventDTO.getRideId(), partition, offset);
        
        try {
            // Handle driver assignment and acceptance/rejection events
            if ("DRIVER_ASSIGNED".equals(eventDTO.getEventType()) || 
                "DRIVER_ACCEPTED".equals(eventDTO.getEventType())) {
                rideService.assignDriver(eventDTO.getRideId(), eventDTO.getDriverId());
                log.info("Driver {} assigned to ride {}", eventDTO.getDriverId(), eventDTO.getRideId());
            } else if ("RIDE_STARTED".equals(eventDTO.getEventType())) {
                rideService.startRide(eventDTO.getRideId());
                log.info("Ride {} started by driver {}", eventDTO.getRideId(), eventDTO.getDriverId());
            } else if ("RIDE_COMPLETED".equals(eventDTO.getEventType())) {
                rideService.completeRide(eventDTO.getRideId());
                log.info("Ride {} completed by driver {}", eventDTO.getRideId(), eventDTO.getDriverId());
            } else if ("DRIVER_REJECTED".equals(eventDTO.getEventType())) {
                log.info("Driver {} rejected ride {}", eventDTO.getDriverId(), eventDTO.getRideId());
                // Trigger new matching request
            }
        } catch (Exception e) {
            log.error("Error processing driver event: {}", eventDTO, e);
        }
    }
}
