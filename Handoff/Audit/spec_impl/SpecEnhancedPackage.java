package spec_impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpecEnhancedPackage {

    public static String calculate(double weight, int origin, int destination, int deliveryOption,
                                   int packaging, int signature, int locationType, int saturday,
                                   int hold, int fragile, int dangerous) {
        // Section 2: Input Parameter Range Validation
        if (weight <= 0.0 || weight > 60.0) return "ERROR";
        if (origin < 1 || origin > 4) return "ERROR";
        if (destination < 1 || destination > 4) return "ERROR";
        if (deliveryOption < 1 || deliveryOption > 7) return "ERROR";
        if (packaging < 1 || packaging > 8) return "ERROR";
        if (signature < 0 || signature > 2) return "ERROR";
        if (locationType < 0 || locationType > 1) return "ERROR";
        if (saturday < 0 || saturday > 1) return "ERROR";
        if (hold < 0 || hold > 1) return "ERROR";
        if (fragile < 0 || fragile > 1) return "ERROR";
        if (dangerous < 0 || dangerous > 1) return "ERROR";

        // Section 3.3 & Rules: Packaging Capacity Limits
        if (packaging == 1 && weight > 2.0) return "ERROR";
        if (packaging == 2 && weight > 10.0) return "ERROR";
        if (packaging == 3 && weight > 25.0) return "ERROR";
        if (packaging == 4 && weight > 40.0) return "ERROR";
        if (packaging == 5 && weight > 10.0) return "ERROR";
        if (packaging >= 6 && packaging <= 8 && weight > 60.0) return "ERROR";

        // Section 4 Rules:
        // Rule 4: Business location requires signature 1 or 2
        if (locationType == 0 && signature == 0) return "ERROR";

        // Rule 5: Weight > 50.0 requires signature 1 or 2
        if (weight > 50.0 && signature == 0) return "ERROR";

        // Rule 7: Fragile and dangerous cannot be combined
        if (fragile == 1 && dangerous == 1) return "ERROR";

        // Rule 8: Dangerous requires deliveryOption in {1, 2, 3, 4}
        if (dangerous == 1 && deliveryOption > 4) return "ERROR";

        // Step 1: Base Price (Section 3.1 & 5.1)
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

        BigDecimal price = baseRate.multiply(BigDecimal.valueOf(weight));

        // Step 2: Delivery Option Multiplier (Section 3.2, 5.2, Rule 2)
        BigDecimal multiplier;
        if (deliveryOption == 1) {
            multiplier = (fragile == 1) ? new BigDecimal("2.75") : new BigDecimal("2.1");
        } else if (deliveryOption == 2) {
            multiplier = new BigDecimal("1.5");
        } else if (deliveryOption == 3) {
            multiplier = new BigDecimal("1.2");
        } else if (deliveryOption == 4) {
            multiplier = new BigDecimal("1.0");
        } else if (deliveryOption == 5) {
            multiplier = new BigDecimal("0.9");
        } else if (deliveryOption == 6) {
            multiplier = new BigDecimal("0.8");
        } else { // 7
            multiplier = new BigDecimal("0.6");
        }
        price = price.multiply(multiplier);

        // Step 3: Packaging Tier (Section 3.3 & 5.3)
        if (packaging == 1 || packaging == 2 || packaging == 6) {
            // +$0.00
        } else if (packaging == 3) {
            price = price.add(new BigDecimal("7.00"));
        } else if (packaging == 4) {
            price = price.add(new BigDecimal("18.00"));
        } else if (packaging == 5) {
            price = price.add(new BigDecimal("5.00"));
        } else if (packaging == 7) {
            price = price.multiply(new BigDecimal("1.15"));
        } else if (packaging == 8) {
            price = price.multiply(new BigDecimal("1.30"));
        }

        // Step 4: Additive Adjustments (Section 3.4 & 5.4)
        // Signature
        if (signature == 1) {
            price = price.add(new BigDecimal("3.00"));
        } else if (signature == 2) {
            price = price.add(new BigDecimal("10.00"));
        }

        // Location
        if (locationType == 1) {
            price = price.add(new BigDecimal("10.00"));
        }

        // Saturday (Rule 9: residence zone 4 Saturday fee is $10 instead of $20)
        if (saturday == 1) {
            if (locationType == 1 && destination == 4) {
                price = price.add(new BigDecimal("10.00"));
            } else {
                price = price.add(new BigDecimal("20.00"));
            }
        }

        // Hold (Rule 3: deliveryOption 7 with hold=1 is $0)
        if (hold == 1 && deliveryOption != 7) {
            price = price.add(new BigDecimal("15.00"));
        }

        // Fragile
        if (fragile == 1) {
            price = price.add(new BigDecimal("25.00"));
            // Rule 6: Fragile direct signature extra fee
            if (signature == 2) {
                price = price.add(new BigDecimal("30.00"));
            }
        }

        // Dangerous
        if (dangerous == 1) {
            price = price.add(new BigDecimal("50.00"));
            // Rule 10: Dangerous signature 0 extra fee
            if (signature == 0) {
                price = price.add(new BigDecimal("40.00"));
            }
        }

        // Step 5: Discount Application (Rule 1 & Section 5.5)
        if (origin == destination && (deliveryOption == 1 || deliveryOption == 2 || deliveryOption == 3)) {
            price = price.multiply(new BigDecimal("0.90"));
        }

        // Step 6: Rounding Policy
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

