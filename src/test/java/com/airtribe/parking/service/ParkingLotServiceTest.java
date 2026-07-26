package com.airtribe.parking.service;

import com.airtribe.parking.ParkingLotApp;
import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.TicketStatus;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.exception.DuplicateVehicleException;
import com.airtribe.parking.exception.InvalidTicketException;
import com.airtribe.parking.service.ParkingLotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Full integration tests for ParkingLotService against an H2 in-memory database.
 *
 * @Transactional on the class causes Spring Test to roll back every test method
 * automatically, keeping the seeded lot state intact across tests.
 *
 * ConsoleMenu (the interactive CLI runner) is mocked so it does not block on stdin.
 * ParkingLotConfig (the data seeder) runs normally — it provides the floors and spots
 * used by the allocation strategy.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ParkingLotServiceTest {

    // Suppress the interactive console so the test context starts cleanly
    @MockBean
    ParkingLotApp.ConsoleMenu consoleMenu;

    @Autowired
    ParkingLotService parkingLotService;

    // Check-in

    @Test
    void checkIn_createsActiveTicketWithCorrectVehicle() {
        Ticket ticket = parkingLotService.checkIn("KA-01-AB-1234", VehicleType.CAR);

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
        assertThat(ticket.getEntryTime()).isNotNull();
        assertThat(ticket.getExitTime()).isNull();
        assertThat(ticket.getFee()).isNull();
        assertThat(ticket.getVehicle().getLicensePlate()).isEqualTo("KA-01-AB-1234");
        assertThat(ticket.getVehicle().getVehicleType()).isEqualTo(VehicleType.CAR);
    }

    @Test
    void checkIn_assignsCorrectSpotSize_forMotorcycle() {
        Ticket ticket = parkingLotService.checkIn("KA-02-MC-0001", VehicleType.MOTORCYCLE);

        assertThat(ticket.getSpot()).isNotNull();
        assertThat(ticket.getSpot().getSpotSize()).isEqualTo(SpotSize.SMALL);
    }

    @Test
    void checkIn_assignsCorrectSpotSize_forCar() {
        Ticket ticket = parkingLotService.checkIn("KA-02-CC-0002", VehicleType.CAR);

        assertThat(ticket.getSpot().getSpotSize()).isEqualTo(SpotSize.MEDIUM);
    }

    @Test
    void checkIn_assignsCorrectSpotSize_forBus() {
        Ticket ticket = parkingLotService.checkIn("KA-02-BU-0003", VehicleType.BUS);

        assertThat(ticket.getSpot().getSpotSize()).isEqualTo(SpotSize.LARGE);
    }

    @Test
    void checkIn_sameLicensePlateTwice_throwsDuplicateVehicleException() {
        parkingLotService.checkIn("MH-01-DUP-9999", VehicleType.CAR);

        assertThatThrownBy(() -> parkingLotService.checkIn("MH-01-DUP-9999", VehicleType.CAR))
                .isInstanceOf(DuplicateVehicleException.class)
                .hasMessageContaining("MH-01-DUP-9999");
    }

    // Check-out

    @Test
    void checkOut_closesTicketWithPaidStatusAndFee() {
        Ticket checkedIn = parkingLotService.checkIn("DL-01-OUT-1111", VehicleType.MOTORCYCLE);

        Ticket closed = parkingLotService.checkOut(checkedIn.getId());

        assertThat(closed.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(closed.getFee()).isNotNull().isPositive();
        assertThat(closed.getExitTime()).isNotNull();
    }

    @Test
    void checkOut_releasesSpot_soItCanBeReallocated() {
        Ticket first = parkingLotService.checkIn("GJ-01-R-1111", VehicleType.CAR);
        UUID releasedSpotId = first.getSpot().getId();

        parkingLotService.checkOut(first.getId());

        // After release, the same spot (or another free one) should be allocatable again
        Ticket second = parkingLotService.checkIn("GJ-01-R-2222", VehicleType.CAR);
        assertThat(second.getStatus()).isEqualTo(TicketStatus.ACTIVE);
    }

    @Test
    void checkOut_unknownTicketId_throwsInvalidTicketException() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> parkingLotService.checkOut(randomId))
                .isInstanceOf(InvalidTicketException.class);
    }

    @Test
    void checkOut_alreadyClosedTicket_throwsInvalidTicketException() {
        Ticket ticket = parkingLotService.checkIn("TN-01-PAID-5555", VehicleType.BUS);
        parkingLotService.checkOut(ticket.getId());

        assertThatThrownBy(() -> parkingLotService.checkOut(ticket.getId()))
                .isInstanceOf(InvalidTicketException.class)
                .hasMessageContaining("not active");
    }

    // Fee sanity checks via SlabBasedFeeStrategy (primary bean)

    @Test
    void checkOut_minimumFee_isAtLeastFirstHourRate() {
        // Even a very short stay should be charged for at least 1 hour
        Ticket ticket = parkingLotService.checkIn("HR-01-FEE-0001", VehicleType.CAR);

        Ticket closed = parkingLotService.checkOut(ticket.getId());

        // Car first-hour rate is ₹20
        assertThat(closed.getFee()).isGreaterThanOrEqualTo(new java.math.BigDecimal("20.00"));
    }
}
