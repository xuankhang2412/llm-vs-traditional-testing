package com.payroll.enhanced;

/**
 * Enum representing injectable defects for mutation testing in Enhanced Payroll.
 * NONE = correct behavior; D1..D20 = seeded faults.
 */
public enum Defect {
    NONE,
    D1,   // [arithmetic] overtime multiplier 1.5 -> 1.7
    D2,   // [conditional] N3 removed
    D3,   // [arithmetic] federal rate for 20.01-40 changes 22% -> 25%
    D4,   // [logic] Arkansas: no withholding for the lower range (15-30)
    D5,   // [boundary] N5 starts at 40.00 instead of 40.01
    D6,   // [conditional] N6 removed
    D7,   // [arithmetic] Dental plan 2: 10 -> 12
    D8,   // [boundary] N8 minimum 10 -> 20
    D9,   // [conditional] N9 removed
    D10,  // [logic] N10 credit is subtracted instead of added
    D11,  // [boundary] N2 triggers at overtime>=5 instead of >5
    D12,  // [boundary] N1 overtime limit 10 -> 12
    D13,  // [arithmetic] Medicare 1.45% -> 1.5%
    D14,  // [arithmetic] Tennessee upper rate 8% -> 6%
    D15,  // [conditional] N4 removed
    D16,  // [conditional] N7 removed
    D17,  // [boundary] Mississippi middle bracket starts at 25.00 instead of 25.01
    D18,  // [logic] pension cap computed on base pay (h*regular) instead of gross
    D19,  // [conditional] N10 requires LTD OR STD instead of both
    D20;  // [logic] states 2 and 3 are swapped in state-tax lookup

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

