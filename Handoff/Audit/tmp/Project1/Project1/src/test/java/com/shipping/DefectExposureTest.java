package com.shipping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DefectExposureTest {

    private final ShippingCalculator correctCalc = new ShippingCalculator(PackageDefect.NONE);

    @Test
    @DisplayName("Expose D1: Max weight limit reduced from 60 to 50")
    void exposeDefectD1() {
        // Weight = 55.0 is valid for NONE (<= 60), but rejected by D1 (<= 50)
        PackageShippingInput input = new PackageShippingInput(
                55.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        // With NONE: passes and calculates 55 * 2.00 * 1.0 = 110.00
        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("110.00"), resNone.getTotalCost());

        // With D1: wrongly throws IllegalArgumentException
        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D1);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(input));
    }

    @Test
    @DisplayName("Expose D2: Validation of weight <= 0 removed")
    void exposeDefectD2() {
        // Weight = -1.0 is invalid for NONE, but D2 allows it and calculates negative cost
        PackageShippingInput input = new PackageShippingInput(
                -1.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        // With NONE: throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(input));

        // With D2: does not throw, returns negative cost (-1 * 2 * 1 = -2.00)
        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D2);
        ShippingResult resD2 = defectCalc.calculate(input);
        assertEquals(new BigDecimal("-2.00"), resD2.getTotalCost());
    }

    @Test
    @DisplayName("Expose D3: Same-zone rate increased from $2 to $3 per lb")
    void exposeDefectD3() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("20.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D3);
        ShippingResult resD3 = defectCalc.calculate(input);
        // D3 computes 10 * 3.00 * 1.0 = 30.00 != 20.00
        assertNotEquals(resNone.getTotalCost(), resD3.getTotalCost());
        assertEquals(new BigDecimal("30.00"), resD3.getTotalCost());
    }

    @Test
    @DisplayName("Expose D4: Zone pair (2,3)/(3,2) looked up with invalid key (2,5), fallback to $2.00")
    void exposeDefectD4() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 2, 3, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("27.50"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D4);
        ShippingResult resD4 = defectCalc.calculate(input);
        // D4 falls back to 2.00/lb: 10 * 2.00 * 1.0 = 20.00 != 27.50
        assertNotEquals(resNone.getTotalCost(), resD4.getTotalCost());
        assertEquals(new BigDecimal("20.00"), resD4.getTotalCost());
    }

    @Test
    @DisplayName("Expose D5: Special handling of pair (1,4)/(4,1) removed, fallback to $2.00")
    void exposeDefectD5() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 4, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("37.50"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D5);
        ShippingResult resD5 = defectCalc.calculate(input);
        // D5 falls back to 2.00/lb: 10 * 2.00 * 1.0 = 20.00 != 37.50
        assertNotEquals(resNone.getTotalCost(), resD5.getTotalCost());
        assertEquals(new BigDecimal("20.00"), resD5.getTotalCost());
    }

    @Test
    @DisplayName("Expose D6: Priority Overnight multiplier increased from 1.2 to 2.2")
    void exposeDefectD6() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 3, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("24.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D6);
        ShippingResult resD6 = defectCalc.calculate(input);
        // D6 multiplier is 2.2: 10 * 2.00 * 2.2 = 44.00 != 24.00
        assertNotEquals(resNone.getTotalCost(), resD6.getTotalCost());
        assertEquals(new BigDecimal("44.00"), resD6.getTotalCost());
    }

    @Test
    @DisplayName("Expose D7: Multipliers of Same Day and Standard Overnight swapped")
    void exposeDefectD7() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 1, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("42.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D7);
        ShippingResult resD7 = defectCalc.calculate(input);
        // D7 multiplier for Same Day is 1.0 instead of 2.1: 10 * 2.00 * 1.0 = 20.00 != 42.00
        assertNotEquals(resNone.getTotalCost(), resD7.getTotalCost());
        assertEquals(new BigDecimal("20.00"), resD7.getTotalCost());
    }

    @Test
    @DisplayName("Expose D8: Residential surcharge increased from $10 to $20")
    void exposeDefectD8() {
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 4, 0, 1, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("30.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D8);
        ShippingResult resD8 = defectCalc.calculate(input);
        // D8 residential surcharge is 20: 20.00 + 20.00 = 40.00 != 30.00
        assertNotEquals(resNone.getTotalCost(), resD8.getTotalCost());
        assertEquals(new BigDecimal("40.00"), resD8.getTotalCost());
    }

    @Test
    @DisplayName("Expose D9: Delivery location type interpreted inversely (Business <-> Residence)")
    void exposeDefectD9() {
        // Business location (0) should have $0 surcharge
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("20.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D9);
        ShippingResult resD9 = defectCalc.calculate(input);
        // D9 treats Business 0 as Residence 1 -> surcharge $10.00: 20 + 10 = 30.00 != 20.00
        assertNotEquals(resNone.getTotalCost(), resD9.getTotalCost());
        assertEquals(new BigDecimal("30.00"), resD9.getTotalCost());
    }

    @Test
    @DisplayName("Expose D10: Fragile condition inverted (surcharge applied when fragile = 0)")
    void exposeDefectD10() {
        // fragile = 0 should have $0 surcharge
        PackageShippingInput input = new PackageShippingInput(
                10.0, 1, 1, 4, 0, 0, 0, 0, 0, 0);

        ShippingResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("20.00"), resNone.getTotalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(PackageDefect.D10);
        ShippingResult resD10 = defectCalc.calculate(input);
        // D10 applies fragile surcharge when fragile == 0: 20 + 25 = 45.00 != 20.00
        assertNotEquals(resNone.getTotalCost(), resD10.getTotalCost());
        assertEquals(new BigDecimal("45.00"), resD10.getTotalCost());
    }
}
