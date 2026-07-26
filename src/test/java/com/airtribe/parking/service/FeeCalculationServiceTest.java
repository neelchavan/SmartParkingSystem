package com.airtribe.parking.service;

import com.airtribe.parking.entity.*;
import com.airtribe.parking.enums.VehicleType;
import com.airtribe.parking.strategy.fee.FlatRateFeeStrategy;
import com.airtribe.parking.strategy.fee.IFeeStrategy;
import com.airtribe.parking.strategy.fee.SlabBasedFeeStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeeCalculationService using Mockito — no Spring context.
 * Also exercises the concrete strategy implementations directly.
 */
@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {

    @Mock
    private IFeeStrategy feeStrategy;

    @InjectMocks
    private FeeCalculationService feeCalculationService;

    // FeeCalculationService delegation

    @Test
    void calculate_delegatesToInjectedStrategy() {
        Ticket ticket = new Ticket();
        BigDecimal expected = new BigDecimal("45.00");
        when(feeStrategy.calculateFee(ticket)).thenReturn(expected);

        BigDecimal result = feeCalculationService.calculate(ticket);

        assertThat(result).isEqualByComparingTo(expected);
        verify(feeStrategy, times(1)).calculateFee(ticket);
    }

    @Test
    void calculate_returnsExactlyWhatStrategyReturns() {
        Ticket ticket = new Ticket();
        when(feeStrategy.calculateFee(ticket)).thenReturn(BigDecimal.ZERO);

        assertThat(feeCalculationService.calculate(ticket)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // FlatRateFeeStrategy — fixed amount regardless of duration

    @Test
    void flatRate_motorcycle_alwaysReturns50() {
        FlatRateFeeStrategy flat = new FlatRateFeeStrategy();
        assertThat(flat.calculateFee(ticketWithType(new Motorcycle("FL-M"), 1)))
                .isEqualByComparingTo("50.00");
        // duration doesn't matter for flat rate
        assertThat(flat.calculateFee(ticketWithType(new Motorcycle("FL-M2"), 10)))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void flatRate_car_alwaysReturns100() {
        FlatRateFeeStrategy flat = new FlatRateFeeStrategy();
        assertThat(flat.calculateFee(ticketWithType(new Car("FL-C"), 5)))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void flatRate_bus_alwaysReturns200() {
        FlatRateFeeStrategy flat = new FlatRateFeeStrategy();
        assertThat(flat.calculateFee(ticketWithType(new Bus("FL-B"), 3)))
                .isEqualByComparingTo("200.00");
    }

    // SlabBasedFeeStrategy — differ by vehicle type for same duration

    @Test
    void slabBased_twoHours_differsByVehicleType() {
        SlabBasedFeeStrategy slab = new SlabBasedFeeStrategy();
        LocalDateTime entry = LocalDateTime.of(2024, 6, 1, 9, 0);
        LocalDateTime exit  = entry.plusHours(2);

        assertThat(slab.calculateFee(ticketAt(new Motorcycle("SL-M"), entry, exit)))
                .isEqualByComparingTo("15.00");  // 10 + 5
        assertThat(slab.calculateFee(ticketAt(new Car("SL-C"), entry, exit)))
                .isEqualByComparingTo("30.00");  // 20 + 10
        assertThat(slab.calculateFee(ticketAt(new Bus("SL-B"), entry, exit)))
                .isEqualByComparingTo("80.00");  // 50 + 30
    }

    // Helpers

    private Ticket ticketWithType(Vehicle vehicle, long durationHours) {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 8, 0);
        return ticketAt(vehicle, now, now.plusHours(durationHours));
    }

    private Ticket ticketAt(Vehicle vehicle, LocalDateTime entry, LocalDateTime exit) {
        Ticket t = new Ticket();
        t.setVehicle(vehicle);
        t.setEntryTime(entry);
        t.setExitTime(exit);
        return t;
    }
}
