package com.airtribe.parking.controller;

import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.exception.InvalidTicketException;
import com.airtribe.parking.service.ParkingLotService;
import com.airtribe.parking.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.UUID;

/**
 * CLI controller for the exit gate.
 * Collects the ticket ID, calls {@link ParkingLotService#checkOut}, and prints the fee summary.
 */
@Component
public class ExitGateController {

    @Autowired
    private ParkingLotService parkingLotService;

    public void handle(Scanner scanner) {
        System.out.print("  Ticket ID: ");
        String raw = scanner.nextLine().trim();

        UUID ticketId;
        try {
            ticketId = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            System.out.println("  ERROR: Invalid ticket ID format. Expected UUID (e.g. 550e8400-e29b-41d4-a716-446655440000).");
            return;
        }

        try {
            Ticket ticket = parkingLotService.checkOut(ticketId);
            long hours = TimeUtil.durationInHoursCeiling(ticket.getEntryTime(), ticket.getExitTime());
            System.out.println();
            System.out.println("  CHECK-OUT SUCCESSFUL");
            System.out.printf("  Ticket ID : %s%n", ticket.getId());
            System.out.printf("  Vehicle   : %s (%s)%n",
                    ticket.getVehicle().getLicensePlate(),
                    ticket.getVehicle().getVehicleType());
            System.out.printf("  Spot      : %s%n", ticket.getSpot().getSpotNumber());
            System.out.printf("  Entry     : %s%n", TimeUtil.format(ticket.getEntryTime()));
            System.out.printf("  Exit      : %s%n", TimeUtil.format(ticket.getExitTime()));
            System.out.printf("  Duration  : %d hour(s)%n", hours);
            System.out.printf("  Fee       : Rs %.2f%n", ticket.getFee());
            System.out.printf("  Status    : %s%n", ticket.getStatus());
        } catch (InvalidTicketException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }
}
