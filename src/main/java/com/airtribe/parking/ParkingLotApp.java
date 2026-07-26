package com.airtribe.parking;

import com.airtribe.parking.controller.AdminController;
import com.airtribe.parking.controller.EntryGateController;
import com.airtribe.parking.controller.ExitGateController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@SpringBootApplication
public class ParkingLotApp {

    public static void main(String[] args) {
        SpringApplication.run(ParkingLotApp.class, args);
    }

    // -----------------------------------------------------------------------
    // Interactive console menu — runs after ParkingLotConfig seeds the lot (Order 1)
    // -----------------------------------------------------------------------
    @Component
    @Order(2)
    public static class ConsoleMenu implements CommandLineRunner {

        @Autowired private EntryGateController entryGateController;
        @Autowired private ExitGateController  exitGateController;
        @Autowired private AdminController     adminController;

        @Override
        public void run(String... args) {
            Scanner scanner = new Scanner(System.in);
            printBanner();

            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("  Your choice: ");
                String choice = scanner.nextLine().trim();
                System.out.println();

                switch (choice) {
                    case "1" -> {
                        printSection("VEHICLE CHECK-IN");
                        entryGateController.handle(scanner);
                    }
                    case "2" -> {
                        printSection("VEHICLE CHECK-OUT");
                        exitGateController.handle(scanner);
                    }
                    case "3" -> {
                        printSection("AVAILABILITY — ALL FLOORS");
                        adminController.showAvailability();
                    }
                    case "4" -> {
                        printSection("ADD NEW FLOOR");
                        adminController.addFloor(scanner);
                    }
                    case "5" -> {
                        printSection("ADD PARKING SPOT");
                        adminController.addSpot(scanner);
                    }
                    case "0" -> {
                        System.out.println("  Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("  Invalid option. Please choose a number from the menu.");
                }
                System.out.println();
            }
            scanner.close();
        }

        private void printBanner() {
            String line = "═".repeat(52);
            System.out.println("\n╔" + line + "╗");
            System.out.printf( "║  %-50s║%n", "SMART PARKING LOT — MANAGEMENT CONSOLE");
            System.out.println("╚" + line + "╝\n");
        }

        private void printMenu() {
            System.out.println("  ┌─── MAIN MENU ──────────────────────────────────┐");
            System.out.println("  │  1) Check-In   (Vehicle Entry)                 │");
            System.out.println("  │  2) Check-Out  (Vehicle Exit + Fee)            │");
            System.out.println("  │  3) View Availability  (All Floors)            │");
            System.out.println("  │  4) Add New Floor              [Admin]         │");
            System.out.println("  │  5) Add Parking Spot           [Admin]         │");
            System.out.println("  │  0) Exit                                       │");
            System.out.println("  └────────────────────────────────────────────────┘");
        }

        private void printSection(String title) {
            int padLen = Math.max(0, 48 - title.length());
            System.out.println("── " + title + " " + "─".repeat(padLen));
        }
    }
}
