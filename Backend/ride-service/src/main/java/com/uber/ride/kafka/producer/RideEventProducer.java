package com.uber.ride.kafka.producer;

import com.uber.ride.dto.RideEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventProducer {

    private static final String TOPIC = "ride-events";
    
    private final KafkaTemplate<String, RideEventDTO> kafkaTemplate;

    public void sendRideEvent(RideEventDTO rideEventDTO) {
        log.info("Sending ride event: {} for ride ID: {}", rideEventDTO.getEventType(), rideEventDTO.getRideId());
        
        CompletableFuture<SendResult<String, RideEventDTO>> future = 
                kafkaTemplate.send(TOPIC, String.valueOf(rideEventDTO.getRideId()), rideEventDTO);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Ride event sent successfully: {} with offset: {}", 
                        rideEventDTO.getEventType(), 
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send ride event: {}", rideEventDTO.getEventType(), ex);
            }
        });
    }
}
