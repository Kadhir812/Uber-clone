package com.uber.driver.repository;

import com.uber.driver.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByPhoneNumber(String phoneNumber);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    List<Driver> findByStatus(Driver.DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' ORDER BY d.rating DESC")
    List<Driver> findAvailableDrivers();

    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE' AND " +
           "d.currentLatitude BETWEEN :minLat AND :maxLat AND " +
           "d.currentLongitude BETWEEN :minLng AND :maxLng " +
           "ORDER BY d.rating DESC")
    List<Driver> findAvailableDriversNearLocation(
        Double minLat, Double maxLat, 
        Double minLng, Double maxLng
    );

    Optional<Driver> findByCurrentRideId(Long rideId);
}
