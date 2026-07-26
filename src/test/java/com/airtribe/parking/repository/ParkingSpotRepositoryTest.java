package com.airtribe.parking.repository;

import com.airtribe.parking.ParkingLotApp;
import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.repository.FloorRepository;
import com.airtribe.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-level CRUD and query tests using the full Spring Boot context
 * backed by the in-memory H2 database.
 *
 * Each test method is rolled back via @Transactional, so tests are isolated.
 * ConsoleMenu is mocked to prevent it from blocking on stdin at context startup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ParkingSpotRepositoryTest {

    @MockBean
    ParkingLotApp.ConsoleMenu consoleMenu;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private FloorRepository floorRepository;

    private Floor floor;

    @BeforeEach
    void setUp() {
        // Create an extra test floor separate from the seeded floors
        floor = new Floor(99);
        floor.addSpot(new ParkingSpot("T99-S1", SpotSize.SMALL,  floor));
        floor.addSpot(new ParkingSpot("T99-S2", SpotSize.SMALL,  floor));
        floor.addSpot(new ParkingSpot("T99-M1", SpotSize.MEDIUM, floor));
        floor.addSpot(new ParkingSpot("T99-M2", SpotSize.MEDIUM, floor));
        floor.addSpot(new ParkingSpot("T99-L1", SpotSize.LARGE,  floor));
        floor = floorRepository.save(floor); // cascades to spots
    }

    // CRUD

    @Test
    void save_persistsSpot_findById_returnsIt() {
        ParkingSpot spot = new ParkingSpot("T99-NEW", SpotSize.MEDIUM, floor);
        ParkingSpot saved = parkingSpotRepository.save(spot);

        assertThat(saved.getId()).isNotNull();

        Optional<ParkingSpot> found = parkingSpotRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSpotNumber()).isEqualTo("T99-NEW");
        assertThat(found.get().getSpotSize()).isEqualTo(SpotSize.MEDIUM);
        assertThat(found.get().getStatus()).isEqualTo(SpotStatus.FREE);
    }

    @Test
    void delete_removesSpotFromRepository() {
        // Save a standalone spot, then verify deletion by ID.
        // We do not remove a spot that is still in floor.spots to avoid an
        // orphanRemoval conflict when Hibernate flushes the session.
        ParkingSpot extra = new ParkingSpot("T99-EXTRA", SpotSize.LARGE, floor);
        extra = parkingSpotRepository.save(extra);
        var savedId = extra.getId();

        parkingSpotRepository.delete(extra);
        parkingSpotRepository.flush(); // ensure deletion is written before the check

        assertThat(parkingSpotRepository.findById(savedId)).isNotPresent();
    }

    // findByFloorAndSpotSizeAndStatus

    @Test
    void findByFloorAndSpotSizeAndStatus_returnsFreeSmallSpots() {
        List<ParkingSpot> small = parkingSpotRepository
                .findByFloorAndSpotSizeAndStatus(floor, SpotSize.SMALL, SpotStatus.FREE);

        assertThat(small).hasSize(2);
        assertThat(small).allMatch(s -> s.getSpotSize() == SpotSize.SMALL);
        assertThat(small).allMatch(s -> s.getStatus()   == SpotStatus.FREE);
    }

    @Test
    void findByFloorAndSpotSizeAndStatus_returnsEmptyAfterAllOccupied() {
        // Occupy both MEDIUM spots
        parkingSpotRepository
                .findByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE)
                .forEach(s -> { s.setStatus(SpotStatus.OCCUPIED); parkingSpotRepository.save(s); });

        List<ParkingSpot> result = parkingSpotRepository
                .findByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE);

        assertThat(result).isEmpty();
    }

    @Test
    void findByFloorAndStatus_returnsAllFreeSpots() {
        List<ParkingSpot> free = parkingSpotRepository
                .findByFloorAndStatus(floor, SpotStatus.FREE);

        assertThat(free).hasSize(5);
    }

    // countByFloorAndStatus / countByFloorAndSpotSizeAndStatus

    @Test
    void countByFloorAndStatus_countsAllFreeSpots() {
        long count = parkingSpotRepository.countByFloorAndStatus(floor, SpotStatus.FREE);
        assertThat(count).isEqualTo(5);
    }

    @Test
    void countByFloorAndSpotSizeAndStatus_countsMediumFreeSpots() {
        long count = parkingSpotRepository
                .countByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void updateStatusToOccupied_decrementsFreeMediumCount() {
        ParkingSpot medium = parkingSpotRepository
                .findByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE)
                .get(0);

        medium.setStatus(SpotStatus.OCCUPIED);
        parkingSpotRepository.save(medium);

        assertThat(parkingSpotRepository
                .countByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE))
                .isEqualTo(1);

        assertThat(parkingSpotRepository.countByFloorAndStatus(floor, SpotStatus.FREE))
                .isEqualTo(4);
    }

    @Test
    void releaseOccupiedSpot_incrementsFreeMediumCount() {
        ParkingSpot medium = parkingSpotRepository
                .findByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE)
                .get(0);

        // Occupy then release
        medium.setStatus(SpotStatus.OCCUPIED);
        parkingSpotRepository.save(medium);

        medium.setStatus(SpotStatus.FREE);
        parkingSpotRepository.save(medium);

        assertThat(parkingSpotRepository
                .countByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE))
                .isEqualTo(2);
    }
}
