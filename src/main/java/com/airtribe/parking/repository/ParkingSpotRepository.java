package com.airtribe.parking.repository;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {
    List<ParkingSpot> findByFloorAndSpotSizeAndStatus(Floor floor, SpotSize spotSize, SpotStatus status);
    List<ParkingSpot> findByFloorAndStatus(Floor floor, SpotStatus status);
    long countByFloorAndStatus(Floor floor, SpotStatus status);
    long countByFloorAndSpotSizeAndStatus(Floor floor, SpotSize spotSize, SpotStatus status);
}
