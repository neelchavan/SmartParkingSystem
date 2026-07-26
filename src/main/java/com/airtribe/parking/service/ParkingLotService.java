package com.airtribe.parking.service;

import com.airtribe.parking.entity.Floor;
import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.SpotStatus;
import com.airtribe.parking.enums.TicketStatus;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.exception.DuplicateVehicleException;
import com.airtribe.parking.exception.InvalidTicketException;
import com.airtribe.parking.factory.VehicleFactory;
import com.airtribe.parking.observer.IAvailabilityObserver;
import com.airtribe.parking.repository.FloorRepository;
import com.airtribe.parking.repository.TicketRepository;
import com.airtribe.parking.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Façade that orchestrates the full check-in / check-out flow.
 * Notifies all registered {@link IAvailabilityObserver}s (Spring injects every
 * component that implements the interface) after every spot state change.
 */
@Service
public class ParkingLotService {

    @Autowired private SpotAllocationService spotAllocationService;
    @Autowired private TicketService          ticketService;
    @Autowired private FeeCalculationService  feeCalculationService;
    @Autowired private VehicleRepository      vehicleRepository;
    @Autowired private TicketRepository       ticketRepository;
    @Autowired private FloorRepository        floorRepository;
    @Autowired private List<IAvailabilityObserver> observers;

    // -------------------------------------------------------------------------
    // Check-in
    // -------------------------------------------------------------------------

    @Transactional
    public Ticket checkIn(String licensePlate, VehicleType vehicleType) {
        // Guard: vehicle already parked?
        Optional<Vehicle> existing = vehicleRepository.findByLicensePlate(licensePlate);
        if (existing.isPresent()) {
            ticketRepository.findByVehicleAndStatus(existing.get(), TicketStatus.ACTIVE)
                    .ifPresent(t -> { throw new DuplicateVehicleException(
                            "Vehicle " + licensePlate + " is already parked (ticket: " + t.getId() + ")"); });
        }

        Vehicle vehicle = existing.orElseGet(() ->
                vehicleRepository.save(VehicleFactory.create(vehicleType, licensePlate)));

        ParkingSpot spot  = spotAllocationService.allocate(vehicle);
        Ticket      ticket = ticketService.createTicket(vehicle, spot);

        Floor floor = loadFloor(spot);
        notifyObservers(floor, spot, SpotStatus.OCCUPIED);
        return ticket;
    }

    // -------------------------------------------------------------------------
    // Check-out
    // -------------------------------------------------------------------------

    @Transactional
    public Ticket checkOut(UUID ticketId) {
        Ticket ticket = ticketService.findById(ticketId);

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new InvalidTicketException(
                    "Ticket " + ticketId + " is not active (status: " + ticket.getStatus() + ")");
        }

        // Temporarily set exit time so fee strategy can compute duration
        ticket.setExitTime(LocalDateTime.now());
        BigDecimal fee = feeCalculationService.calculate(ticket);

        Ticket closed = ticketService.closeTicket(ticketId, fee);

        ParkingSpot spot = closed.getSpot();
        spotAllocationService.release(spot);

        Floor floor = loadFloor(spot);
        notifyObservers(floor, spot, SpotStatus.FREE);
        return closed;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Floor loadFloor(ParkingSpot spot) {
        return floorRepository.findById(spot.getFloor().getId())
                .orElseThrow(() -> new IllegalStateException("Floor not found for spot " + spot.getSpotNumber()));
    }

    private void notifyObservers(Floor floor, ParkingSpot spot, SpotStatus newStatus) {
        observers.forEach(obs -> obs.onSpotUpdated(floor, spot, newStatus));
    }
}
