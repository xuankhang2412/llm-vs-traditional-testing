package com.shipping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ShippingCalculatorTest {

    private ShippingCalculator calculator;

    @BeforeEach
    void setUp() {
        // Defect NONE baseline calculator
        calculator = new ShippingCalculator(PackageDefect.NONE);
    }

    @Test
    @DisplayName("Test 1: Normal standard shipment in same zone (1->1)")
    void testSameZoneStandardShipment() {
        // Spec Step 1: baseRate(1,1) = $2.00/lb. weight = 10.0 -> baseCost = 2.00 * 10.0 = $20.00
        // Spec Step 2: deliveryOption = 4 (Standard Overnight) -> multiplier = 1.0 -> multipliedCost = 20.00 * 1.0 = $20.00
        // Spec Step 3: all surcharges = $0.00 (signature=0, business=0, sat=0, hold=0, fragile=0, dangerous=0)
        // Spec Step 4: total = 20.00 + 0 = 20.00
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("20.00"), result.getTotalCost());
    }

    @Test
    @DisplayName("Test 2: Boundary weight = 60.0 lbs with all surcharges active")
    void testMaxWeightAllSurcharges() {
        // Spec Step 1: baseRate(1,4) = $3.75/lb. weight = 60.0 -> baseCost = 3.75 * 60.0 = $225.00
        // Spec Step 2: deliveryOption = 3 (Priority Overnight) -> multiplier = 1.2 -> multipliedCost = 225.00 * 1.2 = $270.00
        // Spec Step 3: surcharges:
        //   - Direct signature (2): +$10.00
        //   - Residence (1): +$10.00
        //   - Saturday (1): +$20.00
        //   - Hold at location (1): +$15.00
        //   - Fragile (1): +$25.00
        //   - Dangerous (1): +$50.00
        //   Total surcharges = 10 + 10 + 20 + 15 + 25 + 50 = $130.00
        // Spec Step 4: total = 270.00 + 130.00 = 400.00
        PackageShippingInput input = new PackageShippingInput(
                60.0, 1, 4, 3, 2, 1, 1, 1, 1, 1);

        ShippingResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("400.00"), result.getTotalCost());
    }

    @Test
    @DisplayName("Test 3: Boundary weight = 60.01 lbs exceeds maximum limit")
    void testWeightOverMaxBoundary() {
        // Spec: weight must be <= 60 lbs. 60.01 exceeds maximum limit.
        PackageShippingInput input = new PackageShippingInput(
                60.01, 1, 4, 3, 2, 1, 1, 1, 1, 1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
        assertTrue(ex.getMessage().contains("exceeds maximum limit"));
    }

    @Test
    @DisplayName("Test 4: Boundary weight = 0.0 lbs is rejected")
    void testWeightZeroBoundary() {
        // Spec: weight must be > 0. 0.0 is not allowed.
        PackageShippingInput input = new PackageShippingInput(
                0.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
        assertTrue(ex.getMessage().contains("greater than 0"));
    }

    @Test
    @DisplayName("Test 5: Negative weight = -5.0 lbs is rejected")
    void testNegativeWeight() {
        // Spec: weight must be > 0. -5.0 is rejected.
        PackageShippingInput input = new PackageShippingInput(
                -5.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
        assertTrue(ex.getMessage().contains("greater than 0"));
    }

    @Test
    @DisplayName("Test 6: Zone pair (2, 3), Same Day, indirect signature, residential")
    void testZonePair23SameDay() {
        // Spec Step 1: baseRate(2,3) = $2.75/lb. weight = 20.0 -> baseCost = 2.75 * 20.0 = $55.00
        // Spec Step 2: deliveryOption = 1 (Same Day) -> multiplier = 2.1 -> multipliedCost = 55.00 * 2.1 = $115.50
        // Spec Step 3: surcharges:
        //   - Indirect signature (1): +$3.00
        //   - Residence (1): +$10.00
        //   Total surcharges = 3 + 10 = $13.00
        // Spec Step 4: total = 115.50 + 13.00 = 128.50
        PackageShippingInput input = new PackageShippingInput(
                20.0, 2, 3, 1, 1, 1, 0, 0, 0, 0);

        ShippingResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("128.50"), result.getTotalCost());
    }

    @Test
    @DisplayName("Test 7: Zone pair (3, 4), 2-day delivery, hold at location")
    void testZonePair34TwoDayHold() {
        // Spec Step 1: baseRate(3,4) = $2.30/lb. weight = 5.5 -> baseCost = 2.30 * 5.5 = $12.65
        // Spec Step 2: deliveryOption = 6 (2-day) -> multiplier = 0.8 -> multipliedCost = 12.65 * 0.8 = $10.12
        // Spec Step 3: hold at location (1): +$15.00
        // Spec Step 4: total = 10.12 + 15.00 = 25.12
        PackageShippingInput input = new PackageShippingInput(
                5.5, 3, 4, 6, 0, 0, 0, 1, 0, 0);

        ShippingResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("25.12"), result.getTotalCost());
    }

    @Test
    @DisplayName("Test 8: Invalid origin zone 5 throws exception")
    void testInvalidOriginZone() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 5, 1, 4, 0, 0, 0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
    }

    @Test
    @DisplayName("Test 9: Invalid delivery option 8 throws exception")
    void testInvalidDeliveryOption() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 2, 8, 0, 0, 0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
    }
}
