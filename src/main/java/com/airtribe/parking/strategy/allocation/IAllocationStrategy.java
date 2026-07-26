package com.airtribe.parking.strategy.allocation;

import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.entity.Vehicle;

import java.util.Optional;

public interface IAllocationStrategy {
    Optional<ParkingSpot> allocate(Vehicle vehicle);
}
