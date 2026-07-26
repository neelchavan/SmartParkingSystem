package com.airtribe.parking.observer;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Prints a real-time availability summary whenever a spot changes state.
 * Subscribed automatically via Spring's List injection in ParkingLotService.
 */
@Component
public class DisplayBoardObserver implements IAvailabilityObserver {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Override
    public void onSpotUpdated(Floor floor, ParkingSpot spot, SpotStatus newStatus) {
        long freeSmall  = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.SMALL,  SpotStatus.FREE);
        long freeMedium = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE);
        long freeLarge  = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.LARGE,  SpotStatus.FREE);
        long total      = freeSmall + freeMedium + freeLarge;

        System.out.printf(
                "  [DisplayBoard] Floor %-2d | Free: %d total (S:%d M:%d L:%d) | %s → %s%n",
                floor.getFloorNumber(), total, freeSmall, freeMedium, freeLarge,
                spot.getSpotNumber(), newStatus
        );
    }
}
