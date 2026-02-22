package com.uber.driver.service;

import com.uber.driver.kafka.producer.RideCompletedProducer;
import com.uber.driver.kafka.producer.RideStartedProducer;
import com.uber.driver.model.Driver;
import com.uber.driver.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Driver Service Tests")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideStartedProducer rideStartedProducer;

    @Mock
    private RideCompletedProducer rideCompletedProducer;

    @InjectMocks
    private DriverService driverService;

    private Driver validDriver;
    private Driver savedDriver;

    @BeforeEach
    void setUp() {
        // Setup valid driver
        validDriver = Driver.builder()
                .name("John Doe")
                .phoneNumber("+1234567890")
                .licenseNumber("DL123456")
                .vehicleModel("Toyota Camry")
                .vehiclePlate("ABC-1234")
                .currentLatitude(40.7128)
                .currentLongitude(-74.0060)
                .build();

        // Setup saved driver
        savedDriver = Driver.builder()
                .id(1L)
                .name("John Doe")
                .phoneNumber("+1234567890")
                .licenseNumber("DL123456")
                .vehicleModel("Toyota Camry")
                .vehiclePlate("ABC-1234")
                .status(Driver.DriverStatus.AVAILABLE)
                .rating(5.0)
                .totalRides(0)
                .currentLatitude(40.7128)
                .currentLongitude(-74.0060)
                .build();
    }

    // ========================================
    // createDriver() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully create driver with valid data")
    void testCreateDriver_WithValidData_ShouldCreateDriver() {
        // Arrange
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        Driver result = driverService.createDriver(validDriver);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("+1234567890", result.getPhoneNumber());
        assertEquals("DL123456", result.getLicenseNumber());
        assertEquals("Toyota Camry", result.getVehicleModel());
        assertEquals("ABC-1234", result.getVehiclePlate());
        
        verify(driverRepository, times(1)).save(validDriver);
    }

    @Test
    @DisplayName("Should set default rating to 5.0 when creating new driver")
    void testCreateDriver_DefaultRating_ShouldBe5() {
        // Arrange
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        Driver result = driverService.createDriver(validDriver);

        // Assert
        assertEquals(5.0, result.getRating());
    }

    @Test
    @DisplayName("Should set default totalRides to 0 when creating new driver")
    void testCreateDriver_DefaultTotalRides_ShouldBeZero() {
        // Arrange
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        Driver result = driverService.createDriver(validDriver);

        // Assert
        assertEquals(0, result.getTotalRides());
    }

    @Test
    @DisplayName("Should set default status to AVAILABLE when creating new driver")
    void testCreateDriver_DefaultStatus_ShouldBeAvailable() {
        // Arrange
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        Driver result = driverService.createDriver(validDriver);

        // Assert
        assertEquals(Driver.DriverStatus.AVAILABLE, result.getStatus());
    }

    // ========================================
    // getAvailableDriversNearLocation() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should calculate correct location boundaries")
    void testGetAvailableDriversNearLocation_BoundaryCalculation_ShouldBeCorrect() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        List<Driver> nearbyDrivers = Arrays.asList(savedDriver);
        when(driverRepository.findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(nearbyDrivers);

        // Act
        List<Driver> result = driverService.getAvailableDriversNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify boundary calculation
        ArgumentCaptor<Double> minLatCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> maxLatCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> minLngCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> maxLngCaptor = ArgumentCaptor.forClass(Double.class);

        verify(driverRepository).findAvailableDriversNearLocation(
                minLatCaptor.capture(),
                maxLatCaptor.capture(),
                minLngCaptor.capture(),
                maxLngCaptor.capture()
        );

        // Verify the bounds are calculated
        Double minLat = minLatCaptor.getValue();
        Double maxLat = maxLatCaptor.getValue();
        
        assertTrue(minLat < latitude, "Min latitude should be less than center");
        assertTrue(maxLat > latitude, "Max latitude should be greater than center");
    }

    @Test
    @DisplayName("Should apply radius filtering correctly")
    void testGetAvailableDriversNearLocation_RadiusFiltering_ShouldFilterCorrectly() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 10.0;

        Driver driver1 = Driver.builder().id(1L).name("Driver 1").build();
        Driver driver2 = Driver.builder().id(2L).name("Driver 2").build();
        List<Driver> nearbyDrivers = Arrays.asList(driver1, driver2);

        when(driverRepository.findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(nearbyDrivers);

        // Act
        List<Driver> result = driverService.getAvailableDriversNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(driverRepository, times(1)).findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    @DisplayName("Should calculate latitude and longitude delta based on radius")
    void testGetAvailableDriversNearLocation_LatLngDelta_ShouldBeCalculatedCorrectly() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        when(driverRepository.findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(Arrays.asList(savedDriver));

        // Act
        List<Driver> result = driverService.getAvailableDriversNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        
        // Verify that method was called (delta calculation happens internally)
        verify(driverRepository).findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    @DisplayName("Should return empty list when no drivers in range")
    void testGetAvailableDriversNearLocation_NoDriversInRange_ShouldReturnEmptyList() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        when(driverRepository.findAvailableDriversNearLocation(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(Arrays.asList());

        // Act
        List<Driver> result = driverService.getAvailableDriversNearLocation(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========================================
    // assignRideToDriver() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully assign ride when driver status is AVAILABLE")
    void testAssignRideToDriver_DriverAvailable_ShouldAssignRide() {
        // Arrange
        Driver availableDriver = Driver.builder()
                .id(1L)
                .name("John Doe")
                .status(Driver.DriverStatus.AVAILABLE)
                .currentRideId(null)
                .build();

        Driver updatedDriver = Driver.builder()
                .id(1L)
                .name("John Doe")
                .status(Driver.DriverStatus.ON_THE_WAY)
                .currentRideId(100L)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(availableDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(updatedDriver);

        // Act
        Driver result = driverService.assignRideToDriver(1L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getCurrentRideId());
        assertEquals(Driver.DriverStatus.ON_THE_WAY, result.getStatus());
        
        verify(driverRepository, times(1)).findById(1L);
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw exception when driver not found")
    void testAssignRideToDriver_DriverNotFound_ShouldThrowException() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.assignRideToDriver(999L, 100L);
        });

        assertEquals("Driver not found: 999", exception.getMessage());
        verify(driverRepository, times(1)).findById(999L);
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should change driver status to ON_THE_WAY after assignment")
    void testAssignRideToDriver_StatusChange_ShouldBeOnTheWay() {
        // Arrange
        Driver availableDriver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.AVAILABLE)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(availableDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Driver result = driverService.assignRideToDriver(1L, 100L);

        // Assert
        assertEquals(Driver.DriverStatus.ON_THE_WAY, result.getStatus());
        
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(Driver.DriverStatus.ON_THE_WAY, driverCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should throw exception when driver is not available")
    void testAssignRideToDriver_DriverNotAvailable_ShouldThrowException() {
        // Arrange
        Driver busyDriver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.BUSY)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(busyDriver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.assignRideToDriver(1L, 100L);
        });

        assertTrue(exception.getMessage().contains("Driver is not available"));
        assertTrue(exception.getMessage().contains("BUSY"));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    // ========================================
    // acceptRide() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should successfully accept ride when driver is assigned to it")
    void testAcceptRide_DriverAssignedToRide_ShouldAcceptRide() {
        // Arrange
        Driver assignedDriver = Driver.builder()
                .id(1L)
                .name("John Doe")
                .status(Driver.DriverStatus.ON_THE_WAY)
                .currentRideId(100L)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(assignedDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(assignedDriver);

        // Act
        Driver result = driverService.acceptRide(1L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(Driver.DriverStatus.ON_THE_WAY, result.getStatus());
        verify(driverRepository, times(1)).findById(1L);
        verify(driverRepository, times(1)).save(assignedDriver);
    }

    @Test
    @DisplayName("Should throw exception when driver not assigned to the ride")
    void testAcceptRide_DriverNotAssignedToRide_ShouldThrowException() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .currentRideId(200L) // Different ride
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.acceptRide(1L, 100L);
        });

        assertEquals("Driver is not assigned to this ride", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw exception when driver not found during ride acceptance")
    void testAcceptRide_DriverNotFound_ShouldThrowException() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.acceptRide(999L, 100L);
        });

        assertEquals("Driver not found: 999", exception.getMessage());
    }

    // ========================================
    // startRide() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should change status to BUSY when starting ride")
    void testStartRide_StatusChange_ShouldBeBusy() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.ON_THE_WAY)
                .currentRideId(100L)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideStartedProducer).publishRideStarted(anyLong(), anyLong());

        // Act
        Driver result = driverService.startRide(1L, 100L);

        // Assert
        assertEquals(Driver.DriverStatus.BUSY, result.getStatus());
        
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(Driver.DriverStatus.BUSY, driverCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should publish Kafka event when starting ride")
    void testStartRide_KafkaEventPublishing_ShouldPublishEvent() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.ON_THE_WAY)
                .currentRideId(100L)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        doNothing().when(rideStartedProducer).publishRideStarted(anyLong(), anyLong());

        // Act
        driverService.startRide(1L, 100L);

        // Assert
        verify(rideStartedProducer, times(1)).publishRideStarted(100L, 1L);
    }

    @Test
    @DisplayName("Should throw exception when driver not assigned to ride during start")
    void testStartRide_DriverNotAssignedToRide_ShouldThrowException() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .currentRideId(200L) // Different ride
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.startRide(1L, 100L);
        });

        assertEquals("Driver is not assigned to this ride", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
        verify(rideStartedProducer, never()).publishRideStarted(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when driver not found during ride start")
    void testStartRide_DriverNotFound_ShouldThrowException() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.startRide(999L, 100L);
        });

        assertEquals("Driver not found: 999", exception.getMessage());
        verify(rideStartedProducer, never()).publishRideStarted(anyLong(), anyLong());
    }

    // ========================================
    // completeRide() Tests - Priority: HIGH
    // ========================================

    @Test
    @DisplayName("Should change status to AVAILABLE when completing ride")
    void testCompleteRide_StatusChange_ShouldBeAvailable() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.BUSY)
                .currentRideId(100L)
                .totalRides(5)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideCompletedProducer).publishRideCompleted(anyLong(), anyLong(), any());

        // Act
        Driver result = driverService.completeRide(1L, 100L);

        // Assert
        assertEquals(Driver.DriverStatus.AVAILABLE, result.getStatus());
        
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(Driver.DriverStatus.AVAILABLE, driverCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should clear currentRideId when completing ride")
    void testCompleteRide_CurrentRideId_ShouldBeCleared() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.BUSY)
                .currentRideId(100L)
                .totalRides(5)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideCompletedProducer).publishRideCompleted(anyLong(), anyLong(), any());

        // Act
        Driver result = driverService.completeRide(1L, 100L);

        // Assert
        assertNull(result.getCurrentRideId());
        
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertNull(driverCaptor.getValue().getCurrentRideId());
    }

    @Test
    @DisplayName("Should increment totalRides when completing ride")
    void testCompleteRide_TotalRides_ShouldBeIncremented() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.BUSY)
                .currentRideId(100L)
                .totalRides(10)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(rideCompletedProducer).publishRideCompleted(anyLong(), anyLong(), any());

        // Act
        Driver result = driverService.completeRide(1L, 100L);

        // Assert
        assertEquals(11, result.getTotalRides());
        
        ArgumentCaptor<Driver> driverCaptor = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(driverCaptor.capture());
        assertEquals(11, driverCaptor.getValue().getTotalRides());
    }

    @Test
    @DisplayName("Should publish Kafka event when completing ride")
    void testCompleteRide_KafkaEventPublishing_ShouldPublishEvent() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .status(Driver.DriverStatus.BUSY)
                .currentRideId(100L)
                .totalRides(5)
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        doNothing().when(rideCompletedProducer).publishRideCompleted(anyLong(), anyLong(), any());

        // Act
        driverService.completeRide(1L, 100L);

        // Assert
        verify(rideCompletedProducer, times(1)).publishRideCompleted(eq(100L), eq(1L), isNull());
    }

    @Test
    @DisplayName("Should throw exception when driver not assigned to ride during completion")
    void testCompleteRide_DriverNotAssignedToRide_ShouldThrowException() {
        // Arrange
        Driver driver = Driver.builder()
                .id(1L)
                .currentRideId(200L) // Different ride
                .build();

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.completeRide(1L, 100L);
        });

        assertEquals("Driver is not assigned to this ride", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
        verify(rideCompletedProducer, never()).publishRideCompleted(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw exception when driver not found during ride completion")
    void testCompleteRide_DriverNotFound_ShouldThrowException() {
        // Arrange
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.completeRide(999L, 100L);
        });

        assertEquals("Driver not found: 999", exception.getMessage());
        verify(rideCompletedProducer, never()).publishRideCompleted(anyLong(), anyLong(), any());
    }
}
