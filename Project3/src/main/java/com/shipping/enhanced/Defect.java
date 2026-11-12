package com.shipping.enhanced;

/**
 * Enum representing injectable defects for mutation testing in Enhanced Package Shipping.
 * NONE = correct behavior; D1..D20 = seeded faults.
 */
public enum Defect {
    NONE,
    D1,   // [arithmetic] Zone 3-4 rate 2.30 -> 2.50
    D2,   // [arithmetic] Priority Overnight multiplier 1.2 -> 1.5
    D3,   // [conditional] N1 no longer applies to Same Day (option 1)
    D4,   // [boundary] Small Box limit w<=10 becomes w<10
    D5,   // [arithmetic] Indirect signature fee 3 -> 5
    D6,   // [conditional] N4: direct signature no longer accepted for business (only indirect passes)
    D7,   // [conditional] N6 applies to indirect signature instead of direct
    D8,   // [logic] Hold-at-location fee never charged
    D9,   // [conditional] N7 removed (fragile+dangerous allowed)
    D10,  // [conditional] N10 removed
    D11,  // [boundary] N5 threshold weight>50 becomes weight>55
    D12,  // [boundary] Envelope limit w<=2 becomes w<2
    D13,  // [arithmetic] Packaging code 7 multiplier 1.15 -> 1.25
    D14,  // [boundary] Medium Box limit w<=25 becomes w<25
    D15,  // [conditional] N9 applies to destination zone 3 instead of 4
    D16,  // [arithmetic] N1 discount 10% -> 5%
    D17,  // [conditional] N3 exception removed (hold fee charged for option 7)
    D18,  // [conditional] N8 allows deliveryOption 5
    D19,  // [logic] Rate for pair (4,2) falls back to 2.00 while (2,4) stays correct
    D20;  // [logic] Residence fee not charged when Saturday=1

    /**
     * Resolve defect from system property {@code -Ddefect=Dk}.
     */
    public static Defect fromSystemProperty() {
        String prop = System.getProperty("defect", "NONE");
        try {
            return Defect.valueOf(prop.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}

