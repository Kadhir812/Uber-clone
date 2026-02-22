package com.uber.matching.service;

import com.uber.matching.dto.RideEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Matching Service Tests")
class MatchingServiceTest {

    private MatchingService matchingService;
    private RideEventDTO validRideRequest;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService();
        
        // Setup valid ride request
        validRideRequest = RideEventDTO.builder()
                .rideId(100L)
                .userId(1L)
                .eventType("RIDE_REQUESTED")
                .status("REQUESTED")
                .pickupLocation("123 Main St")
                .dropoffLocation("456 Elm St")
                .fare(25.50)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ========================================
    // findDriverForRide() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully find driver for ride request")
    void testFindDriverForRide_DriverAvailable_ShouldFindDriver() {
        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getDriverId(), "Driver ID should be assigned");
        assertTrue(result.getDriverId() > 0, "Driver ID should be positive");
        assertEquals("DRIVER_ASSIGNED", result.getEventType());
        assertEquals("ASSIGNED", result.getStatus());
        assertEquals("123 Main St", result.getPickupLocation());
        assertEquals("456 Elm St", result.getDropoffLocation());
        assertEquals(25.50, result.getFare());
    }

    @Test
    @DisplayName("Should mark driver as unavailable after assignment")
    void testFindDriverForRide_AfterAssignment_DriverShouldBeUnavailable() {
        // Arrange
        long initialAvailableCount = matchingService.getAvailableDriverCount();

        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);
        long afterAssignmentCount = matchingService.getAvailableDriverCount();

        // Assert
        assertNotNull(result.getDriverId());
        assertEquals(initialAvailableCount - 1, afterAssignmentCount, 
                "Available driver count should decrease by 1 after assignment");
    }

    @Test
    @DisplayName("Should return NO_DRIVER_AVAILABLE event when no drivers available")
    void testFindDriverForRide_NoDriversAvailable_ShouldReturnNoDriverEvent() {
        // Arrange - Assign all available drivers
        long availableCount = matchingService.getAvailableDriverCount();
        for (int i = 0; i < availableCount; i++) {
            RideEventDTO request = RideEventDTO.builder()
                    .rideId(100L + i)
                    .userId(1L)
                    .pickupLocation("Location " + i)
                    .dropoffLocation("Destination " + i)
                    .fare(25.0)
                    .build();
            matchingService.findDriverForRide(request);
        }

        // Act - Try to find driver when none available
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        assertEquals(1L, result.getUserId());
        assertNull(result.getDriverId(), "Driver ID should be null when no drivers available");
        assertEquals("NO_DRIVER_AVAILABLE", result.getEventType());
        assertEquals("REQUESTED", result.getStatus());
        assertTrue(result.getMessage().contains("No drivers available"));
    }

    @Test
    @DisplayName("Should set event type to DRIVER_ASSIGNED when driver found")
    void testFindDriverForRide_EventType_ShouldBeDriverAssigned() {
        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertEquals("DRIVER_ASSIGNED", result.getEventType());
        assertTrue(result.getMessage().contains("Driver"));
        assertTrue(result.getMessage().contains("assigned"));
    }

    @Test
    @DisplayName("Should preserve ride details in assignment event")
    void testFindDriverForRide_ShouldPreserveRideDetails() {
        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertEquals(validRideRequest.getRideId(), result.getRideId());
        assertEquals(validRideRequest.getUserId(), result.getUserId());
        assertEquals(validRideRequest.getPickupLocation(), result.getPickupLocation());
        assertEquals(validRideRequest.getDropoffLocation(), result.getDropoffLocation());
        assertEquals(validRideRequest.getFare(), result.getFare());
    }

    // ========================================
    // findNearestAvailableDriver() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should return available driver ID when drivers are available")
    void testFindNearestAvailableDriver_DriversAvailable_ShouldReturnDriverId() {
        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertNotNull(result.getDriverId(), "Should return a driver ID");
        assertTrue(result.getDriverId() >= 1 && result.getDriverId() <= 3, 
                "Driver ID should be within initialized range (1-3)");
    }

    @Test
    @DisplayName("Should return null when no drivers are available")
    void testFindNearestAvailableDriver_NoDriversAvailable_ShouldReturnNull() {
        // Arrange - Exhaust all available drivers
        long availableCount = matchingService.getAvailableDriverCount();
        for (int i = 0; i < availableCount; i++) {
            RideEventDTO request = RideEventDTO.builder()
                    .rideId(200L + i)
                    .userId(2L)
                    .pickupLocation("Location " + i)
                    .dropoffLocation("Destination " + i)
                    .fare(30.0)
                    .build();
            matchingService.findDriverForRide(request);
        }

        // Act
        RideEventDTO result = matchingService.findDriverForRide(validRideRequest);

        // Assert
        assertNull(result.getDriverId(), "Should return null when no drivers available");
        assertEquals("NO_DRIVER_AVAILABLE", result.getEventType());
    }

    @Test
    @DisplayName("Should filter out busy drivers and only return available ones")
    void testFindNearestAvailableDriver_FiltersBusyDrivers_ShouldReturnOnlyAvailable() {
        // Arrange
        long initialCount = matchingService.getAvailableDriverCount();
        
        // Assign one driver
        RideEventDTO firstRequest = RideEventDTO.builder()
                .rideId(101L)
                .userId(1L)
                .pickupLocation("Location 1")
                .dropoffLocation("Destination 1")
                .fare(20.0)
                .build();
        RideEventDTO firstResult = matchingService.findDriverForRide(firstRequest);
        Long busyDriverId = firstResult.getDriverId();

        // Act - Request second ride (should get different driver)
        RideEventDTO secondRequest = RideEventDTO.builder()
                .rideId(102L)
                .userId(2L)
                .pickupLocation("Location 2")
                .dropoffLocation("Destination 2")
                .fare(30.0)
                .build();
        RideEventDTO secondResult = matchingService.findDriverForRide(secondRequest);

        // Assert
        assertNotNull(secondResult.getDriverId());
        assertNotEquals(busyDriverId, secondResult.getDriverId(), 
                "Should assign different driver, not the busy one");
        assertEquals(initialCount - 2, matchingService.getAvailableDriverCount(), 
                "Two drivers should now be busy");
    }

    @Test
    @DisplayName("Should handle multiple available drivers correctly")
    void testFindNearestAvailableDriver_MultipleAvailable_ShouldSelectOne() {
        // Arrange - Get initial count
        long initialCount = matchingService.getAvailableDriverCount();
        assertTrue(initialCount > 0, "Should have available drivers initially");

        // Act - Assign drivers one by one
        for (int i = 0; i < initialCount; i++) {
            RideEventDTO request = RideEventDTO.builder()
                    .rideId(300L + i)
                    .userId(3L)
                    .pickupLocation("Location " + i)
                    .dropoffLocation("Destination " + i)
                    .fare(15.0 + i)
                    .build();
            
            RideEventDTO result = matchingService.findDriverForRide(request);
            
            // Assert each iteration
            assertNotNull(result.getDriverId(), "Should assign driver #" + (i + 1));
            assertEquals(initialCount - (i + 1), matchingService.getAvailableDriverCount(), 
                    "Available count should decrease after each assignment");
        }

        // Final state - all drivers should be busy
        assertEquals(0, matchingService.getAvailableDriverCount(), 
                "All drivers should be busy after exhausting pool");
    }

    // ========================================
    // releaseDriver() Tests - Priority: MEDIUM
    // ========================================

    @Test
    @DisplayName("Should make driver available after release")
    void testReleaseDriver_AfterRelease_DriverShouldBeAvailable() {
        // Arrange - Assign a driver first
        RideEventDTO assignRequest = RideEventDTO.builder()
                .rideId(500L)
                .userId(5L)
                .pickupLocation("Start Location")
                .dropoffLocation("End Location")
                .fare(40.0)
                .build();
        RideEventDTO assignResult = matchingService.findDriverForRide(assignRequest);
        Long assignedDriverId = assignResult.getDriverId();
        long countBeforeRelease = matchingService.getAvailableDriverCount();

        // Act - Release the driver
        matchingService.releaseDriver(assignedDriverId);
        long countAfterRelease = matchingService.getAvailableDriverCount();

        // Assert
        assertEquals(countBeforeRelease + 1, countAfterRelease, 
                "Available driver count should increase by 1 after release");
    }

    @Test
    @DisplayName("Should handle release of non-existent driver gracefully")
    void testReleaseDriver_NonExistentDriver_ShouldHandleGracefully() {
        // Arrange
        Long nonExistentDriverId = 999L;
        long countBefore = matchingService.getAvailableDriverCount();

        // Act - Should not throw exception
        assertDoesNotThrow(() -> {
            matchingService.releaseDriver(nonExistentDriverId);
        });

        long countAfter = matchingService.getAvailableDriverCount();

        // Assert - Count should remain unchanged
        assertEquals(countBefore, countAfter, 
                "Available count should not change for non-existent driver");
    }

    @Test
    @DisplayName("Should allow reassigning driver after release")
    void testReleaseDriver_AfterRelease_DriverCanBeReassigned() {
        // Arrange - Assign and release a driver
        RideEventDTO firstRequest = RideEventDTO.builder()
                .rideId(600L)
                .userId(6L)
                .pickupLocation("Location A")
                .dropoffLocation("Location B")
                .fare(50.0)
                .build();
        RideEventDTO firstResult = matchingService.findDriverForRide(firstRequest);
        Long driverId = firstResult.getDriverId();
        
        matchingService.releaseDriver(driverId);

        // Act - Try to assign another ride
        RideEventDTO secondRequest = RideEventDTO.builder()
                .rideId(601L)
                .userId(7L)
                .pickupLocation("Location C")
                .dropoffLocation("Location D")
                .fare(60.0)
                .build();
        RideEventDTO secondResult = matchingService.findDriverForRide(secondRequest);

        // Assert
        assertNotNull(secondResult.getDriverId(), "Should be able to assign driver again");
        assertEquals("DRIVER_ASSIGNED", secondResult.getEventType());
    }

    // ========================================
    // Additional Helper Tests
    // ========================================

    @Test
    @DisplayName("Should initialize with 3 mock drivers")
    void testInitialization_ShouldHaveThreeMockDrivers() {
        // Arrange & Act
        long availableCount = matchingService.getAvailableDriverCount();

        // Assert
        assertEquals(3, availableCount, "Should initialize with 3 mock drivers");
    }

    @Test
    @DisplayName("Should handle ride cancellation and release driver")
    void testHandleRideCancellation_ShouldReleaseDriver() {
        // Arrange - Assign a driver
        RideEventDTO request = RideEventDTO.builder()
                .rideId(700L)
                .userId(7L)
                .pickupLocation("Pick Location")
                .dropoffLocation("Drop Location")
                .fare(35.0)
                .build();
        RideEventDTO result = matchingService.findDriverForRide(request);
        Long driverId = result.getDriverId();
        long countBeforeCancel = matchingService.getAvailableDriverCount();

        // Act
        matchingService.handleRideCancellation(700L, driverId);
        long countAfterCancel = matchingService.getAvailableDriverCount();

        // Assert
        assertEquals(countBeforeCancel + 1, countAfterCancel, 
                "Driver should be released after ride cancellation");
    }

    @Test
    @DisplayName("Should handle cancellation with null driver ID")
    void testHandleRideCancellation_WithNullDriverId_ShouldNotFail() {
        // Arrange
        long countBefore = matchingService.getAvailableDriverCount();

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            matchingService.handleRideCancellation(800L, null);
        });

        long countAfter = matchingService.getAvailableDriverCount();
        assertEquals(countBefore, countAfter, "Count should remain unchanged");
    }

    @Test
    @DisplayName("Should handle cancellation with zero driver ID")
    void testHandleRideCancellation_WithZeroDriverId_ShouldNotFail() {
        // Arrange
        long countBefore = matchingService.getAvailableDriverCount();

        // Act & Assert
        assertDoesNotThrow(() -> {
            matchingService.handleRideCancellation(900L, 0L);
        });

        long countAfter = matchingService.getAvailableDriverCount();
        assertEquals(countBefore, countAfter, "Count should remain unchanged");
    }
}
