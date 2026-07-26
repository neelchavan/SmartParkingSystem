package com.airtribe.parking.observer;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotStatus;

public interface IAvailabilityObserver {
    void onSpotUpdated(Floor floor, ParkingSpot spot, SpotStatus newStatus);
}
