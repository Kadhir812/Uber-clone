package com.uber.ride.service;

import com.uber.ride.dto.RideEventDTO;
import com.uber.ride.dto.RideRequestDTO;
import com.uber.ride.kafka.producer.RideEventProducer;
import com.uber.ride.model.Ride;
import com.uber.ride.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Ride Service Tests")
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideEventProducer rideEventProducer;

    @InjectMocks
    private RideService rideService;

    private RideRequestDTO validRideRequest;
    private Ride savedRide;

    @BeforeEach
    void setUp() {
        // Setup valid ride request
        validRideRequest = new RideRequestDTO();
        validRideRequest.setUserId(1L);
        validRideRequest.setPickupLocation("123 Main St");
        validRideRequest.setDropoffLocation("456 Elm St");
        validRideRequest.setFare(25.50);

        // Setup saved ride
        savedRide = new Ride();
        savedRide.setId(100L);
        savedRide.setUserId(1L);
        savedRide.setPickupLocation("123 Main St");
        savedRide.setDropoffLocation("456 Elm St");
        savedRide.setFare(25.50);
        savedRide.setStatus(Ride.RideStatus.REQUESTED);
        savedRide.setDriverId(0L);
    }

    // ========================================
    // requestRide() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully create ride with valid request")
    void testRequestRide_WithValidRequest_ShouldCreateRide() {
        // Arrange
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.requestRide(validRideRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("123 Main St", result.getPickupLocation());
        assertEquals("456 Elm St", result.getDropoffLocation());
        assertEquals(25.50, result.getFare());
        assertEquals(Ride.RideStatus.REQUESTED, result.getStatus());
        assertEquals(0L, result.getDriverId());

        // Verify repository was called
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Should set fare correctly from request")
    void testRequestRide_FareCalculation_ShouldSetCorrectFare() {
        // Arrange
        validRideRequest.setFare(100.00);
        savedRide.setFare(100.00);
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.requestRide(validRideRequest);

        // Assert
        assertEquals(100.00, result.getFare());
        
        // Verify fare was set correctly before saving
        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(rideCaptor.capture());
        assertEquals(100.00, rideCaptor.getValue().getFare());
    }

    @Test
    @DisplayName("Should publish Kafka event after ride creation")
    void testRequestRide_KafkaEventPublishing_ShouldPublishEvent() {
        // Arrange
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        rideService.requestRide(validRideRequest);

        // Assert - Verify Kafka event was published
        ArgumentCaptor<RideEventDTO> eventCaptor = ArgumentCaptor.forClass(RideEventDTO.class);
        verify(rideEventProducer, times(1)).sendRideEvent(eventCaptor.capture());

        RideEventDTO publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent);
        assertEquals(100L, publishedEvent.getRideId());
        assertEquals(1L, publishedEvent.getUserId());
        assertEquals("RIDE_REQUESTED", publishedEvent.getEventType());
        assertEquals("REQUESTED", publishedEvent.getStatus());
        assertEquals(25.50, publishedEvent.getFare());
    }

    @Test
    @DisplayName("Should set initial status to REQUESTED")
    void testRequestRide_InitialStatus_ShouldBeRequested() {
        // Arrange
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.requestRide(validRideRequest);

        // Assert
        assertEquals(Ride.RideStatus.REQUESTED, result.getStatus());
        
        // Verify status was set before saving
        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(rideCaptor.capture());
        assertEquals(Ride.RideStatus.REQUESTED, rideCaptor.getValue().getStatus());
    }

    // ========================================
    // updateRideStatus() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully update ride status with valid input")
    void testUpdateRideStatus_WithValidInput_ShouldUpdateStatus() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setUserId(1L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);
        existingRide.setFare(25.50);

        Ride updatedRide = new Ride();
        updatedRide.setId(100L);
        updatedRide.setUserId(1L);
        updatedRide.setStatus(Ride.RideStatus.ACCEPTED);
        updatedRide.setFare(25.50);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(updatedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.updateRideStatus(100L, Ride.RideStatus.ACCEPTED);

        // Assert
        assertEquals(Ride.RideStatus.ACCEPTED, result.getStatus());
        verify(rideRepository, times(1)).findById(100L);
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Should throw exception when ride ID is invalid")
    void testUpdateRideStatus_WithInvalidRideId_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rideService.updateRideStatus(999L, Ride.RideStatus.ACCEPTED);
        });

        assertEquals("Ride not found with id: 999", exception.getMessage());
        verify(rideRepository, times(1)).findById(999L);
        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideEventProducer, never()).sendRideEvent(any(RideEventDTO.class));
    }

    @Test
    @DisplayName("Should handle status transition from REQUESTED to ACCEPTED")
    void testUpdateRideStatus_StatusTransition_RequestedToAccepted() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.updateRideStatus(100L, Ride.RideStatus.ACCEPTED);

        // Assert
        assertEquals(Ride.RideStatus.ACCEPTED, result.getStatus());
    }

    @Test
    @DisplayName("Should handle status transition from ACCEPTED to IN_PROGRESS")
    void testUpdateRideStatus_StatusTransition_AcceptedToInProgress() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setStatus(Ride.RideStatus.ACCEPTED);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.updateRideStatus(100L, Ride.RideStatus.IN_PROGRESS);

        // Assert
        assertEquals(Ride.RideStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    @DisplayName("Should handle status transition from IN_PROGRESS to COMPLETED")
    void testUpdateRideStatus_StatusTransition_InProgressToCompleted() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setStatus(Ride.RideStatus.IN_PROGRESS);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.updateRideStatus(100L, Ride.RideStatus.COMPLETED);

        // Assert
        assertEquals(Ride.RideStatus.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("Should publish Kafka event after status update")
    void testUpdateRideStatus_ShouldPublishKafkaEvent() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setUserId(1L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);
        existingRide.setFare(25.50);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        rideService.updateRideStatus(100L, Ride.RideStatus.ACCEPTED);

        // Assert
        ArgumentCaptor<RideEventDTO> eventCaptor = ArgumentCaptor.forClass(RideEventDTO.class);
        verify(rideEventProducer, times(1)).sendRideEvent(eventCaptor.capture());

        RideEventDTO publishedEvent = eventCaptor.getValue();
        assertEquals("RIDE_STATUS_UPDATED", publishedEvent.getEventType());
        assertEquals("ACCEPTED", publishedEvent.getStatus());
    }

    // ========================================
    // assignDriver() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully assign driver to ride")
    void testAssignDriver_WithValidInput_ShouldAssignDriver() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setUserId(1L);
        existingRide.setDriverId(0L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);
        existingRide.setFare(25.50);

        Ride updatedRide = new Ride();
        updatedRide.setId(100L);
        updatedRide.setUserId(1L);
        updatedRide.setDriverId(5L);
        updatedRide.setStatus(Ride.RideStatus.ACCEPTED);
        updatedRide.setFare(25.50);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(updatedRide);
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.assignDriver(100L, 5L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(5L, result.getDriverId());
        assertEquals(Ride.RideStatus.ACCEPTED, result.getStatus());
        
        verify(rideRepository, times(1)).findById(100L);
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Should throw exception when ride is not found during driver assignment")
    void testAssignDriver_WithInvalidRideId_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rideService.assignDriver(999L, 5L);
        });

        assertEquals("Ride not found with id: 999", exception.getMessage());
        verify(rideRepository, times(1)).findById(999L);
        verify(rideRepository, never()).save(any(Ride.class));
        verify(rideEventProducer, never()).sendRideEvent(any(RideEventDTO.class));
    }

    @Test
    @DisplayName("Should change status to ACCEPTED after driver assignment")
    void testAssignDriver_StatusChange_ShouldBeAccepted() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setUserId(1L);
        existingRide.setDriverId(0L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.assignDriver(100L, 5L);

        // Assert
        assertEquals(Ride.RideStatus.ACCEPTED, result.getStatus());
        
        // Verify status was updated before saving
        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(rideCaptor.capture());
        assertEquals(Ride.RideStatus.ACCEPTED, rideCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should publish DRIVER_ASSIGNED event after driver assignment")
    void testAssignDriver_ShouldPublishDriverAssignedEvent() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setUserId(1L);
        existingRide.setDriverId(0L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);
        existingRide.setFare(25.50);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        rideService.assignDriver(100L, 5L);

        // Assert
        ArgumentCaptor<RideEventDTO> eventCaptor = ArgumentCaptor.forClass(RideEventDTO.class);
        verify(rideEventProducer, times(1)).sendRideEvent(eventCaptor.capture());

        RideEventDTO publishedEvent = eventCaptor.getValue();
        assertEquals("DRIVER_ASSIGNED", publishedEvent.getEventType());
        assertEquals(100L, publishedEvent.getRideId());
        assertEquals(5L, publishedEvent.getDriverId());
        assertEquals("ACCEPTED", publishedEvent.getStatus());
    }

    @Test
    @DisplayName("Should set correct driver ID during assignment")
    void testAssignDriver_DriverId_ShouldBeSetCorrectly() {
        // Arrange
        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setDriverId(0L);
        existingRide.setStatus(Ride.RideStatus.REQUESTED);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideEventProducer).sendRideEvent(any(RideEventDTO.class));

        // Act
        Ride result = rideService.assignDriver(100L, 42L);

        // Assert
        assertEquals(42L, result.getDriverId());
        
        // Verify driver ID was set before saving
        ArgumentCaptor<Ride> rideCaptor = ArgumentCaptor.forClass(Ride.class);
        verify(rideRepository).save(rideCaptor.capture());
        assertEquals(42L, rideCaptor.getValue().getDriverId());
    }
}
