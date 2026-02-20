package com.uber.ride.controller;

import com.uber.ride.dto.RideRequestDTO;
import com.uber.ride.model.Ride;
import com.uber.ride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public ResponseEntity<Ride> requestRide(@Valid @RequestBody RideRequestDTO rideRequestDTO) {
        Ride ride = rideService.requestRide(rideRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<Ride> getRide(@PathVariable Long rideId) {
        Ride ride = rideService.getRideById(rideId);
        return ResponseEntity.ok(ride);
    }

    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = rideService.getAllRides();
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ride>> getRidesByUser(@PathVariable Long userId) {
        List<Ride> rides = rideService.getRidesByUserId(userId);
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getRidesByDriver(@PathVariable Long driverId) {
        List<Ride> rides = rideService.getRidesByDriverId(driverId);
        return ResponseEntity.ok(rides);
    }

    @PutMapping("/{rideId}/status")
    public ResponseEntity<Ride> updateRideStatus(
            @PathVariable Long rideId,
            @RequestParam Ride.RideStatus status) {
        Ride ride = rideService.updateRideStatus(rideId, status);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/assign-driver")
    public ResponseEntity<Ride> assignDriver(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {
        Ride ride = rideService.assignDriver(rideId, driverId);
        return ResponseEntity.ok(ride);
    }
}
