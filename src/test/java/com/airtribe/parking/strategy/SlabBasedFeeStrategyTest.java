package com.airtribe.parking.strategy;

import com.airtribe.parking.entity.*;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.strategy.fee.SlabBasedFeeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * unit tests for SlabBasedFeeStrategyy
 *
 * Rate card under test:
 *   MOTORCYCLE : ₹10 first hour, ₹5/hr after
 *   CAR        : ₹20 first hour, ₹10/hr after
 *   BUS        : ₹50 first hour, ₹30/hr after
 *
 * Duration is always rounded up to the nearest whole hour (minimum 1 hour).
 */
class SlabBasedFeeStrategyTest {

    private SlabBasedFeeStrategy strategy;
    private static final LocalDateTime BASE = LocalDateTime.of(2024, 1, 1, 10, 0);

    @BeforeEach
    void setUp() {
        strategy = new SlabBasedFeeStrategy();
    }

    private Ticket ticketFor(VehicleType type, long minutes) {
        Vehicle vehicle = switch (type) {
            case MOTORCYCLE -> new Motorcycle("TEST-M");
            case CAR        -> new Car("TEST-C");
            case BUS        -> new Bus("TEST-B");
        };
        Ticket ticket = new Ticket();
        ticket.setVehicle(vehicle);
        ticket.setEntryTime(BASE);
        ticket.setExitTime(BASE.plusMinutes(minutes));
        return ticket;
    }

    // Motorcycle

    @Test
    void motorcycle_exactlyOneHour_chargesFirstHourRate() {
        assertThat(strategy.calculateFee(ticketFor(VehicleType.MOTORCYCLE, 60)))
                .isEqualByComparingTo("10.00");
    }

    @Test
    void motorcycle_partialHour_roundsUpToOneHour() {
        // 30 min → ceiling = 1 hour → ₹10
        assertThat(strategy.calculateFee(ticketFor(VehicleType.MOTORCYCLE, 30)))
                .isEqualByComparingTo("10.00");
    }

    @Test
    void motorcycle_oneMinute_stillChargedOneHour() {
        // minimum charge is 1 hour
        assertThat(strategy.calculateFee(ticketFor(VehicleType.MOTORCYCLE, 1)))
                .isEqualByComparingTo("10.00");
    }

    @Test
    void motorcycle_twoHours_firstPlusOneSubsequent() {
        // 10 + 5*1 = 15
        assertThat(strategy.calculateFee(ticketFor(VehicleType.MOTORCYCLE, 120)))
                .isEqualByComparingTo("15.00");
    }

    @Test
    void motorcycle_threeHoursAndOneMinute_roundsUpToFourHours() {
        // 3h 1m → 4 hours → 10 + 5*3 = 25
        assertThat(strategy.calculateFee(ticketFor(VehicleType.MOTORCYCLE, 181)))
                .isEqualByComparingTo("25.00");
    }

    // Car

    @Test
    void car_exactlyOneHour_chargesFirstHourRate() {
        assertThat(strategy.calculateFee(ticketFor(VehicleType.CAR, 60)))
                .isEqualByComparingTo("20.00");
    }

    @Test
    void car_partialHour_roundsUpToOneHour() {
        // 45 min → 1 hour → ₹20
        assertThat(strategy.calculateFee(ticketFor(VehicleType.CAR, 45)))
                .isEqualByComparingTo("20.00");
    }

    @Test
    void car_threeHours_firstPlusTwoSubsequent() {
        // 20 + 10*2 = 40
        assertThat(strategy.calculateFee(ticketFor(VehicleType.CAR, 180)))
                .isEqualByComparingTo("40.00");
    }

    @Test
    void car_oneHourThirtyMinutes_roundsUpToTwoHours() {
        // 1h 30m → 2 hours → 20 + 10 = 30
        assertThat(strategy.calculateFee(ticketFor(VehicleType.CAR, 90)))
                .isEqualByComparingTo("30.00");
    }

    // Bus

    @Test
    void bus_exactlyOneHour_chargesFirstHourRate() {
        assertThat(strategy.calculateFee(ticketFor(VehicleType.BUS, 60)))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void bus_partialHour_roundsUpToOneHour() {
        assertThat(strategy.calculateFee(ticketFor(VehicleType.BUS, 20)))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void bus_fourHours_firstPlusThreeSubsequent() {
        // 50 + 30*3 = 140
        assertThat(strategy.calculateFee(ticketFor(VehicleType.BUS, 240)))
                .isEqualByComparingTo("140.00");
    }

    @Test
    void bus_twoHoursOneMinute_roundsUpToThreeHours() {
        // 2h 1m → 3 hours → 50 + 30*2 = 110
        assertThat(strategy.calculateFee(ticketFor(VehicleType.BUS, 121)))
                .isEqualByComparingTo("110.00");
    }
}
