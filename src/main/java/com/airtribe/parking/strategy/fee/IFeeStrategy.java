package com.airtribe.parking.strategy.fee;

import com.airtribe.parking.entity.Ticket;

import java.math.BigDecimal;

public interface IFeeStrategy {
    BigDecimal calculateFee(Ticket ticket);
}
