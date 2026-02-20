package com.uber.driver.dto;

import com.uber.driver.model.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponseDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String vehicleModel;
    private String vehiclePlate;
    private String status;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double rating;
    private Integer totalRides;
    private Long currentRideId;

    public static DriverResponseDTO fromEntity(Driver driver) {
        return DriverResponseDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .phoneNumber(driver.getPhoneNumber())
                .vehicleModel(driver.getVehicleModel())
                .vehiclePlate(driver.getVehiclePlate())
                .status(driver.getStatus().name())
                .currentLatitude(driver.getCurrentLatitude())
                .currentLongitude(driver.getCurrentLongitude())
                .rating(driver.getRating())
                .totalRides(driver.getTotalRides())
                .currentRideId(driver.getCurrentRideId())
                .build();
    }
}
