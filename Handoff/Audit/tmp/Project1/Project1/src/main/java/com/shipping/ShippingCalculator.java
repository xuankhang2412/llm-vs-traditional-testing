package com.shipping;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ShippingCalculator {
    private final PackageDefect defect;

    public ShippingCalculator() {
        this(PackageDefect.NONE);
    }

    public ShippingCalculator(PackageDefect defect) {
        this.defect = (defect != null) ? defect : PackageDefect.NONE;
    }

    public PackageDefect getDefect() {
        return defect;
    }

    public ShippingResult calculate(PackageShippingInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        // Spec A1 Validation
        input.validate(defect);

        // Spec A1 Step 1 - base = baseRate(origin, destination) x weight
        BigDecimal baseRate = getBaseRate(input.getOrigin(), input.getDestination());
        BigDecimal baseCost = baseRate.multiply(BigDecimal.valueOf(input.getWeight()));

        // Spec A1 Step 2 - multiply by delivery multiplier
        BigDecimal deliveryMultiplier = getDeliveryMultiplier(input.getDeliveryOption());
        BigDecimal multipliedCost = baseCost.multiply(deliveryMultiplier);

        // Spec A1 Step 3 - add surcharges
        BigDecimal signatureSurcharge = getSignatureSurcharge(input.getSignatureOption());
        BigDecimal residentialSurcharge = getResidentialSurcharge(input.getDeliveryLocationType());
        BigDecimal saturdaySurcharge = (input.getSaturdayDelivery() == 1) ? BigDecimal.valueOf(20.00) : BigDecimal.ZERO;
        BigDecimal holdSurcharge = (input.getHoldAtLocation() == 1) ? BigDecimal.valueOf(15.00) : BigDecimal.ZERO;
        BigDecimal fragileSurcharge = getFragileSurcharge(input.getFragile());
        BigDecimal dangerousSurcharge = (input.getDangerous() == 1) ? BigDecimal.valueOf(50.00) : BigDecimal.ZERO;

        // Spec A1 Step 4 - output total price in USD plus a breakdown (base, multiplier, each adjustment, total)
        BigDecimal totalBeforeRounding = multipliedCost
                .add(signatureSurcharge)
                .add(residentialSurcharge)
                .add(saturdaySurcharge)
                .add(holdSurcharge)
                .add(fragileSurcharge)
                .add(dangerousSurcharge);

        BigDecimal roundedTotal = totalBeforeRounding.setScale(2, RoundingMode.HALF_UP);

        return new ShippingResult(
                baseRate,
                baseCost,
                deliveryMultiplier,
                multipliedCost,
                signatureSurcharge,
                residentialSurcharge,
                saturdaySurcharge,
                holdSurcharge,
                fragileSurcharge,
                dangerousSurcharge,
                roundedTotal);
    }

    private BigDecimal getBaseRate(int origin, int destination) {
        int minZone = Math.min(origin, destination);
        int maxZone = Math.max(origin, destination);

        // Same zone check
        if (minZone == maxZone) {
            // DEFECT D3 [arithmetic]: Same-zone rate increased from $2 to $3 per lb
            if (defect == PackageDefect.D3) {
                return BigDecimal.valueOf(3.00);
            } else {
                return BigDecimal.valueOf(2.00);
            }
        }

        int keyMin = minZone;
        int keyMax = maxZone;

        // DEFECT D4 [logic]: Zone pair (2,3)/(3,2) looked up with an invalid key (2,5), falling back to default $2.00/lb
        if (defect == PackageDefect.D4 && minZone == 2 && maxZone == 3) {
            keyMin = 2;
            keyMax = 5;
        }

        if (keyMin == 1 && keyMax == 2) {
            return BigDecimal.valueOf(2.50);
        } else if (keyMin == 1 && keyMax == 3) {
            return BigDecimal.valueOf(3.00);
        } else if (keyMin == 1 && keyMax == 4) {
            // DEFECT D5 [logic]: Special handling of pair (1,4)/(4,1) removed, falling back to default $2.00/lb
            if (defect == PackageDefect.D5) {
                return BigDecimal.valueOf(2.00);
            } else {
                return BigDecimal.valueOf(3.75);
            }
        } else if (keyMin == 2 && keyMax == 3) {
            return BigDecimal.valueOf(2.75);
        } else if (keyMin == 2 && keyMax == 4) {
            return BigDecimal.valueOf(3.25);
        } else if (keyMin == 3 && keyMax == 4) {
            return BigDecimal.valueOf(2.30);
        } else {
            // Default rate per lb fallback
            return BigDecimal.valueOf(2.00);
        }
    }

    private BigDecimal getDeliveryMultiplier(int deliveryOption) {
        switch (deliveryOption) {
            case 1: // Same Day
                // DEFECT D7 [logic]: Multipliers of Same Day and Standard Overnight swapped
                if (defect == PackageDefect.D7) {
                    return BigDecimal.valueOf(1.0);
                } else {
                    return BigDecimal.valueOf(2.1);
                }
            case 2: // First Overnight
                return BigDecimal.valueOf(1.5);
            case 3: // Priority Overnight
                // DEFECT D6 [arithmetic]: Priority Overnight multiplier increased from 1.2 to 2.2
                if (defect == PackageDefect.D6) {
                    return BigDecimal.valueOf(2.2);
                } else {
                    return BigDecimal.valueOf(1.2);
                }
            case 4: // Standard Overnight
                // DEFECT D7 [logic]: Multipliers of Same Day and Standard Overnight swapped
                if (defect == PackageDefect.D7) {
                    return BigDecimal.valueOf(2.1);
                } else {
                    return BigDecimal.valueOf(1.0);
                }
            case 5: // 2-day AM
                return BigDecimal.valueOf(0.9);
            case 6: // 2-day
                return BigDecimal.valueOf(0.8);
            case 7: // Express Saver
                return BigDecimal.valueOf(0.6);
            default:
                throw new IllegalArgumentException("Unknown delivery option: " + deliveryOption);
        }
    }

    private BigDecimal getSignatureSurcharge(int signatureOption) {
        switch (signatureOption) {
            case 0: // None
                return BigDecimal.ZERO;
            case 1: // Indirect
                return BigDecimal.valueOf(3.00);
            case 2: // Direct
                return BigDecimal.valueOf(10.00);
            default:
                throw new IllegalArgumentException("Unknown signature option: " + signatureOption);
        }
    }

    private BigDecimal getResidentialSurcharge(int deliveryLocationType) {
        int locType = deliveryLocationType;

        // DEFECT D9 [logic]: Delivery location type interpreted inversely (Business <-> Residence)
        if (defect == PackageDefect.D9) {
            locType = (locType == 0) ? 1 : 0;
        }

        if (locType == 1) { // Residence
            // DEFECT D8 [arithmetic]: Residential surcharge increased from $10 to $20
            if (defect == PackageDefect.D8) {
                return BigDecimal.valueOf(20.00);
            } else {
                return BigDecimal.valueOf(10.00);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getFragileSurcharge(int fragile) {
        // DEFECT D10 [logic]: Fragile condition inverted (surcharge applied when fragile = 0)
        boolean applyFragile;
        if (defect == PackageDefect.D10) {
            applyFragile = (fragile == 0);
        } else {
            applyFragile = (fragile == 1);
        }

        return applyFragile ? BigDecimal.valueOf(25.00) : BigDecimal.ZERO;
    }
}
