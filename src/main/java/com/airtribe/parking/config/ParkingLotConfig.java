package com.airtribe.parking.config;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.repository.FloorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ParkingLotConfig {

    @Bean
    @Order(1)
    public CommandLineRunner seedParkingLot(FloorRepository floorRepository) {
        return args -> {
            if (floorRepository.count() == 0) {
                for (int f = 1; f <= 3; f++) {
                    Floor floor = new Floor(f);
                    int spotIndex = 1;
                    for (int i = 0; i < 5; i++) {
                        floor.addSpot(new ParkingSpot("F" + f + "-S" + spotIndex++, SpotSize.SMALL, floor));
                    }
                    for (int i = 0; i < 5; i++) {
                        floor.addSpot(new ParkingSpot("F" + f + "-S" + spotIndex++, SpotSize.MEDIUM, floor));
                    }
                    for (int i = 0; i < 3; i++) {
                        floor.addSpot(new ParkingSpot("F" + f + "-S" + spotIndex++, SpotSize.LARGE, floor));
                    }
                    floorRepository.save(floor);
                }
                System.out.println("[ParkingLotConfig] Seeded 3 floors with 13 spots each.");
            }
        };
    }
}
