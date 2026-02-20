package com.uber.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideEventDTO {

    private Long rideId;
    private Long userId;
    private Long driverId;
    private String pickupLocation;
    private String dropoffLocation;
    private String status;
    private Double fare;
    private String eventType;
    private Long timestamp;
}
