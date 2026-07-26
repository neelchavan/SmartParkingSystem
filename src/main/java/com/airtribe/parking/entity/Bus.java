package com.airtribe.parking.entity;

import com.airtribe.parking.enums.VehicleType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "bus")
public class Bus extends Vehicle {

    public Bus() { super(); }

    public Bus(String licensePlate) {
        super(licensePlate, VehicleType.BUS);
    }
}
