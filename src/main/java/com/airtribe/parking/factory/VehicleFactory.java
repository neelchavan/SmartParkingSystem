package com.airtribe.parking.factory;

import com.airtribe.parking.entity.Bus;
import com.airtribe.parking.entity.Car;
import com.airtribe.parking.entity.Motorcycle;
import com.airtribe.parking.entity.Vehicle;
import com.airtribe.parking.enums.VehicleType;

public final class VehicleFactory {

    private VehicleFactory() {}

    public static Vehicle create(VehicleType type, String licensePlate) {
        return switch (type) {
            case MOTORCYCLE -> new Motorcycle(licensePlate);
            case CAR        -> new Car(licensePlate);
            case BUS        -> new Bus(licensePlate);
        };
    }
}
