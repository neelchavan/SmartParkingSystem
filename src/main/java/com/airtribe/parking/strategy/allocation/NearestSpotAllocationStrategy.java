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
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Nearest-spot strategy: picks the smallest available spot that fits the vehicle,
 * iterating floors from lowest to highest. Falls back to larger sizes if no exact
 * match is available on any floor.
 *
 * Fallback order: MOTORCYCLE → SMALL > MEDIUM > LARGE
 *                 CAR        → MEDIUM > LARGE
 *                 BUS        → LARGE only
 */
@Component("nearest")
public class NearestSpotAllocationStrategy implements IAllocationStrategy {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    private static List<SpotSize> suitableSizes(VehicleType type) {
        return switch (type) {
            case MOTORCYCLE -> List.of(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE);
            case CAR        -> List.of(SpotSize.MEDIUM, SpotSize.LARGE);
            case BUS        -> List.of(SpotSize.LARGE);
        };
    }

    @Override
    public Optional<ParkingSpot> allocate(Vehicle vehicle) {
        List<SpotSize> sizes = suitableSizes(vehicle.getVehicleType());

        List<Floor> floors = floorRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Floor::getFloorNumber))
                .toList();

        for (Floor floor : floors) {
            for (SpotSize size : sizes) {
                List<ParkingSpot> candidates = parkingSpotRepository
                        .findByFloorAndSpotSizeAndStatus(floor, size, SpotStatus.FREE);
                if (!candidates.isEmpty()) {
                    candidates.sort(Comparator.comparing(ParkingSpot::getSpotNumber));
                    return Optional.of(candidates.get(0));
                }
            }
        }
        return Optional.empty();
    }
}
