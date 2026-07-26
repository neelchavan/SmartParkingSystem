package com.airtribe.parking.strategy.allocation;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.repository.FloorRepository;
import com.airtribe.parking.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Best-fit by size: allocates the exact matching spot size for the vehicle type,
 * iterating floors from lowest to highest (nearest first).
 *
 * Mapping: MOTORCYCLE → SMALL, CAR → MEDIUM, BUS → LARGE
 */
@Component("bestFit")
@Primary
public class BestFitAllocationStrategy implements IAllocationStrategy {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    private static SpotSize requiredSize(VehicleType type) {
        return switch (type) {
            case MOTORCYCLE -> SpotSize.SMALL;
            case CAR        -> SpotSize.MEDIUM;
            case BUS        -> SpotSize.LARGE;
        };
    }

    @Override
    public Optional<ParkingSpot> allocate(Vehicle vehicle) {
        SpotSize required = requiredSize(vehicle.getVehicleType());

        List<Floor> floors = floorRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Floor::getFloorNumber))
                .toList();

        for (Floor floor : floors) {
            List<ParkingSpot> candidates = parkingSpotRepository
                    .findByFloorAndSpotSizeAndStatus(floor, required, SpotStatus.FREE);
            if (!candidates.isEmpty()) {
                candidates.sort(Comparator.comparing(ParkingSpot::getSpotNumber));
                return Optional.of(candidates.get(0));
            }
        }
        return Optional.empty();
    }
}
