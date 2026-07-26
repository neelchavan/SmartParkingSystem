package com.airtribe.parking.entity;

import com.airtribe.parking.enums.SpotSize;
import com.airtribe.parking.enums.SpotStatus;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "parking_spot")
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotSize spotSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotStatus status = SpotStatus.FREE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Version
    private int version;

    public ParkingSpot() {}

    public ParkingSpot(String spotNumber, SpotSize spotSize, Floor floor) {
        this.spotNumber = spotNumber;
        this.spotSize = spotSize;
        this.floor = floor;
    }

    public UUID getId() { return id; }

    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }

    public SpotSize getSpotSize() { return spotSize; }
    public void setSpotSize(SpotSize spotSize) { this.spotSize = spotSize; }

    public SpotStatus getStatus() { return status; }
    public void setStatus(SpotStatus status) { this.status = status; }

    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }

    public int getVersion() { return version; }
}
