package com.airtribe.parking.strategy.fee;

import com.airtribe.parking.entity.RateCard;
import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.util.TimeUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Slab-based hourly fee: first hour at a higher rate, subsequent hours at a lower rate.
 * Duration is rounded up to the nearest hour (minimum 1 hour).
 *
 * Default rates (from RateCard):
 *   MOTORCYCLE : ₹10 first hour, ₹5/hr after
 *   CAR        : ₹20 first hour, ₹10/hr after
 *   BUS        : ₹50 first hour, ₹30/hr after
 */
@Component("slabBased")
@Primary
public class SlabBasedFeeStrategy implements IFeeStrategy {

    private final RateCard rateCard = new RateCard();

    @Override
    public BigDecimal calculateFee(Ticket ticket) {
        VehicleType type = ticket.getVehicle().getVehicleType();
        long hours = TimeUtil.durationInHoursCeiling(ticket.getEntryTime(), ticket.getExitTime());

        BigDecimal firstHour = rateCard.getFirstHourRate(type);
        BigDecimal subseqRate = rateCard.getSubsequentHourRate(type);

        if (hours <= 1) {
            return firstHour;
        }
        BigDecimal additional = subseqRate.multiply(BigDecimal.valueOf(hours - 1));
        return firstHour.add(additional);
    }
}
