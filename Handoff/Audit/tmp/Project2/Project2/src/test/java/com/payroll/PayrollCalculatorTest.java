package com.payroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PayrollCalculatorTest {

    private PayrollCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PayrollCalculator(PayrollDefect.NONE);
    }

    @Test
    @DisplayName("Test 1: Verify the sample from the research paper (hourly=24.00, reg=36)")
    void testPaperSample() {
        // Hand arithmetic from spec:
        // hourly = 24.00, regular = 36 -> basePay = 24.00 * 36 = 864.00
        // ot = 0, hol = 0 -> gross = 864.00
        // Federal rate for 24.00 (in 20.01..40.00) = 22% (0.22)
        // federalTax = 864.00 * 0.22 = 190.08
        // socialSecurity = 864.00 * 0.062 = 53.568
        // medicare = 864.00 * 0.0145 = 12.528
        // Deductions:
        //   med = 0, den = 1 ($5.00), vis = 0, uw = 0
        //   pension = min(40.00, 0.10 * 864.00 = 86.40) = 40.00
        //   ltd = 0, std = 1 ($9.00), parking = 0
        //   totalDeductions = 5.00 + 40.00 + 9.00 = 54.00
        // Taxes total = 190.08 + 53.568 + 12.528 = 256.176
        // Net before rounding = 864.00 - 256.176 - 54.00 = 553.824
        // Final rounded net (HALF_UP, 2 decimals) = 553.82
        // Note: Paper lists 553.8231 (difference < 0.001), both round to 553.82.
        PayrollInput input = new PayrollInput(
                24.00, 36.0, 0.0, 0.0, 0, 1, 0, 0.0, 40.0, 0, 1, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("864.00"), result.getGrossSalary().setScale(2));
        assertEquals(new BigDecimal("553.82"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 2: Boundary hourly = $15.00 (minimum limit) with OT and Holiday")
    void testMinHourlySalary() {
        // Hand arithmetic:
        // base = 15.00 * 40 = 600.00
        // ot = 15.00 * 10 * 1.5 = 225.00
        // hol = 15.00 * 5 * 2.0 = 150.00
        // gross = 600 + 225 + 150 = 975.00
        // fedRate = 10% -> fedTax = 97.50
        // ss = 975 * 0.062 = 60.45
        // med = 975 * 0.0145 = 14.1375
        // Deductions: med P1 (50) + den P1 (5) + vis P1 (5) + pension min(50, 97.5)=50 + uw 10 + ltd 14 + pkg 39.50 = 173.50
        // Raw Net = 975 - (97.50 + 60.45 + 14.1375) - 173.50 = 975 - 172.0875 - 173.50 = 629.4125
        // Net rounded = 629.41
        PayrollInput input = new PayrollInput(
                15.00, 40.0, 10.0, 5.0, 1, 1, 1, 10.0, 50.0, 1, 0, 1);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("975.00"), result.getGrossSalary().setScale(2));
        assertEquals(new BigDecimal("629.41"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 3: Boundary hourly = $20.00 (10% bracket upper boundary)")
    void testHourly20_00() {
        // Hand arithmetic:
        // gross = 20.00 * 40 = 800.00
        // fedRate = 0.10 -> fedTax = 80.00
        // ss = 800.00 * 0.062 = 49.60
        // med = 800.00 * 0.0145 = 11.60
        // total taxes = 80.00 + 49.60 + 11.60 = 141.20
        // net = 800.00 - 141.20 = 658.80
        PayrollInput input = new PayrollInput(
                20.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("658.80"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 4: Boundary hourly = $20.01 (22% bracket lower boundary)")
    void testHourly20_01() {
        // Hand arithmetic:
        // gross = 20.01 * 40 = 800.40
        // fedRate = 0.22 -> fedTax = 800.40 * 0.22 = 176.088
        // ss = 800.40 * 0.062 = 49.6248
        // med = 800.40 * 0.0145 = 11.6058
        // total taxes = 176.088 + 49.6248 + 11.6058 = 237.3186
        // raw net = 800.40 - 237.3186 = 563.0814
        // net rounded = 563.08
        PayrollInput input = new PayrollInput(
                20.01, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("563.08"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 5: Boundary hourly = $40.00 (22% bracket upper boundary)")
    void testHourly40_00() {
        // Hand arithmetic:
        // gross = 40.00 * 40 = 1600.00
        // fedRate = 0.22 -> fedTax = 1600.00 * 0.22 = 352.00
        // ss = 1600.00 * 0.062 = 99.20
        // med = 1600.00 * 0.0145 = 23.20
        // total taxes = 352.00 + 99.20 + 23.20 = 474.40
        // net = 1600.00 - 474.40 = 1125.60
        PayrollInput input = new PayrollInput(
                40.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("1125.60"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 6: Boundary hourly = $40.01 (35% bracket lower boundary)")
    void testHourly40_01() {
        // Hand arithmetic:
        // gross = 40.01 * 40 = 1600.40
        // fedRate = 0.35 -> fedTax = 1600.40 * 0.35 = 560.14
        // ss = 1600.40 * 0.062 = 99.2248
        // med = 1600.40 * 0.0145 = 23.2058
        // total taxes = 560.14 + 99.2248 + 23.2058 = 682.5706
        // raw net = 1600.40 - 682.5706 = 917.8294
        // net rounded = 917.83
        PayrollInput input = new PayrollInput(
                40.01, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("917.83"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 7: Boundary hourly = $50.00 (maximum limit) with capped pension")
    void testHourly50_00CappedPension() {
        // Hand arithmetic:
        // gross = 50.00 * 40 = 2000.00
        // fedRate = 0.35 -> fedTax = 700.00
        // ss = 2000 * 0.062 = 124.00
        // med = 2000 * 0.0145 = 29.00
        // total taxes = 853.00
        // pension = min(500.00, 10% of 2000 = 200.00) = 200.00
        // net = 2000 - 853 - 200 = 947.00
        PayrollInput input = new PayrollInput(
                50.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 500.0, 0, 0, 0);

        PayrollResult result = calculator.calculate(input);
        assertEquals(new BigDecimal("947.00"), result.getNetSalary());
    }

    @Test
    @DisplayName("Test 8: Hourly salary below 15.00 throws exception")
    void testHourlyUnder15() {
        PayrollInput input = new PayrollInput(
                14.99, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
    }

    @Test
    @DisplayName("Test 9: Hourly salary above 50.00 throws exception")
    void testHourlyOver50() {
        PayrollInput input = new PayrollInput(
                50.01, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
    }

    @Test
    @DisplayName("Test 10: Negative hours throws exception")
    void testNegativeHours() {
        PayrollInput input = new PayrollInput(
                25.00, -5.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(input));
    }
}