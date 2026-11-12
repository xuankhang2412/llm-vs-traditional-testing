package com.payroll.enhanced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Defect Exposure Tests for Enhanced Payroll.
 * Every test passes under Defect.NONE and exposes the named defect Dk.
 */
public class DefectExposureTest {

    private final PayrollCalculator correctCalc = new PayrollCalculator(Defect.NONE);

    @Test
    @DisplayName("Expose D1: Overtime multiplier 1.5 -> 1.7")
    void exposeD1() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 4.0, 0.0, 2, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("774.53"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D1);
        PayrollResult resD1 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("788.00"), resD1.netPay());
        assertNotEquals(resNone.netPay(), resD1.netPay());
    }

    @Test
    @DisplayName("Expose D2: N3 removed")
    void exposeD2() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 8.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("978.66"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D2);
        PayrollResult resD2 = defectCalc.calculate(in);
        assertNotEquals(resNone.netPay(), resD2.netPay());
    }

    @Test
    @DisplayName("Expose D3: Federal rate for 20.01-40 changes 22% -> 25%")
    void exposeD3() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("663.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D3);
        PayrollResult resD3 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("633.50"), resD3.netPay());
        assertNotEquals(resNone.netPay(), resD3.netPay());
    }

    @Test
    @DisplayName("Expose D4: Arkansas lower range (15-30) withholding 3% -> 0%")
    void exposeD4() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 2, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("673.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D4);
        PayrollResult resD4 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("703.50"), resD4.netPay());
        assertNotEquals(resNone.netPay(), resD4.netPay());
    }

    @Test
    @DisplayName("Expose D5: N5 starts at 40.00 instead of 40.01")
    void exposeD5() {
        PayrollInput in = new PayrollInput(40.00, 40.0, 0.0, 0.0, 1, 1, 1, 0, 0.0, 0.0, 0, 0, 0);
        assertDoesNotThrow(() -> correctCalc.calculate(in));

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D5);
        assertThrows(IllegalArgumentException.class, () -> defectCalc.calculate(in));
    }

    @Test
    @DisplayName("Expose D6: N6 removed (health deductions charged even if hourly <= 20)")
    void exposeD6() {
        PayrollInput in = new PayrollInput(18.0, 40.0, 0.0, 0.0, 1, 3, 2, 2, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("607.92"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D6);
        PayrollResult resD6 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("460.92"), resD6.netPay());
        assertNotEquals(resNone.netPay(), resD6.netPay());
    }

    @Test
    @DisplayName("Expose D7: Dental plan 2 deduction 10 -> 12")
    void exposeD7() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 2, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("653.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D7);
        PayrollResult resD7 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("651.50"), resD7.netPay());
        assertNotEquals(resNone.netPay(), resD7.netPay());
    }

    @Test
    @DisplayName("Expose D8: N8 minimum 10 -> 20")
    void exposeD8() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 15.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("650.75"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D8);
        PayrollResult resD8 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("648.50"), resD8.netPay());
        assertNotEquals(resNone.netPay(), resD8.netPay());
    }

    @Test
    @DisplayName("Expose D9: N9 removed (LTD without STD allowed)")
    void exposeD9() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 1, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D9);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        PayrollResult resD9 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("649.50"), resD9.netPay());
    }

    @Test
    @DisplayName("Expose D10: N10 credit is subtracted instead of added")
    void exposeD10() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 1, 1, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("645.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D10);
        PayrollResult resD10 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("635.50"), resD10.netPay());
        assertNotEquals(resNone.netPay(), resD10.netPay());
    }

    @Test
    @DisplayName("Expose D11: N2 triggers at overtime>=5 instead of >5")
    void exposeD11() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 5.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("1187.50"), resNone.grossPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D11);
        PayrollResult resD11 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("1237.50"), resD11.grossPay());
        assertNotEquals(resNone.grossPay(), resD11.grossPay());
    }

    @Test
    @DisplayName("Expose D12: N1 overtime limit 10 -> 12")
    void exposeD12() {
        PayrollInput in = new PayrollInput(18.0, 40.0, 11.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D12);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        PayrollResult resD12 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("878.67"), resD12.netPay());
    }

    @Test
    @DisplayName("Expose D13: Medicare 1.45% -> 1.5%")
    void exposeD13() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("663.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D13);
        PayrollResult resD13 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("663.00"), resD13.netPay());
        assertNotEquals(resNone.netPay(), resD13.netPay());
    }

    @Test
    @DisplayName("Expose D14: Tennessee upper rate 8% -> 6%")
    void exposeD14() {
        PayrollInput in = new PayrollInput(40.0, 40.0, 0.0, 0.0, 1, 2, 1, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D14);
        PayrollResult resD14 = defectCalc.calculate(in);
        assertNotEquals(resNone.netPay(), resD14.netPay());
    }

    @Test
    @DisplayName("Expose D15: N4 removed")
    void exposeD15() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 1, 0, 0, 0.0, 0.0, 0, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(in));

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D15);
        assertDoesNotThrow(() -> defectCalc.calculate(in));
        PayrollResult resD15 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("613.50"), resD15.netPay());
    }

    @Test
    @DisplayName("Expose D16: N7 removed")
    void exposeD16() {
        PayrollInput in = new PayrollInput(30.0, 40.0, 0.0, 0.0, 2, 3, 2, 2, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("676.20"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D16);
        PayrollResult resD16 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("661.20"), resD16.netPay());
        assertNotEquals(resNone.netPay(), resD16.netPay());
    }

    @Test
    @DisplayName("Expose D17: Mississippi middle bracket starts at 25.00 instead of 25.01")
    void exposeD17() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 3, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("683.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D17);
        PayrollResult resD17 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("673.50"), resD17.netPay());
        assertNotEquals(resNone.netPay(), resD17.netPay());
    }

    @Test
    @DisplayName("Expose D18: Pension cap computed on base pay instead of gross")
    void exposeD18() {
        PayrollInput in = new PayrollInput(25.0, 30.0, 5.0, 3.0, 2, 0, 0, 0, 0.0, 200.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D18);
        PayrollResult resD18 = defectCalc.calculate(in);
        assertNotEquals(resNone.netPay(), resD18.netPay());
    }

    @Test
    @DisplayName("Expose D19: N10 requires LTD OR STD instead of both")
    void exposeD19() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 1, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("654.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D19);
        PayrollResult resD19 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("659.50"), resD19.netPay());
        assertNotEquals(resNone.netPay(), resD19.netPay());
    }

    @Test
    @DisplayName("Expose D20: States 2 and 3 are swapped in state-tax lookup")
    void exposeD20() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 2, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult resNone = correctCalc.calculate(in);
        assertEquals(new BigDecimal("673.50"), resNone.netPay());

        PayrollCalculator defectCalc = new PayrollCalculator(Defect.D20);
        PayrollResult resD20 = defectCalc.calculate(in);
        assertEquals(new BigDecimal("683.50"), resD20.netPay());
        assertNotEquals(resNone.netPay(), resD20.netPay());
    }
}
