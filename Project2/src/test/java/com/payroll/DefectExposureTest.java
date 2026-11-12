package com.payroll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DefectExposureTest {

    private final PayrollCalculator correctCalc = new PayrollCalculator(PayrollDefect.NONE);

    @Test
    @DisplayName("Expose D1: Maximum hourly salary ($50) validation removed")
    void exposeDefectD1() {
        // Hourly = 55.00 is rejected by NONE, but accepted by D1
        PayrollInput input = new PayrollInput(
                55.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        // NONE throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> correctCalc.calculate(input));

        // D1 computes without throwing
        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D1);
        PayrollResult resD1 = defectCalc.calculate(input);
        assertNotNull(resD1.getNetSalary());
    }

    @Test
    @DisplayName("Expose D2: Social Security rate changed from 0.062 to 0.05")
    void exposeDefectD2() {
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("658.80"), resNone.getNetSalary());

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D2);
        PayrollResult resD2 = defectCalc.calculate(input);
        // D2 SS rate is 0.05 instead of 0.062 (saving 800 * 0.012 = 9.60)
        assertNotEquals(resNone.getNetSalary(), resD2.getNetSalary());
        assertEquals(new BigDecimal("668.40"), resD2.getNetSalary());
    }

    @Test
    @DisplayName("Expose D3: Overtime multiplier changed from 1.5 to 1.25")
    void exposeDefectD3() {
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 10.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D3);
        PayrollResult resD3 = defectCalc.calculate(input);

        // D3 OT multiplier is 1.25 instead of 1.5
        assertNotEquals(resNone.getNetSalary(), resD3.getNetSalary());
    }

    @Test
    @DisplayName("Expose D4: 22% bracket ends at $35 instead of $40 (so $35.01-$40 gets 35%)")
    void exposeDefectD4() {
        // Hourly = 38.00 falls in 22% bracket for NONE, but in 35% bracket for D4
        PayrollInput input = new PayrollInput(
                38.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("0.22").compareTo(resNone.getFederalTaxRate()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D4);
        PayrollResult resD4 = defectCalc.calculate(input);
        assertEquals(0, new BigDecimal("0.35").compareTo(resD4.getFederalTaxRate()));
        assertNotEquals(resNone.getNetSalary(), resD4.getNetSalary());
    }

    @Test
    @DisplayName("Expose D5: Vision insurance deduction never applied")
    void exposeDefectD5() {
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 0, 0, 2, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("12.00").compareTo(resNone.getVisionDeduction()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D5);
        PayrollResult resD5 = defectCalc.calculate(input);
        assertEquals(0, BigDecimal.ZERO.compareTo(resD5.getVisionDeduction()));
        assertNotEquals(resNone.getNetSalary(), resD5.getNetSalary());
    }

    @Test
    @DisplayName("Expose D6: Medical Plan 2 and Plan 3 amounts swapped")
    void exposeDefectD6() {
        // Medical plan 2 is $75 for NONE, but $125 for D6
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 2, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("75.00").compareTo(resNone.getMedicalDeduction()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D6);
        PayrollResult resD6 = defectCalc.calculate(input);
        assertEquals(0, new BigDecimal("125.00").compareTo(resD6.getMedicalDeduction()));
        assertNotEquals(resNone.getNetSalary(), resD6.getNetSalary());
    }

    @Test
    @DisplayName("Expose D7: Dental deduction uses the medical plan amounts")
    void exposeDefectD7() {
        // Dental plan 1 is $5 for NONE, but $50 for D7
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 0, 1, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("5.00").compareTo(resNone.getDentalDeduction()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D7);
        PayrollResult resD7 = defectCalc.calculate(input);
        assertEquals(0, new BigDecimal("50.00").compareTo(resD7.getDentalDeduction()));
        assertNotEquals(resNone.getNetSalary(), resD7.getNetSalary());
    }

    @Test
    @DisplayName("Expose D8: Medical deduction uses the LTD amount ($14) instead of plan amounts")
    void exposeDefectD8() {
        // Medical plan 1 is $50 for NONE, but $14 for D8
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 1, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("50.00").compareTo(resNone.getMedicalDeduction()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D8);
        PayrollResult resD8 = defectCalc.calculate(input);
        assertEquals(0, new BigDecimal("14.00").compareTo(resD8.getMedicalDeduction()));
        assertNotEquals(resNone.getNetSalary(), resD8.getNetSalary());
    }

    @Test
    @DisplayName("Expose D9: United Way contribution ignored")
    void exposeDefectD9() {
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 0, 0, 0, 25.00, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(0, new BigDecimal("25.00").compareTo(resNone.getUnitedWayDeduction()));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D9);
        PayrollResult resD9 = defectCalc.calculate(input);
        assertEquals(0, BigDecimal.ZERO.compareTo(resD9.getUnitedWayDeduction()));
        assertNotEquals(resNone.getNetSalary(), resD9.getNetSalary());
    }

    @Test
    @DisplayName("Expose D10: Overtime hours ignored (treated as 0)")
    void exposeDefectD10() {
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 10.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult resNone = correctCalc.calculate(input);
        assertEquals(new BigDecimal("300.00"), resNone.getOvertimePay().setScale(2));

        PayrollCalculator defectCalc = new PayrollCalculator(PayrollDefect.D10);
        PayrollResult resD10 = defectCalc.calculate(input);
        assertEquals(new BigDecimal("0.00"), resD10.getOvertimePay().setScale(2));
        assertNotEquals(resNone.getNetSalary(), resD10.getNetSalary());
    }
}