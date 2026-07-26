package com.airtribe.parking.strategy.fee;

import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.enums.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Flat-rate fee: a fixed amount per vehicle type regardless of parking duration.
 *
 * Default rates:
 *   MOTORCYCLE : ₹50
 *   CAR        : ₹100
 *   BUS        : ₹200
 */
@Component("flatRate")
public class FlatRateFeeStrategy implements IFeeStrategy {

    private static final Map<VehicleType, BigDecimal> FLAT_RATES = new EnumMap<>(VehicleType.class);

    static {
        FLAT_RATES.put(VehicleType.MOTORCYCLE, new BigDecimal("50.00"));
        FLAT_RATES.put(VehicleType.CAR,        new BigDecimal("100.00"));
        FLAT_RATES.put(VehicleType.BUS,        new BigDecimal("200.00"));
    }

    @Override
    public BigDecimal calculateFee(Ticket ticket) {
        return FLAT_RATES.getOrDefault(ticket.getVehicle().getVehicleType(), BigDecimal.ZERO);
    }
}
