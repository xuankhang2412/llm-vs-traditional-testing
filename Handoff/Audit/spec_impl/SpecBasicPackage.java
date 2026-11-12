package spec_impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpecBasicPackage {

    public static String calculate(double weight, int origin, int destination, int deliveryOption,
                                   int signatureOption, int deliveryLocationType, int saturdayDelivery,
                                   int holdAtLocation, int fragile, int dangerous) {
        // Validation per Spec Section 2
        if (weight <= 0.0 || weight > 60.0) return "ERROR";
        if (origin < 1 || origin > 4) return "ERROR";
        if (destination < 1 || destination > 4) return "ERROR";
        if (deliveryOption < 1 || deliveryOption > 7) return "ERROR";
        if (signatureOption < 0 || signatureOption > 2) return "ERROR";
        if (deliveryLocationType < 0 || deliveryLocationType > 1) return "ERROR";
        if (saturdayDelivery < 0 || saturdayDelivery > 1) return "ERROR";
        if (holdAtLocation < 0 || holdAtLocation > 1) return "ERROR";
        if (fragile < 0 || fragile > 1) return "ERROR";
        if (dangerous < 0 || dangerous > 1) return "ERROR";

        // Base rate per pound (symmetric) per Section 3.1
        BigDecimal baseRate;
        int minZ = Math.min(origin, destination);
        int maxZ = Math.max(origin, destination);
        if (minZ == maxZ) {
            baseRate = new BigDecimal("2.00");
        } else if (minZ == 1 && maxZ == 2) {
            baseRate = new BigDecimal("2.50");
        } else if (minZ == 1 && maxZ == 3) {
            baseRate = new BigDecimal("3.00");
        } else if (minZ == 1 && maxZ == 4) {
            baseRate = new BigDecimal("3.75");
        } else if (minZ == 2 && maxZ == 3) {
            baseRate = new BigDecimal("2.75");
        } else if (minZ == 2 && maxZ == 4) {
            baseRate = new BigDecimal("3.25");
        } else { // 3 and 4
            baseRate = new BigDecimal("2.30");
        }

        BigDecimal baseCost = baseRate.multiply(BigDecimal.valueOf(weight));

        // Delivery speed multiplier per Section 3.2
        BigDecimal deliveryMultiplier;
        switch (deliveryOption) {
            case 1: deliveryMultiplier = new BigDecimal("2.1"); break;
            case 2: deliveryMultiplier = new BigDecimal("1.5"); break;
            case 3: deliveryMultiplier = new BigDecimal("1.2"); break;
            case 4: deliveryMultiplier = new BigDecimal("1.0"); break;
            case 5: deliveryMultiplier = new BigDecimal("0.9"); break;
            case 6: deliveryMultiplier = new BigDecimal("0.8"); break;
            case 7: deliveryMultiplier = new BigDecimal("0.6"); break;
            default: return "ERROR";
        }

        BigDecimal multipliedCost = baseCost.multiply(deliveryMultiplier);

        // Additive surcharges per Section 3.3
        BigDecimal sigSurcharge = BigDecimal.ZERO;
        if (signatureOption == 1) sigSurcharge = new BigDecimal("3.00");
        else if (signatureOption == 2) sigSurcharge = new BigDecimal("10.00");

        BigDecimal locSurcharge = (deliveryLocationType == 1) ? new BigDecimal("10.00") : BigDecimal.ZERO;
        BigDecimal satSurcharge = (saturdayDelivery == 1) ? new BigDecimal("20.00") : BigDecimal.ZERO;
        BigDecimal holdSurcharge = (holdAtLocation == 1) ? new BigDecimal("15.00") : BigDecimal.ZERO;
        BigDecimal fragSurcharge = (fragile == 1) ? new BigDecimal("25.00") : BigDecimal.ZERO;
        BigDecimal dangSurcharge = (dangerous == 1) ? new BigDecimal("50.00") : BigDecimal.ZERO;

        BigDecimal total = multipliedCost
                .add(sigSurcharge)
                .add(locSurcharge)
                .add(satSurcharge)
                .add(holdSurcharge)
                .add(fragSurcharge)
                .add(dangSurcharge);

        return total.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

