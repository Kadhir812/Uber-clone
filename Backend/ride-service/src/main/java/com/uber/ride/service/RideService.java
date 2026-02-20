package com.uber.ride.service;

import com.uber.ride.dto.RideEventDTO;
import com.uber.ride.dto.RideRequestDTO;
import com.uber.ride.kafka.producer.RideEventProducer;
import com.uber.ride.model.Ride;
import com.uber.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final RideEventProducer rideEventProducer;

    @Transactional
    public Ride requestRide(RideRequestDTO rideRequestDTO) {
        log.info("Requesting ride for user: {}", rideRequestDTO.getUserId());
        
        Ride ride = new Ride();
        ride.setUserId(rideRequestDTO.getUserId());
        ride.setPickupLocation(rideRequestDTO.getPickupLocation());
        ride.setDropoffLocation(rideRequestDTO.getDropoffLocation());
        ride.setFare(rideRequestDTO.getFare());
        ride.setStatus(Ride.RideStatus.REQUESTED);
        ride.setDriverId(0L); // Will be assigned by matching service
        
        Ride savedRide = rideRepository.save(ride);
        
        // Publish ride requested event
        RideEventDTO eventDTO = convertToEventDTO(savedRide, "RIDE_REQUESTED");
        rideEventProducer.sendRideEvent(eventDTO);
        
        return savedRide;
    }

    @Transactional
    public Ride updateRideStatus(Long rideId, Ride.RideStatus status) {
        log.info("Updating ride {} status to {}", rideId, status);
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        
        ride.setStatus(status);
        Ride updatedRide = rideRepository.save(ride);
        
        // Publish ride status updated event
        RideEventDTO eventDTO = convertToEventDTO(updatedRide, "RIDE_STATUS_UPDATED");
        rideEventProducer.sendRideEvent(eventDTO);
        
        return updatedRide;
    }

    @Transactional
    public Ride assignDriver(Long rideId, Long driverId) {
        log.info("Assigning driver {} to ride {}", driverId, rideId);
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        
        ride.setDriverId(driverId);
        ride.setStatus(Ride.RideStatus.ACCEPTED);
        Ride updatedRide = rideRepository.save(ride);
        
        // Publish driver assigned event
        RideEventDTO eventDTO = convertToEventDTO(updatedRide, "DRIVER_ASSIGNED");
        rideEventProducer.sendRideEvent(eventDTO);
        
        return updatedRide;
    }

    @Transactional
    public Ride startRide(Long rideId) {
        log.info("Starting ride {}", rideId);
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        
        ride.setStatus(Ride.RideStatus.IN_PROGRESS);
        Ride updatedRide = rideRepository.save(ride);
        
        log.info("Ride {} status updated to IN_PROGRESS", rideId);
        
        return updatedRide;
    }

    @Transactional
    public Ride completeRide(Long rideId) {
        log.info("Completing ride {}", rideId);
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
        
        ride.setStatus(Ride.RideStatus.COMPLETED);
        Ride updatedRide = rideRepository.save(ride);
        
        log.info("Ride {} status updated to COMPLETED", rideId);
        
        return updatedRide;
    }

    public Ride getRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));
    }

    public List<Ride> getRidesByUserId(Long userId) {
        return rideRepository.findByUserId(userId);
    }

    public List<Ride> getRidesByDriverId(Long driverId) {
        return rideRepository.findByDriverId(driverId);
    }

    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    private RideEventDTO convertToEventDTO(Ride ride, String eventType) {
        RideEventDTO eventDTO = new RideEventDTO();
        eventDTO.setRideId(ride.getId());
        eventDTO.setUserId(ride.getUserId());
        eventDTO.setDriverId(ride.getDriverId());
        eventDTO.setPickupLocation(ride.getPickupLocation());
        eventDTO.setDropoffLocation(ride.getDropoffLocation());
        eventDTO.setStatus(ride.getStatus().name());
        eventDTO.setFare(ride.getFare());
        eventDTO.setEventType(eventType);
        eventDTO.setTimestamp(System.currentTimeMillis());
        return eventDTO;
    }
}
