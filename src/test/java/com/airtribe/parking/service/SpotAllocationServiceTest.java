package com.airtribe.parking.service;

import com.airtribe.parking.ParkingLotApp;
import com.airtribe.parking.entity.Car;
import com.airtribe.parking.entity.Motorcycle;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.exception.NoAvailableSpotException;
import com.airtribe.parking.service.SpotAllocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency tests for SpotAllocationService.
 *
 * Design note on synchronized + @Transactional:
 *   Spring's @Transactional proxy wraps the method OUTSIDE the synchronized keyword.
 *   This means two threads can have overlapping transactions even though only one
 *   thread executes the critical section at a time. The @Version field on ParkingSpot
 *   acts as the real guard against double-assignment: if two threads try to commit an
 *   update to the same spot, one receives an ObjectOptimisticLockingFailureException.
 *
 *   The tests here verify that:
 *     (a) Sequential allocations never produce duplicates.
 *     (b) Under true concurrency, every SUCCESSFULLY committed allocation targets a
 *         unique spot — the @Version field prevents any two threads from committing
 *         on the same row.
 *
 * @DirtiesContext ensures the committed (non-rolled-back) allocations don't leak into
 * later test classes that share the Spring context.
 * Tests are NOT @Transactional — threads must commit their work for the test to be valid.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SpotAllocationServiceTest {

    @MockBean
    ParkingLotApp.ConsoleMenu consoleMenu;

    @Autowired
    SpotAllocationService spotAllocationService;

    // Sequential uniqueness — baseline sanity check

    @Test
    void sequentialAllocations_noDuplicateSpots() {
        int count = 10; // well within the 15 MEDIUM spots seeded by ParkingLotConfig
        List<UUID> ids = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ParkingSpot spot = spotAllocationService.allocate(new Car("SEQ-CAR-" + i));
            ids.add(spot.getId());
        }

        assertThat(ids.stream().distinct().count())
                .as("Every sequential allocation must target a unique spot")
                .isEqualTo(count);
    }

    // Concurrent uniqueness — @Version prevents double-assignment

    @Test
    void concurrentCarAllocations_noDoubleAssignment() throws InterruptedException {
        int threadCount = 5; // small number to keep the test fast and deterministic

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<UUID>      allocatedSpotIds = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> unrecoverableErrors = Collections.synchronizedList(new ArrayList<>());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // all threads start together

                    // Retry on optimistic-lock conflict — this is expected under concurrency.
                    // The @Version field on ParkingSpot ensures no two successful commits
                    // ever land on the same row.
                    Exception lastException = null;
                    for (int attempt = 0; attempt < 5; attempt++) {
                        try {
                            ParkingSpot spot = spotAllocationService.allocate(new Car("CONC-" + idx));
                            allocatedSpotIds.add(spot.getId());
                            lastException = null;
                            break;
                        } catch (ObjectOptimisticLockingFailureException e) {
                            lastException = e; // retry
                        }
                    }
                    if (lastException != null) {
                        unrecoverableErrors.add(lastException);
                    }
                } catch (Exception e) {
                    unrecoverableErrors.add(e);
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // fire all threads simultaneously
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(unrecoverableErrors)
                .as("All threads should successfully allocate a spot after retrying")
                .isEmpty();

        assertThat(allocatedSpotIds)
                .as("Every thread must have been assigned a spot")
                .hasSize(threadCount);

        assertThat(allocatedSpotIds.stream().distinct().count())
                .as("Each allocated spot must be unique — @Version prevents double-assignment")
                .isEqualTo(threadCount);
    }

    // Allocate → release cycle

    @Test
    void allocateAndRelease_spotBecomesAvailableAgain() {
        ParkingSpot spot = spotAllocationService.allocate(new Motorcycle("CYCLE-001"));
        assertThat(spot).isNotNull();

        spotAllocationService.release(spot);

        // Should be allocatable again without NoAvailableSpotException
        ParkingSpot reused = spotAllocationService.allocate(new Motorcycle("CYCLE-002"));
        assertThat(reused).isNotNull();
    }
}
