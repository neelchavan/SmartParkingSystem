package com.airtribe.parking.entity;

import com.airtribe.parking.enums.VehicleType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "motorcycle")
public class Motorcycle extends Vehicle {

    public Motorcycle() { super(); }

    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }
}
