package com.airtribe.parking.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "floor")
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private int floorNumber;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParkingSpot> spots = new ArrayList<>();

    public Floor() {}

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public UUID getId() { return id; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public List<ParkingSpot> getSpots() { return spots; }
    public void setSpots(List<ParkingSpot> spots) { this.spots = spots; }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
        spot.setFloor(this);
    }
}
