package com.shipping.enhanced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline tests for ShippingCalculator under Defect.NONE.
 * Every expected value is computed by hand in comments.
 * Covers calibration test, boundaries, and all notes (N1-N10).
 */
public class ShippingCalculatorTest {

    private final ShippingCalculator calc = new ShippingCalculator(Defect.NONE);

    // ── T1: Mandatory Calibration Test (from source paper) ─────────────────────
    // weight=35, origin=3, dest=3, deliveryOption=2, packaging=4, signature=1, loc=0, sat=1, hold=0, frag=0, dang=0
    // Base rate(3,3)=2.00 -> baseCost = 2.00 * 35 = 70.00
    // Delivery option 2 (First Overnight) multiplier = 1.5 -> 70.00 * 1.5 = 105.00
    // Packaging 4 (Large Box, w=35<=40) = +$18.00 -> 105.00 + 18.00 = 123.00
    // Signature 1 = +$3.00
    // Saturday = +$20.00 (loc=0, so normal Saturday fee)
    // Running total = 123.00 + 3.00 + 20.00 = 146.00
    // N1: origin==dest (3==3) and deliveryOption=2 in {1,2,3} -> 10% discount: 146.00 * 0.90 = 131.40
    @Test
    @DisplayName("T1: Calibration test from source paper -> exactly $131.40")
    void testCalibration() {
        ShippingInput input = new ShippingInput(35.0, 3, 3, 2, 4, 1, 0, 1, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("131.40"), result.totalCost());
    }

    // ── T2: Basic same-zone standard shipping to residence ─────────────────────
    // weight=10, origin=1, dest=1, deliveryOption=4, packaging=2, signature=0, loc=1, sat=0, hold=0, frag=0, dang=0
    // Base rate(1,1)=2.00 -> baseCost = 2.00 * 10 = 20.00
    // Delivery option 4 multiplier = 1.0 -> 20.00
    // Packaging 2 (Small Box, w=10<=10) = +$0 -> 20.00
    // Residence (loc=1) = +$10.00 -> 30.00
    // N1 not applicable (deliveryOption=4 not in {1,2,3}) -> 30.00
    @Test
    @DisplayName("T2: Basic same-zone standard shipping to residence -> $30.00")
    void testSameZoneStandardResidence() {
        ShippingInput input = new ShippingInput(10.0, 1, 1, 4, 2, 0, 1, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("30.00"), result.totalCost());
    }

    // ── T3: N1 discount on Same Day (deliveryOption 1) with Envelope ────────────
    // weight=2, origin=2, dest=2, deliveryOption=1, packaging=1, signature=1, loc=0, sat=0, hold=0, frag=0, dang=0
    // Base rate(2,2)=2.00 -> baseCost = 2.00 * 2 = 4.00
    // Delivery option 1 (Same Day) multiplier = 2.1 -> 4.00 * 2.1 = 8.40
    // Packaging 1 (Envelope, w=2<=2) = +$0 -> 8.40
    // Signature 1 = +$3.00 -> 11.40
    // N1 discount: 2==2 and deliveryOption=1 in {1,2,3} -> 11.40 * 0.90 = 10.26
    @Test
    @DisplayName("T3: N1 discount with Same Day and Envelope -> $10.26")
    void testN1DiscountSameDayEnvelope() {
        ShippingInput input = new ShippingInput(2.0, 2, 2, 1, 1, 1, 0, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("10.26"), result.totalCost());
    }

    // ── T4: N2 (fragile + SameDay) and N6 (fragile + direct signature) ──────────
    // weight=5, origin=1, dest=2, deliveryOption=1, packaging=2, signature=2, loc=0, sat=0, hold=0, frag=1, dang=0
    // Base rate(1,2)=2.50 -> baseCost = 2.50 * 5 = 12.50
    // N2: fragile=1 and deliveryOption=1 -> multiplier = 2.75 -> 12.50 * 2.75 = 34.375
    // Packaging 2 (Small Box, w=5<=10) = +$0 -> 34.375
    // Signature 2 = +$10.00
    // Fragile = +$25.00
    // N6: fragile=1 and direct signature(2) -> extra +$30.00
    // Total = 34.375 + 10.00 + 25.00 + 30.00 = 99.375 -> rounded HALF_UP = 99.38
    @Test
    @DisplayName("T4: N2 fragile same-day multiplier and N6 fragile direct signature -> $99.38")
    void testN2AndN6FragileDirectSignature() {
        ShippingInput input = new ShippingInput(5.0, 1, 2, 1, 2, 2, 0, 0, 0, 1, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("99.38"), result.totalCost());
    }

    // ── T5: N3 (deliveryOption 7 Express Saver with hold=1 -> no hold fee) ──────
    // weight=8, origin=2, dest=3, deliveryOption=7, packaging=2, signature=1, loc=0, sat=0, hold=1, frag=0, dang=0
    // Base rate(2,3)=2.75 -> baseCost = 2.75 * 8 = 22.00
    // Delivery option 7 multiplier = 0.6 -> 22.00 * 0.6 = 13.20
    // Packaging 2 = +$0 -> 13.20
    // Signature 1 = +$3.00
    // N3: deliveryOption=7 and hold=1 -> hold fee = $0
    // Total = 13.20 + 3.00 = 16.20
    @Test
    @DisplayName("T5: N3 hold fee waived for Express Saver -> $16.20")
    void testN3HoldFeeWaivedForOption7() {
        ShippingInput input = new ShippingInput(8.0, 2, 3, 7, 2, 1, 0, 0, 1, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("16.20"), result.totalCost());
    }

    // ── T6: Standard hold fee ($15) applied for deliveryOption != 7 ─────────────
    // weight=8, origin=2, dest=3, deliveryOption=6, packaging=2, signature=1, loc=0, sat=0, hold=1, frag=0, dang=0
    // Base rate(2,3)=2.75 -> baseCost = 2.75 * 8 = 22.00
    // Delivery option 6 multiplier = 0.8 -> 22.00 * 0.8 = 17.60
    // Packaging 2 = +$0 -> 17.60
    // Signature 1 = +$3.00
    // Hold fee = +$15.00
    // Total = 17.60 + 3.00 + 15.00 = 35.60
    @Test
    @DisplayName("T6: Hold fee charged for delivery option 6 -> $35.60")
    void testHoldFeeChargedForOption6() {
        ShippingInput input = new ShippingInput(8.0, 2, 3, 6, 2, 1, 0, 0, 1, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("35.60"), result.totalCost());
    }

    // ── T7: N4 validation (Business location requires signature 1 or 2) ─────────
    @Test
    @DisplayName("T7: N4 business location requires signature 1 or 2 (rejects sig 0)")
    void testN4BusinessRequiresSignature() {
        ShippingInput input = new ShippingInput(10.0, 1, 2, 4, 2, 0, 0, 0, 0, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(input));
        assertTrue(ex.getMessage().contains("Business location requires signature"));
    }

    // ── T8: N5 validation (weight > 50 requires signature 1 or 2) ───────────────
    @Test
    @DisplayName("T8: N5 weight > 50 requires signature 1 or 2")
    void testN5HeavyPackageRequiresSignature() {
        ShippingInput input = new ShippingInput(55.0, 1, 2, 4, 8, 0, 1, 0, 0, 0, 0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(input));
        assertTrue(ex.getMessage().contains("require signature 1 or 2"));
    }

    // ── T9: N7 validation (fragile + dangerous cannot be combined) ──────────────
    @Test
    @DisplayName("T9: N7 fragile and dangerous combined -> reject")
    void testN7FragileAndDangerousReject() {
        ShippingInput input = new ShippingInput(10.0, 1, 2, 4, 2, 1, 0, 0, 0, 1, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(input));
        assertTrue(ex.getMessage().contains("Fragile and dangerous cannot be combined"));
    }

    // ── T10: N8 validation (dangerous requires deliveryOption 1..4) ────────────
    @Test
    @DisplayName("T10: N8 dangerous requires deliveryOption in {1,2,3,4} (rejects 5)")
    void testN8DangerousDeliveryOptionReject() {
        ShippingInput input = new ShippingInput(10.0, 1, 2, 5, 2, 1, 0, 0, 0, 0, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> calc.calculate(input));
        assertTrue(ex.getMessage().contains("Dangerous goods only allowed on delivery options 1-4"));
    }

    // ── T11: N9 (residence + zone 4 + Saturday fee is $10 instead of $20) ───────
    // weight=15, origin=1, dest=4, deliveryOption=4, packaging=3, signature=1, loc=1, sat=1, hold=0, frag=0, dang=0
    // Base rate(1,4)=3.75 -> baseCost = 3.75 * 15 = 56.25
    // Delivery option 4 multiplier = 1.0 -> 56.25
    // Packaging 3 (Medium Box, w=15<=25) = +$7.00 -> 63.25
    // Signature 1 = +$3.00
    // Residence = +$10.00
    // Saturday fee (N9: dest=4, loc=1) = +$10.00
    // Total = 63.25 + 3.00 + 10.00 + 10.00 = 86.25
    @Test
    @DisplayName("T11: N9 reduced Saturday fee for residence in destination zone 4 -> $86.25")
    void testN9ReducedSaturdayFee() {
        ShippingInput input = new ShippingInput(15.0, 1, 4, 4, 3, 1, 1, 1, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("86.25"), result.totalCost());
    }

    // ── T12: N10 (dangerous with signature 0 -> extra +$40) ────────────────────
    // weight=20, origin=1, dest=2, deliveryOption=4, packaging=3, signature=0, loc=1, sat=0, hold=0, frag=0, dang=1
    // Base rate(1,2)=2.50 -> baseCost = 2.50 * 20 = 50.00
    // Delivery option 4 multiplier = 1.0 -> 50.00
    // Packaging 3 (Medium Box, w=20<=25) = +$7.00 -> 57.00
    // Residence (loc=1) = +$10.00
    // Dangerous = +$50.00
    // N10: dangerous=1 and signature=0 -> extra +$40.00
    // Total = 57.00 + 10.00 + 50.00 + 40.00 = 157.00
    @Test
    @DisplayName("T12: N10 dangerous with signature 0 gets extra +$40 -> $157.00")
    void testN10DangerousSignature0() {
        ShippingInput input = new ShippingInput(20.0, 1, 2, 4, 3, 0, 1, 0, 0, 0, 1);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("157.00"), result.totalCost());
    }

    // ── T13: Packaging 7 multiplier (x1.15) ────────────────────────────────────
    // weight=20, origin=1, dest=3, deliveryOption=4, packaging=7, signature=0, loc=1, sat=0, hold=0, frag=0, dang=0
    // Base rate(1,3)=3.00 -> baseCost = 3.00 * 20 = 60.00
    // Delivery option 4 multiplier = 1.0 -> 60.00
    // Packaging 7: 60.00 * 1.15 = 69.00
    // Residence = +$10.00
    // Total = 69.00 + 10.00 = 79.00
    @Test
    @DisplayName("T13: Packaging 7 multiplier x1.15 -> $79.00")
    void testPackaging7Multiplier() {
        ShippingInput input = new ShippingInput(20.0, 1, 3, 4, 7, 0, 1, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("79.00"), result.totalCost());
    }

    // ── T14: Packaging 8 multiplier (x1.30) ────────────────────────────────────
    // weight=20, origin=2, dest=4, deliveryOption=4, packaging=8, signature=1, loc=0, sat=0, hold=0, frag=0, dang=0
    // Base rate(2,4)=3.25 -> baseCost = 3.25 * 20 = 65.00
    // Delivery option 4 multiplier = 1.0 -> 65.00
    // Packaging 8: 65.00 * 1.30 = 84.50
    // Signature 1 = +$3.00
    // Total = 84.50 + 3.00 = 87.50
    @Test
    @DisplayName("T14: Packaging 8 multiplier x1.30 -> $87.50")
    void testPackaging8Multiplier() {
        ShippingInput input = new ShippingInput(20.0, 2, 4, 4, 8, 1, 0, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("87.50"), result.totalCost());
    }

    // ── T15: Packaging 5 Tube (w<=10, +$5) ─────────────────────────────────────
    // weight=10, origin=3, dest=4, deliveryOption=4, packaging=5, signature=1, loc=0, sat=0, hold=0, frag=0, dang=0
    // Base rate(3,4)=2.30 -> baseCost = 2.30 * 10 = 23.00
    // Delivery option 4 multiplier = 1.0 -> 23.00
    // Packaging 5 (Tube, w=10<=10) = +$5.00 -> 28.00
    // Signature 1 = +$3.00
    // Total = 28.00 + 3.00 = 31.00
    @Test
    @DisplayName("T15: Packaging 5 Tube fee -> $31.00")
    void testPackaging5Tube() {
        ShippingInput input = new ShippingInput(10.0, 3, 4, 4, 5, 1, 0, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("31.00"), result.totalCost());
    }

    // ── T16: Boundary weight = 60.0 (maximum valid) ────────────────────────────
    // weight=60.0, origin=1, dest=1, deliveryOption=4, packaging=8, signature=1, loc=0, sat=0, hold=0, frag=0, dang=0
    // Base rate(1,1)=2.00 -> baseCost = 2.00 * 60 = 120.00
    // Delivery option 4 multiplier = 1.0 -> 120.00
    // Packaging 8: 120.00 * 1.30 = 156.00
    // Signature 1 = +$3.00
    // Total = 156.00 + 3.00 = 159.00
    @Test
    @DisplayName("T16: Boundary maximum weight 60.0 lbs -> $159.00")
    void testMaxWeight60() {
        ShippingInput input = new ShippingInput(60.0, 1, 1, 4, 8, 1, 0, 0, 0, 0, 0);
        ShippingResult result = calc.calculate(input);
        assertEquals(new BigDecimal("159.00"), result.totalCost());
    }

    // ── T17: Boundary weight rejections: <=0 and >60 ────────────────────────────
    @Test
    @DisplayName("T17: Boundary weights <=0 and >60 rejected")
    void testBoundaryWeightRejections() {
        // weight = 0.0
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(0.0, 1, 1, 4, 2, 1, 0, 0, 0, 0, 0)));
        // weight = -1.0
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(-1.0, 1, 1, 4, 2, 1, 0, 0, 0, 0, 0)));
        // weight = 60.01
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(60.01, 1, 1, 4, 8, 1, 0, 0, 0, 0, 0)));
    }

    // ── T18: Packaging weight limit rejections ──────────────────────────────────
    @Test
    @DisplayName("T18: Packaging weight limits exceeded -> reject")
    void testPackagingWeightLimitExceeded() {
        // Envelope w=2.01 > 2
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(2.01, 1, 1, 4, 1, 1, 0, 0, 0, 0, 0)));
        // Small Box w=10.01 > 10
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(10.01, 1, 1, 4, 2, 1, 0, 0, 0, 0, 0)));
        // Medium Box w=25.01 > 25
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(25.01, 1, 1, 4, 3, 1, 0, 0, 0, 0, 0)));
        // Large Box w=40.01 > 40
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(40.01, 1, 1, 4, 4, 1, 0, 0, 0, 0, 0)));
        // Tube w=10.01 > 10
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(new ShippingInput(10.01, 1, 1, 4, 5, 1, 0, 0, 0, 0, 0)));
    }
}

