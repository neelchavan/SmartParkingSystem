package com.airtribe.parking.repository;

import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByVehicleAndStatus(Vehicle vehicle, TicketStatus status);
    List<Ticket> findByStatus(TicketStatus status);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.vehicle JOIN FETCH t.spot s JOIN FETCH s.floor WHERE t.status = :status")
    List<Ticket> findByStatusWithDetails(@Param("status") TicketStatus status);
}

