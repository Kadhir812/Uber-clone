package com.uber.driver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    private String vehicleModel;

    @Column(nullable = false)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @Column
    private Double currentLatitude;

    @Column
    private Double currentLongitude;

    @Column
    private Double rating;

    @Column
    private Integer totalRides = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Currently assigned ride
    @Column
    private Long currentRideId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DriverStatus.AVAILABLE;
        }
        if (rating == null) {
            rating = 5.0;
        }
        if (totalRides == null) {
            totalRides = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DriverStatus {
        AVAILABLE,      // Ready to accept rides
        BUSY,          // Currently on a ride
        OFFLINE,       // Not available
        ON_THE_WAY     // Going to pick up passenger
    }
}
