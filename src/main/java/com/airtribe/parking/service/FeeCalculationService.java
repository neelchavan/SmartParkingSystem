package com.airtribe.parking.service;

import com.airtribe.parking.entity.Ticket;
import com.airtribe.parking.strategy.fee.IFeeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FeeCalculationService {

    @Autowired
    private IFeeStrategy feeStrategy;

    public BigDecimal calculate(Ticket ticket) {
        return feeStrategy.calculateFee(ticket);
    }
}
