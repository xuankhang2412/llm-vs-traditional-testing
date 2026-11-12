package com.payroll.enhanced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline tests for Enhanced PayrollCalculator under Defect.NONE.
 * Every expected value is computed by hand in comments.
 * Covers states 1-3, tax brackets, all notes (N1-N10), boundaries, and deductions.
 */
public class PayrollCalculatorTest {

    private final PayrollCalculator calc = new PayrollCalculator(Defect.NONE);

    // ── T1: Basic Tennessee, low rate (h=18) ──────────────────────────────────
    // hourly=18, reg=40, ot=0, hol=0, state=1(TN), med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, park=0
    // Gross: 18 * 40 = 720.00
    // Fed tax: h=18 in [15, 20] -> 10% -> 72.00
    // SS tax: 720 * 0.062 = 44.64
    // Medicare: 720 * 0.0145 = 10.44
    // State tax TN: h=18 <= 20 -> 0% -> 0.00
    // Net: 720 - 72 - 44.64 - 10.44 = 592.92
    @Test
    @DisplayName("T1: Basic Tennessee weekly pay -> Gross: $720.00, Net: $592.92")
    void testBasicTennessee() {
        PayrollInput in = new PayrollInput(18.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("720.00"), res.grossPay());
        assertEquals(new BigDecimal("592.92"), res.netPay());
    }

    // ── T2: Arkansas with overtime + N2 bonus (h=25) ───────────────────────────
    // hourly=25, reg=40, ot=8, hol=0, state=2(AR), med=1, den=1, vis=0, uw=0, pen=0, ltd=0, std=0, park=0
    // Base pay: 25 * 40 = 1000.00
    // Overtime pay: 25 * 8 * 1.5 = 300.00
    // N2 bonus: ot=8 > 5 -> +$50.00
    // Gross: 1000 + 300 + 50 = 1350.00
    // Fed tax: h=25 in [20.01, 40] -> 22% -> 1350 * 0.22 = 297.00
    // SS tax: 1350 * 0.062 = 83.70
    // Medicare: 1350 * 0.0145 = 19.575
    // State tax AR: h=25 in [15, 30] -> 3% -> 1350 * 0.03 = 40.50
    // Deductions: med P1=50, den P1=5 = 55.00
    // Net: 1350 - 297 - 83.70 - 19.575 - 40.50 - 55 = 854.225 -> rounded HALF_UP = 854.23
    @Test
    @DisplayName("T2: Arkansas with overtime and N2 bonus -> Gross: $1350.00, Net: $854.23")
    void testArkansasOvertimeAndBonus() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 8.0, 0.0, 2, 1, 1, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1350.00"), res.grossPay());
        assertEquals(new BigDecimal("854.23"), res.netPay());
    }

    // ── T3: Mississippi high rate bracket (h=45) ──────────────────────────────
    // hourly=45, reg=40, ot=0, hol=0, state=3(MS), med=2, den=1, vis=1, uw=0, pen=0, ltd=0, std=0, park=0
    // Gross: 45 * 40 = 1800.00
    // Fed tax: h=45 in [40.01, 50] -> 35% -> 630.00
    // SS tax: 1800 * 0.062 = 111.60
    // Medicare: 1800 * 0.0145 = 26.10
    // State tax MS: h=45 in [40.01, 50] -> 5% -> 90.00
    // Deductions: med P2=75, den P1=5, vis P1=5 = 85.00
    // Net: 1800 - 630 - 111.60 - 26.10 - 90 - 85 = 857.30
    @Test
    @DisplayName("T3: Mississippi high rate bracket -> Gross: $1800.00, Net: $857.30")
    void testMississippiHighBracket() {
        PayrollInput in = new PayrollInput(45.0, 40.0, 0.0, 0.0, 3, 2, 1, 1, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1800.00"), res.grossPay());
        assertEquals(new BigDecimal("857.30"), res.netPay());
    }

    // ── T4: N1 rejection (h=18, ot=11 > 10) ───────────────────────────────────
    @Test
    @DisplayName("T4: N1 rejects overtime > 10 for low hourly rates (15-20)")
    void testN1OvertimeCapReject() {
        PayrollInput in = new PayrollInput(18.0, 40.0, 11.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(in));
        assertTrue(ex.getMessage().contains("limits overtime to 10.0 hours"));
    }

    // ── T5: N4 rejection (medical without dental) ──────────────────────────────
    @Test
    @DisplayName("T5: N4 rejects medical != 0 when dental == 0")
    void testN4MedicalWithoutDentalReject() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 1, 0, 0, 0.0, 0.0, 0, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(in));
        assertTrue(ex.getMessage().contains("Medical enrollment requires dental"));
    }

    // ── T6: N5 rejection (hourly >= 40.01 requires medical plan 2 or 3) ────────
    @Test
    @DisplayName("T6: N5 rejects medical plan 1 when hourly >= 40.01")
    void testN5HighEarnerMedicalPlanReject() {
        PayrollInput in = new PayrollInput(41.0, 40.0, 0.0, 0.0, 1, 1, 1, 0, 0.0, 0.0, 0, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(in));
        assertTrue(ex.getMessage().contains("requires medical plan 2 or 3"));
    }

    // ── T7: N6 (hourly <= 20.00 zeroes medical, dental, and vision deductions) ─
    // hourly=20, reg=40, ot=0, hol=0, state=1(TN), med=3, den=2, vis=2, uw=0, pen=0, ltd=0, std=0, park=0
    // Gross: 20 * 40 = 800.00
    // Fed tax: 800 * 0.10 = 80.00
    // SS tax: 800 * 0.062 = 49.60
    // Medicare: 800 * 0.0145 = 11.60
    // State tax TN: 0.00
    // Deductions: N6 zeroes med(125), den(10), vis(12) = 0.00
    // N7 credit: med=3, den=2, vis=2 -> +$15.00
    // Net: 800 - 80 - 49.60 - 11.60 + 15.00 = 673.80
    @Test
    @DisplayName("T7: N6 zeroes medical, dental, vision deductions when hourly <= 20.00 -> Net: $673.80")
    void testN6DeductionsZeroedForLowHourly() {
        PayrollInput in = new PayrollInput(20.0, 40.0, 0.0, 0.0, 1, 3, 2, 2, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("800.00"), res.grossPay());
        assertEquals(new BigDecimal("673.80"), res.netPay());
    }

    // ── T8: N7 credit (medical=3, dental=2, vision=2 -> +$15 to net) ──────────
    // hourly=30, reg=40, ot=0, hol=0, state=2(AR), med=3, den=2, vis=2, uw=0, pen=0, ltd=0, std=0, park=0
    // Gross: 1200.00
    // Fed tax: 264.00, SS: 74.40, Medicare: 17.40, State AR: 36.00
    // Deductions: med=125, den=10, vis=12 = 147.00
    // Net before credits: 1200 - 264 - 74.40 - 17.40 - 36 - 147 = 661.20
    // N7 credit: +$15.00 -> Net = 676.20
    @Test
    @DisplayName("T8: N7 credit +$15 for comprehensive health coverage -> Net: $676.20")
    void testN7HealthCredit() {
        PayrollInput in = new PayrollInput(30.0, 40.0, 0.0, 0.0, 2, 3, 2, 2, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1200.00"), res.grossPay());
        assertEquals(new BigDecimal("676.20"), res.netPay());
    }

    // ── T9: N8 credit (unitedWay >= 10 -> +15% of contribution to net) ─────────
    // hourly=25, reg=40, ot=0, hol=0, state=1(TN), med=0, den=0, vis=0, uw=20, pen=0, ltd=0, std=0, park=0
    // Gross: 1000.00
    // Fed tax: 220.00, SS: 62.00, Medicare: 14.50, State TN: 40.00
    // Deductions: uw = 20.00
    // Net before credit: 1000 - 220 - 62 - 14.50 - 40 - 20 = 643.50
    // N8 credit: 20 * 0.15 = +$3.00 -> Net = 646.50
    @Test
    @DisplayName("T9: N8 credit +15% of United Way contribution -> Net: $646.50")
    void testN8UnitedWayCredit() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 20.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1000.00"), res.grossPay());
        assertEquals(new BigDecimal("646.50"), res.netPay());
    }

    // ── T10: N9 rejection (LTD=1, STD=0) ──────────────────────────────────────
    @Test
    @DisplayName("T10: N9 rejects LTD=1 when STD=0")
    void testN9LtdRequiresStdReject() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 1, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(in));
        assertTrue(ex.getMessage().contains("Long-term disability requires short-term disability"));
    }

    // ── T11: N10 credit (LTD=1 and STD=1 -> +$5 to net) ───────────────────────
    // hourly=25, reg=40, ot=0, hol=0, state=1(TN), med=0, den=0, vis=0, uw=0, pen=0, ltd=1, std=1, park=0
    // Gross: 1000.00
    // Taxes: 220 + 62 + 14.50 + 40 = 336.50
    // Deductions: ltd=14, std=9 = 23.00
    // Net before credit: 1000 - 336.50 - 23 = 640.50
    // N10 credit: +$5.00 -> Net = 645.50
    @Test
    @DisplayName("T11: N10 credit +$5 for combined LTD and STD -> Net: $645.50")
    void testN10DisabilityCredit() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 1, 1, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1000.00"), res.grossPay());
        assertEquals(new BigDecimal("645.50"), res.netPay());
    }

    // ── T12: Pension cap (capped at 10% of gross) ─────────────────────────────
    // hourly=30, reg=40, ot=0, hol=0, state=2(AR), med=0, den=0, vis=0, uw=0, pen=200, ltd=0, std=0, park=0
    // Gross: 1200.00
    // Pension cap: 10% of 1200 = 120.00 (min(200, 120) = 120.00)
    // Taxes: 264 + 74.40 + 17.40 + 36 = 391.80
    // Net: 1200 - 391.80 - 120 = 688.20
    @Test
    @DisplayName("T12: Pension cap at 10% of gross -> Net: $688.20")
    void testPensionCap() {
        PayrollInput in = new PayrollInput(30.0, 40.0, 0.0, 0.0, 2, 0, 0, 0, 0.0, 200.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1200.00"), res.grossPay());
        assertEquals(new BigDecimal("688.20"), res.netPay());
    }

    // ── T13: Holiday bonus N3 (holiday > 5 -> +$75) ───────────────────────────
    // hourly=25, reg=40, ot=0, hol=8, state=1(TN), med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, park=0
    // Gross: 1000 + (25 * 8 * 2.0 = 400) + 75 = 1475.00
    // Fed tax: 1475 * 0.22 = 324.50
    // SS tax: 1475 * 0.062 = 91.45
    // Medicare: 1475 * 0.0145 = 21.3875
    // State TN: 1475 * 0.04 = 59.00
    // Net: 1475 - 324.50 - 91.45 - 21.3875 - 59 = 978.6625 -> rounded HALF_UP = 978.66
    @Test
    @DisplayName("T13: Holiday bonus N3 -> Gross: $1475.00, Net: $978.66")
    void testHolidayBonusN3() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 8.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1475.00"), res.grossPay());
        assertEquals(new BigDecimal("978.66"), res.netPay());
    }

    // ── T14: Parking deduction ($39.50) ───────────────────────────────────────
    // hourly=25, reg=40, ot=0, hol=0, state=1(TN), med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, park=1
    // Gross: 1000.00
    // Taxes: 336.50
    // Parking: 39.50
    // Net: 1000 - 336.50 - 39.50 = 624.00
    @Test
    @DisplayName("T14: Parking deduction -> Gross: $1000.00, Net: $624.00")
    void testParkingDeduction() {
        PayrollInput in = new PayrollInput(25.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 1);
        PayrollResult res = calc.calculate(in);
        assertEquals(new BigDecimal("1000.00"), res.grossPay());
        assertEquals(new BigDecimal("624.00"), res.netPay());
    }

    // ── T15: Boundary checks for hourly rate (<15 and >50) ─────────────────────
    @Test
    @DisplayName("T15: Hourly rate boundaries (<15 and >50) rejected")
    void testHourlyRateBoundaries() {
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new PayrollInput(14.99, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0)));
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new PayrollInput(50.01, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0)));
    }
}
