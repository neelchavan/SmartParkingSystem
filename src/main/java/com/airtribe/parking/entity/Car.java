package com.airtribe.parking.entity;

import com.airtribe.parking.enums.VehicleType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "car")
public class Car extends Vehicle {

    public Car() { super(); }

    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
}
