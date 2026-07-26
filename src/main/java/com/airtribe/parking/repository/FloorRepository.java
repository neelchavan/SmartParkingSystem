package com.airtribe.parking.repository;

import com.airtribe.parking.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID> {
    Optional<Floor> findByFloorNumber(int floorNumber);
}
