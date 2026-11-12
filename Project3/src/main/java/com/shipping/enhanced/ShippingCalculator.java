package com.shipping.enhanced;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates enhanced package shipping cost based on weight, zones, delivery option,
 * packaging, signature, location type, and special surcharges.
 * Supports defect injection for mutation testing.
 */
public class ShippingCalculator {

    private final Defect defect;

    public ShippingCalculator() {
        this(Defect.fromSystemProperty());
    }

    public ShippingCalculator(Defect defect) {
        this.defect = (defect != null) ? defect : Defect.NONE;
    }

    public Defect getDefect() {
        return defect;
    }

    /**
     * @throws IllegalArgumentException if any input is invalid or violates business rules
     */
    public ShippingResult calculate(ShippingInput in) {
        if (in == null) {
            throw new IllegalArgumentException("ShippingInput cannot be null");
        }

        /* ── 1. Basic validation ──────────────────────────── */
        if (in.weight() <= 0.0 || in.weight() > 60.0) {
            throw new IllegalArgumentException("Weight must be in (0, 60] lbs (got " + in.weight() + ")");
        }
        if (in.origin() < 1 || in.origin() > 4) {
            throw new IllegalArgumentException("Origin zone must be 1-4 (got " + in.origin() + ")");
        }
        if (in.destination() < 1 || in.destination() > 4) {
            throw new IllegalArgumentException("Destination zone must be 1-4 (got " + in.destination() + ")");
        }
        if (in.deliveryOption() < 1 || in.deliveryOption() > 7) {
            throw new IllegalArgumentException("Delivery option must be 1-7 (got " + in.deliveryOption() + ")");
        }
        if (in.packaging() < 1 || in.packaging() > 8) {
            throw new IllegalArgumentException("Packaging must be 1-8 (got " + in.packaging() + ")");
        }
        if (in.signature() < 0 || in.signature() > 2) {
            throw new IllegalArgumentException("Signature must be 0, 1, or 2 (got " + in.signature() + ")");
        }
        if (in.locationType() < 0 || in.locationType() > 1) {
            throw new IllegalArgumentException("Location type must be 0 (Business) or 1 (Residence) (got " + in.locationType() + ")");
        }
        if (in.saturday() != 0 && in.saturday() != 1) {
            throw new IllegalArgumentException("Saturday delivery must be 0 or 1 (got " + in.saturday() + ")");
        }
        if (in.hold() != 0 && in.hold() != 1) {
            throw new IllegalArgumentException("Hold at location must be 0 or 1 (got " + in.hold() + ")");
        }
        if (in.fragile() != 0 && in.fragile() != 1) {
            throw new IllegalArgumentException("Fragile flag must be 0 or 1 (got " + in.fragile() + ")");
        }
        if (in.dangerous() != 0 && in.dangerous() != 1) {
            throw new IllegalArgumentException("Dangerous flag must be 0 or 1 (got " + in.dangerous() + ")");
        }

        /* ── Packaging weight limits ──────────────────────── */
        // Packaging 1: Envelope (w <= 2)
        // DEFECT D12 [boundary]: Envelope limit w<=2 becomes w<2
        if (in.packaging() == 1) {
            if (defect == Defect.D12) {
                if (in.weight() >= 2.0) {
                    throw new IllegalArgumentException("Envelope weight limit exceeded under D12 (got " + in.weight() + ")");
                }
            } else {
                if (in.weight() > 2.0) {
                    throw new IllegalArgumentException("Envelope weight limit exceeded (got " + in.weight() + ")");
                }
            }
        }

        // Packaging 2: Small Box (w <= 10)
        // DEFECT D4 [boundary]: Small Box limit w<=10 becomes w<10
        if (in.packaging() == 2) {
            if (defect == Defect.D4) {
                if (in.weight() >= 10.0) {
                    throw new IllegalArgumentException("Small Box weight limit exceeded under D4 (got " + in.weight() + ")");
                }
            } else {
                if (in.weight() > 10.0) {
                    throw new IllegalArgumentException("Small Box weight limit exceeded (got " + in.weight() + ")");
                }
            }
        }

        // Packaging 3: Medium Box (w <= 25)
        // DEFECT D14 [boundary]: Medium Box limit w<=25 becomes w<25
        if (in.packaging() == 3) {
            if (defect == Defect.D14) {
                if (in.weight() >= 25.0) {
                    throw new IllegalArgumentException("Medium Box weight limit exceeded under D14 (got " + in.weight() + ")");
                }
            } else {
                if (in.weight() > 25.0) {
                    throw new IllegalArgumentException("Medium Box weight limit exceeded (got " + in.weight() + ")");
                }
            }
        }

        // Packaging 4: Large Box (w <= 40)
        if (in.packaging() == 4 && in.weight() > 40.0) {
            throw new IllegalArgumentException("Large Box weight limit exceeded (got " + in.weight() + ")");
        }

        // Packaging 5: Tube (w <= 10)
        if (in.packaging() == 5 && in.weight() > 10.0) {
            throw new IllegalArgumentException("Tube weight limit exceeded (got " + in.weight() + ")");
        }

        /* ── Business location signature rule (N4) ────────── */
        // DEFECT D6 [conditional]: N4 direct signature no longer accepted for business (only indirect passes)
        if (in.locationType() == 0) {
            if (defect == Defect.D6) {
                if (in.signature() != 1) {
                    throw new IllegalArgumentException("Business location requires indirect signature under D6 (got " + in.signature() + ")");
                }
            } else {
                if (in.signature() != 1 && in.signature() != 2) {
                    throw new IllegalArgumentException("Business location requires signature 1 or 2 (got " + in.signature() + ")");
                }
            }
        }

        /* ── Heavy package signature rule (N5) ────────────── */
        // DEFECT D11 [boundary]: N5 threshold weight>50 becomes weight>55
        double n5Threshold = (defect == Defect.D11) ? 55.0 : 50.0;
        if (in.weight() > n5Threshold) {
            if (in.signature() != 1 && in.signature() != 2) {
                throw new IllegalArgumentException("Packages over " + n5Threshold + " lbs require signature 1 or 2 (got " + in.signature() + ")");
            }
        }

        /* ── Fragile and dangerous combination (N7) ───────── */
        // DEFECT D9 [conditional]: N7 removed (fragile+dangerous allowed)
        if (defect != Defect.D9) {
            if (in.fragile() == 1 && in.dangerous() == 1) {
                throw new IllegalArgumentException("Fragile and dangerous cannot be combined (N7)");
            }
        }

        /* ── Dangerous goods delivery option rule (N8) ────── */
        // DEFECT D18 [conditional]: N8 allows deliveryOption 5
        if (in.dangerous() == 1) {
            if (defect == Defect.D18) {
                if (in.deliveryOption() < 1 || in.deliveryOption() > 5) {
                    throw new IllegalArgumentException("Dangerous goods only allowed on delivery options 1-5 under D18 (got " + in.deliveryOption() + ")");
                }
            } else {
                if (in.deliveryOption() < 1 || in.deliveryOption() > 4) {
                    throw new IllegalArgumentException("Dangerous goods only allowed on delivery options 1-4 (got " + in.deliveryOption() + ")");
                }
            }
        }

        /* ── ORDER OF OPERATIONS ─────────────────────────── */

        // Step 1: Base rate per lb and base price
        BigDecimal rate = getBaseRate(in.origin(), in.destination());
        BigDecimal price = rate.multiply(BigDecimal.valueOf(in.weight()));

        // Step 2: Delivery multiplier (with N2)
        BigDecimal deliveryMult = getDeliveryMultiplier(in.deliveryOption(), in.fragile());
        price = price.multiply(deliveryMult);

        // Step 3: Packaging
        price = applyPackaging(price, in.packaging());

        // Step 4: Additive adjustments
        // Signature fee
        if (in.signature() == 1) {
            // DEFECT D5 [arithmetic]: Indirect signature fee 3 -> 5
            price = price.add(defect == Defect.D5 ? new BigDecimal("5.00") : new BigDecimal("3.00"));
        } else if (in.signature() == 2) {
            price = price.add(new BigDecimal("10.00"));
        }

        // Residence fee
        // DEFECT D20 [logic]: Residence fee not charged when Saturday=1
        if (in.locationType() == 1) {
            if (defect == Defect.D20 && in.saturday() == 1) {
                // Residence fee skipped under D20
            } else {
                price = price.add(new BigDecimal("10.00"));
            }
        }

        // Saturday fee (with N9)
        // DEFECT D15 [conditional]: N9 applies to destination zone 3 instead of 4
        if (in.saturday() == 1) {
            int n9TargetDest = (defect == Defect.D15) ? 3 : 4;
            if (in.locationType() == 1 && in.destination() == n9TargetDest) {
                price = price.add(new BigDecimal("10.00"));
            } else {
                price = price.add(new BigDecimal("20.00"));
            }
        }

        // Hold at location fee (with N3)
        // DEFECT D8 [logic]: Hold-at-location fee never charged
        // DEFECT D17 [conditional]: N3 exception removed (hold fee charged for option 7)
        if (defect == Defect.D8) {
            // Never charged
        } else if (in.hold() == 1) {
            if (defect == Defect.D17) {
                price = price.add(new BigDecimal("15.00"));
            } else {
                if (in.deliveryOption() != 7) {
                    price = price.add(new BigDecimal("15.00"));
                }
            }
        }

        // Fragile additive fee
        if (in.fragile() == 1) {
            price = price.add(new BigDecimal("25.00"));
        }

        // Dangerous additive fee
        if (in.dangerous() == 1) {
            price = price.add(new BigDecimal("50.00"));
        }

        // N6: fragile and direct signature -> extra +30
        // DEFECT D7 [conditional]: N6 applies to indirect signature instead of direct
        if (in.fragile() == 1) {
            int n6Sig = (defect == Defect.D7) ? 1 : 2;
            if (in.signature() == n6Sig) {
                price = price.add(new BigDecimal("30.00"));
            }
        }

        // N10: dangerous with signature 0 -> extra +40
        // DEFECT D10 [conditional]: N10 removed
        if (defect != Defect.D10) {
            if (in.dangerous() == 1 && in.signature() == 0) {
                price = price.add(new BigDecimal("40.00"));
            }
        }

        // Step 5: N1 discount on the total
        // N1: origin==destination and deliveryOption in {1,2,3} -> 10% discount on the whole running total.
        // DEFECT D3 [conditional]: N1 no longer applies to Same Day (option 1)
        // DEFECT D16 [arithmetic]: N1 discount 10% -> 5%
        boolean n1DeliveryOption;
        if (defect == Defect.D3) {
            n1DeliveryOption = (in.deliveryOption() == 2 || in.deliveryOption() == 3);
        } else {
            n1DeliveryOption = (in.deliveryOption() == 1 || in.deliveryOption() == 2 || in.deliveryOption() == 3);
        }

        if (in.origin() == in.destination() && n1DeliveryOption) {
            BigDecimal discountMultiplier = (defect == Defect.D16) ? new BigDecimal("0.95") : new BigDecimal("0.90");
            price = price.multiply(discountMultiplier);
        }

        // Step 6: Final rounding
        BigDecimal totalCost = price.setScale(2, RoundingMode.HALF_UP);
        return new ShippingResult(totalCost);
    }

    private BigDecimal getBaseRate(int origin, int destination) {
        // DEFECT D19 [logic]: Rate for pair (4,2) falls back to 2.00 while (2,4) stays correct
        if (defect == Defect.D19 && origin == 4 && destination == 2) {
            return new BigDecimal("2.00");
        }

        if (origin == destination) {
            return new BigDecimal("2.00");
        }

        int lo = Math.min(origin, destination);
        int hi = Math.max(origin, destination);

        if (lo == 1 && hi == 2) return new BigDecimal("2.50");
        if (lo == 1 && hi == 3) return new BigDecimal("3.00");
        if (lo == 1 && hi == 4) return new BigDecimal("3.75");
        if (lo == 2 && hi == 3) return new BigDecimal("2.75");
        if (lo == 2 && hi == 4) return new BigDecimal("3.25");
        if (lo == 3 && hi == 4) {
            // DEFECT D1 [arithmetic]: Zone 3-4 rate 2.30 -> 2.50
            return (defect == Defect.D1) ? new BigDecimal("2.50") : new BigDecimal("2.30");
        }

        return new BigDecimal("2.00");
    }

    private BigDecimal getDeliveryMultiplier(int deliveryOption, int fragile) {
        // N2: fragile and deliveryOption 1 -> multiplier x2.75 instead of x2.1
        if (deliveryOption == 1) {
            return (fragile == 1) ? new BigDecimal("2.75") : new BigDecimal("2.1");
        }
        if (deliveryOption == 2) {
            return new BigDecimal("1.5");
        }
        if (deliveryOption == 3) {
            // DEFECT D2 [arithmetic]: Priority Overnight multiplier 1.2 -> 1.5
            return (defect == Defect.D2) ? new BigDecimal("1.5") : new BigDecimal("1.2");
        }
        if (deliveryOption == 4) {
            return new BigDecimal("1.0");
        }
        if (deliveryOption == 5) {
            return new BigDecimal("0.9");
        }
        if (deliveryOption == 6) {
            return new BigDecimal("0.8");
        }
        if (deliveryOption == 7) {
            return new BigDecimal("0.6");
        }
        return BigDecimal.ONE;
    }

    private BigDecimal applyPackaging(BigDecimal currentPrice, int packaging) {
        switch (packaging) {
            case 1: // Envelope +0
            case 2: // Small Box +0
            case 6: // Customer packaged total dims <=30in +0
                return currentPrice;
            case 3: // Medium Box +$7
                return currentPrice.add(new BigDecimal("7.00"));
            case 4: // Large Box +$18
                return currentPrice.add(new BigDecimal("18.00"));
            case 5: // Tube +$5
                return currentPrice.add(new BigDecimal("5.00"));
            case 7: // Customer packaged >30 and <=60in: x1.15
                // DEFECT D13 [arithmetic]: Packaging code 7 multiplier 1.15 -> 1.25
                BigDecimal mult7 = (defect == Defect.D13) ? new BigDecimal("1.25") : new BigDecimal("1.15");
                return currentPrice.multiply(mult7);
            case 8: // Customer packaged >60 and <=100in: x1.30
                return currentPrice.multiply(new BigDecimal("1.30"));
            default:
                return currentPrice;
        }
    }
}

