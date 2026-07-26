package com.airtribe.parking.controller;

import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.exception.DuplicateVehicleException;
import com.airtribe.parking.exception.NoAvailableSpotException;
import com.airtribe.parking.service.ParkingLotService;
import com.airtribe.parking.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * CLI controller for the entry gate.
 * Collects license plate and vehicle type, then delegates to {@link ParkingLotService#checkIn}.
 */
@Component
public class EntryGateController {

    @Autowired
    private ParkingLotService parkingLotService;

    public void handle(Scanner scanner) {
        System.out.print("  License plate: ");
        String plate = scanner.nextLine().trim().toUpperCase();
        if (plate.isBlank()) {
            System.out.println("  ERROR: License plate cannot be empty.");
            return;
        }

        System.out.println("  Vehicle type:");
        System.out.println("    1) MOTORCYCLE");
        System.out.println("    2) CAR");
        System.out.println("    3) BUS");
        System.out.print("  Choice: ");
        String choice = scanner.nextLine().trim();

        VehicleType type = switch (choice) {
            case "1" -> VehicleType.MOTORCYCLE;
            case "2" -> VehicleType.CAR;
            case "3" -> VehicleType.BUS;
            default -> null;
        };

        if (type == null) {
            System.out.println("  ERROR: Invalid vehicle type. Aborting check-in.");
            return;
        }

        try {
            Ticket ticket = parkingLotService.checkIn(plate, type);
            System.out.println();
            System.out.println("  CHECK-IN SUCCESSFUL");
            System.out.printf("  Ticket ID : %s%n", ticket.getId());
            System.out.printf("  Vehicle   : %s (%s)%n",
                    ticket.getVehicle().getLicensePlate(),
                    ticket.getVehicle().getVehicleType());
            System.out.printf("  Spot      : %s (%s, Floor %d)%n",
                    ticket.getSpot().getSpotNumber(),
                    ticket.getSpot().getSpotSize(),
                    ticket.getSpot().getFloor().getFloorNumber());
            System.out.printf("  Entry     : %s%n", TimeUtil.format(ticket.getEntryTime()));
            System.out.println("  Keep this ticket ID for exit.");
        } catch (DuplicateVehicleException | NoAvailableSpotException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }
}
