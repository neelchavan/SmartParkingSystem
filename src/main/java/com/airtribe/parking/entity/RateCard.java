package com.airtribe.parking.entity;

import com.airtribe.parking.enums.VehicleType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class RateCard {

    private final Map<VehicleType, BigDecimal> firstHourRates = new EnumMap<>(VehicleType.class);
    private final Map<VehicleType, BigDecimal> subsequentHourRates = new EnumMap<>(VehicleType.class);

    public RateCard() {
        // Motorcycle: ₹10 first hour, ₹5/hr after
        firstHourRates.put(VehicleType.MOTORCYCLE, new BigDecimal("10.00"));
        subsequentHourRates.put(VehicleType.MOTORCYCLE, new BigDecimal("5.00"));
        // Car: ₹20 first hour, ₹10/hr after
        firstHourRates.put(VehicleType.CAR, new BigDecimal("20.00"));
        subsequentHourRates.put(VehicleType.CAR, new BigDecimal("10.00"));
        // Bus: ₹50 first hour, ₹30/hr after
        firstHourRates.put(VehicleType.BUS, new BigDecimal("50.00"));
        subsequentHourRates.put(VehicleType.BUS, new BigDecimal("30.00"));
    }

    public BigDecimal getFirstHourRate(VehicleType type) {
        return firstHourRates.get(type);
    }

    public BigDecimal getSubsequentHourRate(VehicleType type) {
        return subsequentHourRates.get(type);
    }
}
