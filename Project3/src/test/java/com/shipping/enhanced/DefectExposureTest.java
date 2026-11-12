package com.shipping.enhanced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Defect Exposure Tests for Enhanced Package Shipping.
 * Every test passes under Defect.NONE and exposes the named defect Dk.
 */
public class DefectExposureTest {

    private final ShippingCalculator correctCalc = new ShippingCalculator(Defect.NONE);

    @Test
    @DisplayName("Expose D1: Zone 3-4 rate 2.30 -> 2.50")
    void exposeD1() {
        ShippingInput in = new ShippingInput(10.0, 3, 4, 4, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("26.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D1);
        ShippingResult resD1 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("28.00"), resD1.totalCost());
        assertNotEquals(resNone.totalCost(), resD1.totalCost());
    }

    @Test
    @DisplayName("Expose D2: Priority Overnight multiplier 1.2 -> 1.5")
    void exposeD2() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 3, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("33.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D2);
        ShippingResult resD2 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("40.50"), resD2.totalCost());
        assertNotEquals(resNone.totalCost(), resD2.totalCost());
    }

    @Test
    @DisplayName("Expose D3: N1 no longer applies to Same Day (option 1)")
    void exposeD3() {
        ShippingInput in = new ShippingInput(10.0, 2, 2, 1, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("40.50"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D3);
        ShippingResult resD3 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("45.00"), resD3.totalCost());
        assertNotEquals(resNone.totalCost(), resD3.totalCost());
    }

    @Test
    @DisplayName("Expose D4: Small Box limit w<=10 becomes w<10")
    void exposeD4() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 4, 2, 1, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D4);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(in));
    }

    @Test
    @DisplayName("Expose D5: Indirect signature fee 3 -> 5")
    void exposeD5() {
        ShippingInput in = new ShippingInput(5.0, 1, 2, 4, 6, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("15.50"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D5);
        ShippingResult resD5 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("17.50"), resD5.totalCost());
        assertNotEquals(resNone.totalCost(), resD5.totalCost());
    }

    @Test
    @DisplayName("Expose D6: N4 direct signature no longer accepted for business (only indirect passes)")
    void exposeD6() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 4, 2, 2, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D6);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(in));
    }

    @Test
    @DisplayName("Expose D7: N6 applies to indirect signature instead of direct")
    void exposeD7() {
        ShippingInput in = new ShippingInput(5.0, 1, 2, 4, 2, 2, 0, 0, 0, 1, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("77.50"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D7);
        ShippingResult resD7 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("47.50"), resD7.totalCost());
        assertNotEquals(resNone.totalCost(), resD7.totalCost());
    }

    @Test
    @DisplayName("Expose D8: Hold-at-location fee never charged")
    void exposeD8() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 4, 2, 1, 0, 0, 1, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("43.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D8);
        ShippingResult resD8 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("28.00"), resD8.totalCost());
        assertNotEquals(resNone.totalCost(), resD8.totalCost());
    }

    @Test
    @DisplayName("Expose D9: N7 removed (fragile+dangerous allowed)")
    void exposeD9() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 4, 2, 1, 0, 0, 0, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D9);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        ShippingResult resD9 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("103.00"), resD9.totalCost());
    }

    @Test
    @DisplayName("Expose D10: N10 removed")
    void exposeD10() {
        ShippingInput in = new ShippingInput(20.0, 1, 2, 4, 3, 0, 1, 0, 0, 0, 1);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("157.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D10);
        ShippingResult resD10 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("117.00"), resD10.totalCost());
        assertNotEquals(resNone.totalCost(), resD10.totalCost());
    }

    @Test
    @DisplayName("Expose D11: N5 threshold weight>50 becomes weight>55")
    void exposeD11() {
        ShippingInput in = new ShippingInput(55.0, 1, 2, 4, 8, 0, 1, 0, 0, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D11);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        ShippingResult resD11 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("188.75"), resD11.totalCost());
    }

    @Test
    @DisplayName("Expose D12: Envelope limit w<=2 becomes w<2")
    void exposeD12() {
        ShippingInput in = new ShippingInput(2.0, 1, 1, 4, 1, 1, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D12);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(in));
    }

    @Test
    @DisplayName("Expose D13: Packaging code 7 multiplier 1.15 -> 1.25")
    void exposeD13() {
        ShippingInput in = new ShippingInput(20.0, 1, 3, 4, 7, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("72.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D13);
        ShippingResult resD13 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("78.00"), resD13.totalCost());
        assertNotEquals(resNone.totalCost(), resD13.totalCost());
    }

    @Test
    @DisplayName("Expose D14: Medium Box limit w<=25 becomes w<25")
    void exposeD14() {
        ShippingInput in = new ShippingInput(25.0, 1, 2, 4, 3, 1, 0, 0, 0, 0, 0);
        assertDoesNotThrow(() -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D14);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(in));
    }

    @Test
    @DisplayName("Expose D15: N9 applies to destination zone 3 instead of 4")
    void exposeD15() {
        ShippingInput in = new ShippingInput(10.0, 1, 3, 4, 2, 1, 1, 1, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("63.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D15);
        ShippingResult resD15 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("53.00"), resD15.totalCost());
        assertNotEquals(resNone.totalCost(), resD15.totalCost());
    }

    @Test
    @DisplayName("Expose D16: N1 discount 10% -> 5%")
    void exposeD16() {
        ShippingInput in = new ShippingInput(10.0, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("29.70"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D16);
        ShippingResult resD16 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("31.35"), resD16.totalCost());
        assertNotEquals(resNone.totalCost(), resD16.totalCost());
    }

    @Test
    @DisplayName("Expose D17: N3 exception removed (hold fee charged for option 7)")
    void exposeD17() {
        ShippingInput in = new ShippingInput(8.0, 2, 3, 7, 2, 1, 0, 0, 1, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("16.20"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D17);
        ShippingResult resD17 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("31.20"), resD17.totalCost());
        assertNotEquals(resNone.totalCost(), resD17.totalCost());
    }

    @Test
    @DisplayName("Expose D18: N8 allows deliveryOption 5")
    void exposeD18() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 5, 2, 1, 0, 0, 0, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D18);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        ShippingResult resD18 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("75.50"), resD18.totalCost());
    }

    @Test
    @DisplayName("Expose D19: Rate for pair (4,2) falls back to 2.00 while (2,4) stays correct")
    void exposeD19() {
        ShippingInput in42 = new ShippingInput(10.0, 4, 2, 4, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resNone42 = correctCalc.calculate(in42);
        assertEquals(new BigDecimal("35.50"), resNone42.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D19);
        ShippingResult resD19_42 = defectCalc.calculate(in42);
        assertEquals(new BigDecimal("23.00"), resD19_42.totalCost());
        assertNotEquals(resNone42.totalCost(), resD19_42.totalCost());

        // Verify (2,4) stays 35.50
        ShippingInput in24 = new ShippingInput(10.0, 2, 4, 4, 2, 1, 0, 0, 0, 0, 0);
        ShippingResult resD19_24 = defectCalc.calculate(in24);
        assertEquals(new BigDecimal("35.50"), resD19_24.totalCost());
    }

    @Test
    @DisplayName("Expose D20: Residence fee not charged when Saturday=1")
    void exposeD20() {
        ShippingInput in = new ShippingInput(10.0, 1, 2, 4, 2, 1, 1, 1, 0, 0, 0);
        ShippingResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("58.00"), resNone.totalCost());

        ShippingCalculator defectCalc = new ShippingCalculator(Defect.D20);
        ShippingResult resD20 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("48.00"), resD20.totalCost());
        assertNotEquals(resNone.totalCost(), resD20.totalCost());
    }
}

