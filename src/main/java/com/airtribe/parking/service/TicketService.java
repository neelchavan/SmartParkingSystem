package com.airtribe.parking.service;

import com.airtribe.parking.entity.ParkingSpot;
import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.TicketStatus;
import com.airtribe.parking.exception.InvalidTicketException;
import com.airtribe.parking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public Ticket createTicket(Vehicle vehicle, ParkingSpot spot) {
        Ticket ticket = new Ticket(vehicle, spot);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket closeTicket(UUID ticketId, BigDecimal fee) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new InvalidTicketException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new InvalidTicketException(
                    "Ticket " + ticketId + " is not active (status: " + ticket.getStatus() + ")");
        }

        ticket.setExitTime(LocalDateTime.now());
        ticket.setFee(fee);
        ticket.setStatus(TicketStatus.PAID);
        return ticketRepository.save(ticket);
    }

    public Ticket findById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new InvalidTicketException("Ticket not found: " + ticketId));
    }
}
