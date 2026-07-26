package com.airtribe.parking.service;

import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.exception.NoAvailableSpotException;
import com.airtribe.parking.repository.ParkingSpotRepository;
import com.airtribe.parking.strategy.allocation.IAllocationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thread-safe service that delegates spot selection to an {@link IAllocationStrategy}
 * and atomically marks the chosen spot OCCUPIED. The {@code synchronized} keyword
 * ensures only one thread can allocate or release a spot at a time (prevents double-
 * assignment when multiple vehicles arrive concurrently).
 */
@Service
public class SpotAllocationService {

    @Autowired
    private IAllocationStrategy allocationStrategy;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Transactional
    public synchronized ParkingSpot allocate(Vehicle vehicle) {
        ParkingSpot spot = allocationStrategy.allocate(vehicle)
                .orElseThrow(() -> new NoAvailableSpotException(
                        "No available spot for vehicle type: " + vehicle.getVehicleType()));

        spot.setStatus(SpotStatus.OCCUPIED);
        return parkingSpotRepository.save(spot);
    }

    @Transactional
    public synchronized void release(ParkingSpot spot) {
        spot.setStatus(SpotStatus.FREE);
        parkingSpotRepository.save(spot);
    }
}
