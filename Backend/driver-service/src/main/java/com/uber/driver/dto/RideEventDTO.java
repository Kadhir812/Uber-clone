package com.uber.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideEventDTO {
    private Long rideId;
    private Long userId;
    private Long driverId;
    private String eventType;
    private String status;
    private String pickupLocation;
    private String dropoffLocation;
    private Double fare;
    private String message;
    private Long timestamp;
}
