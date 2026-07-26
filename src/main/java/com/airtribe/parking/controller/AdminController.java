package com.airtribe.parking.controller;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.repository.FloorRepository;
import com.airtribe.parking.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * CLI controller for administrative tasks:
 * viewing real-time availability per floor, adding floors, and adding spots.
 */
@Component
public class AdminController {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    /** Prints a table of free/occupied/total counts per floor, broken down by spot size. */
    public void showAvailability() {
        List<Floor> floors = floorRepository.findAll();
        if (floors.isEmpty()) {
            System.out.println("  No floors configured yet.");
            return;
        }

        System.out.println();
        System.out.printf("  %-8s %-6s %-9s %-7s  %s%n",
                "Floor", "Free", "Occupied", "Total", "Free by size (S/M/L)");
        System.out.println("  " + "─".repeat(58));

        for (Floor floor : floors) {
            long free     = parkingSpotRepository.countByFloorAndStatus(floor, SpotStatus.FREE);
            long occupied = parkingSpotRepository.countByFloorAndStatus(floor, SpotStatus.OCCUPIED);
            long total    = free + occupied;
            long sF = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.SMALL,  SpotStatus.FREE);
            long mF = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.MEDIUM, SpotStatus.FREE);
            long lF = parkingSpotRepository.countByFloorAndSpotSizeAndStatus(floor, SpotSize.LARGE,  SpotStatus.FREE);

            System.out.printf("  Floor %-3d %-6d %-9d %-7d  S:%d  M:%d  L:%d%n",
                    floor.getFloorNumber(), free, occupied, total, sF, mF, lF);
        }
    }

    /** Prompts for a floor number and adds a new empty floor. */
    public void addFloor(Scanner scanner) {
        System.out.print("  Floor number: ");
        String input = scanner.nextLine().trim();
        int floorNumber;
        try {
            floorNumber = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("  ERROR: Floor number must be an integer.");
            return;
        }

        if (floorRepository.findByFloorNumber(floorNumber).isPresent()) {
            System.out.println("  ERROR: Floor " + floorNumber + " already exists.");
            return;
        }

        Floor floor = floorRepository.save(new Floor(floorNumber));
        System.out.printf("  Floor %d added (ID: %s).%n", floor.getFloorNumber(), floor.getId());
    }

    /** Prompts for floor number, spot number, and spot size, then persists the new spot. */
    public void addSpot(Scanner scanner) {
        System.out.print("  Floor number: ");
        int floorNumber;
        try {
            floorNumber = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  ERROR: Floor number must be an integer.");
            return;
        }

        Optional<Floor> floorOpt = floorRepository.findByFloorNumber(floorNumber);
        if (floorOpt.isEmpty()) {
            System.out.println("  ERROR: Floor " + floorNumber + " not found.");
            return;
        }
        Floor floor = floorOpt.get();

        System.out.print("  Spot number (e.g. F4-S14): ");
        String spotNumber = scanner.nextLine().trim().toUpperCase();
        if (spotNumber.isBlank()) {
            System.out.println("  ERROR: Spot number cannot be empty.");
            return;
        }

        System.out.println("  Spot size:");
        System.out.println("    1) SMALL  (Motorcycle)");
        System.out.println("    2) MEDIUM (Car)");
        System.out.println("    3) LARGE  (Bus)");
        System.out.print("  Choice: ");
        String choice = scanner.nextLine().trim();

        SpotSize size = switch (choice) {
            case "1" -> SpotSize.SMALL;
            case "2" -> SpotSize.MEDIUM;
            case "3" -> SpotSize.LARGE;
            default -> null;
        };

        if (size == null) {
            System.out.println("  ERROR: Invalid spot size choice.");
            return;
        }

        parkingSpotRepository.save(new ParkingSpot(spotNumber, size, floor));
        System.out.printf("  Spot %s (%s) added to Floor %d.%n", spotNumber, size, floorNumber);
    }
}
