package com.payroll.enhanced;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates weekly gross and net pay based on hourly rate, hours worked,
 * state tax brackets, benefit deductions, and credits.
 * Supports defect injection for mutation testing.
 */
public class PayrollCalculator {

    private final Defect defect;

    public PayrollCalculator() {
        this(Defect.fromSystemProperty());
    }

    public PayrollCalculator(Defect defect) {
        this.defect = (defect != null) ? defect : Defect.NONE;
    }

    public Defect getDefect() {
        return defect;
    }

    /**
     * @throws IllegalArgumentException if any input violates domain constraints or business rules
     */
    public PayrollResult calculate(PayrollInput in) {
        if (in == null) {
            throw new IllegalArgumentException("PayrollInput cannot be null");
        }

        /* ── 1. Basic validation ──────────────────────────── */
        if (in.hourly() < 15.00 || in.hourly() > 50.00) {
            throw new IllegalArgumentException("Hourly rate must be between $15.00 and $50.00 (got " + in.hourly() + ")");
        }
        if (in.regularHours() < 0.0) {
            throw new IllegalArgumentException("Regular hours cannot be negative (got " + in.regularHours() + ")");
        }
        if (in.overtimeHours() < 0.0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative (got " + in.overtimeHours() + ")");
        }
        if (in.holidayHours() < 0.0) {
            throw new IllegalArgumentException("Holiday hours cannot be negative (got " + in.holidayHours() + ")");
        }
        if (in.state() < 1 || in.state() > 3) {
            throw new IllegalArgumentException("State must be 1 (Tennessee), 2 (Arkansas), or 3 (Mississippi) (got " + in.state() + ")");
        }
        if (in.medical() < 0 || in.medical() > 3) {
            throw new IllegalArgumentException("Medical plan must be 0-3 (got " + in.medical() + ")");
        }
        if (in.dental() < 0 || in.dental() > 2) {
            throw new IllegalArgumentException("Dental plan must be 0-2 (got " + in.dental() + ")");
        }
        if (in.vision() < 0 || in.vision() > 2) {
            throw new IllegalArgumentException("Vision plan must be 0-2 (got " + in.vision() + ")");
        }
        if (in.unitedWay() < 0.0) {
            throw new IllegalArgumentException("United Way contribution cannot be negative (got " + in.unitedWay() + ")");
        }
        if (in.pension() < 0.0) {
            throw new IllegalArgumentException("Pension contribution cannot be negative (got " + in.pension() + ")");
        }
        if (in.ltd() != 0 && in.ltd() != 1) {
            throw new IllegalArgumentException("LTD option must be 0 or 1 (got " + in.ltd() + ")");
        }
        if (in.std() != 0 && in.std() != 1) {
            throw new IllegalArgumentException("STD option must be 0 or 1 (got " + in.std() + ")");
        }
        if (in.parking() != 0 && in.parking() != 1) {
            throw new IllegalArgumentException("Parking option must be 0 or 1 (got " + in.parking() + ")");
        }

        /* ── N1: Low hourly overtime cap ─────────────────── */
        // DEFECT D12 [boundary]: N1 overtime limit 10 -> 12
        double n1OtLimit = (defect == Defect.D12) ? 12.0 : 10.0;
        if (in.hourly() >= 15.00 && in.hourly() <= 20.00) {
            if (in.overtimeHours() > n1OtLimit) {
                throw new IllegalArgumentException("Hourly rate $15-20 limits overtime to " + n1OtLimit + " hours (got " + in.overtimeHours() + ")");
            }
        }

        /* ── N4: Medical enrollment requires dental ───────── */
        // DEFECT D15 [conditional]: N4 removed
        if (defect != Defect.D15) {
            if (in.medical() != 0 && in.dental() == 0) {
                throw new IllegalArgumentException("Medical enrollment requires dental enrollment (N4)");
            }
        }

        /* ── N5: High earners medical plan restriction ────── */
        // DEFECT D5 [boundary]: N5 starts at 40.00 instead of 40.01
        double n5Threshold = (defect == Defect.D5) ? 40.00 : 40.01;
        if (in.hourly() >= n5Threshold) {
            if (in.medical() != 2 && in.medical() != 3) {
                throw new IllegalArgumentException("Hourly rate >= $" + n5Threshold + " requires medical plan 2 or 3 (got " + in.medical() + ")");
            }
        }

        /* ── N9: LTD requires STD ─────────────────────────── */
        // DEFECT D9 [conditional]: N9 removed
        if (defect != Defect.D9) {
            if (in.ltd() == 1 && in.std() == 0) {
                throw new IllegalArgumentException("Long-term disability requires short-term disability (N9)");
            }
        }

        /* ── 2. Gross Pay Calculation ─────────────────────── */
        BigDecimal hRate = BigDecimal.valueOf(in.hourly());
        BigDecimal basePay = hRate.multiply(BigDecimal.valueOf(in.regularHours()));

        // DEFECT D1 [arithmetic]: overtime multiplier 1.5 -> 1.7
        BigDecimal otMultiplier = (defect == Defect.D1) ? new BigDecimal("1.7") : new BigDecimal("1.5");
        BigDecimal overtimePay = hRate.multiply(BigDecimal.valueOf(in.overtimeHours())).multiply(otMultiplier);

        BigDecimal holidayPay = hRate.multiply(BigDecimal.valueOf(in.holidayHours())).multiply(new BigDecimal("2.0"));

        // N2 bonus: overtime > 5 -> +$50 to gross
        // DEFECT D11 [boundary]: N2 triggers at overtime>=5 instead of >5
        boolean n2BonusTrigger = (defect == Defect.D11) ? (in.overtimeHours() >= 5.0) : (in.overtimeHours() > 5.0);
        BigDecimal otBonus = n2BonusTrigger ? new BigDecimal("50.00") : BigDecimal.ZERO;

        // N3 bonus: holiday > 5 -> +$75 to gross
        // DEFECT D2 [conditional]: N3 removed
        boolean n3BonusTrigger = (defect != Defect.D2) && (in.holidayHours() > 5.0);
        BigDecimal holidayBonus = n3BonusTrigger ? new BigDecimal("75.00") : BigDecimal.ZERO;

        BigDecimal gross = basePay.add(overtimePay).add(holidayPay).add(otBonus).add(holidayBonus);

        /* ── 3. Federal Tax ──────────────────────────────── */
        // DEFECT D3 [arithmetic]: federal rate for 20.01-40 changes 22% -> 25%
        BigDecimal fedTaxRate;
        if (in.hourly() <= 20.00) {
            fedTaxRate = new BigDecimal("0.10");
        } else if (in.hourly() <= 40.00) {
            fedTaxRate = (defect == Defect.D3) ? new BigDecimal("0.25") : new BigDecimal("0.22");
        } else {
            fedTaxRate = new BigDecimal("0.35");
        }
        BigDecimal fedTax = gross.multiply(fedTaxRate);

        /* ── 4. Social Security & Medicare (FICA) ────────── */
        BigDecimal ssTax = gross.multiply(new BigDecimal("0.062"));

        // DEFECT D13 [arithmetic]: Medicare 1.45% -> 1.5%
        BigDecimal medTaxRate = (defect == Defect.D13) ? new BigDecimal("0.015") : new BigDecimal("0.0145");
        BigDecimal medicareTax = gross.multiply(medTaxRate);

        /* ── 5. State Tax ────────────────────────────────── */
        // DEFECT D20 [logic]: states 2 and 3 are swapped in state-tax lookup
        int effectiveState = in.state();
        if (defect == Defect.D20) {
            if (effectiveState == 2) effectiveState = 3;
            else if (effectiveState == 3) effectiveState = 2;
        }

        BigDecimal stateTaxRate;
        if (effectiveState == 1) {
            // Tennessee: h<=20 none; 20.01-35 -> 4%; >35 -> 8%
            // DEFECT D14 [arithmetic]: Tennessee upper rate 8% -> 6%
            if (in.hourly() <= 20.00) {
                stateTaxRate = BigDecimal.ZERO;
            } else if (in.hourly() <= 35.00) {
                stateTaxRate = new BigDecimal("0.04");
            } else {
                stateTaxRate = (defect == Defect.D14) ? new BigDecimal("0.06") : new BigDecimal("0.08");
            }
        } else if (effectiveState == 2) {
            // Arkansas: 15-30 -> 3%; >30 -> 6%
            // DEFECT D4 [logic]: Arkansas: no withholding for lower range (15-30)
            if (in.hourly() <= 30.00) {
                stateTaxRate = (defect == Defect.D4) ? BigDecimal.ZERO : new BigDecimal("0.03");
            } else {
                stateTaxRate = new BigDecimal("0.06");
            }
        } else {
            // Mississippi: 15-25 -> 2%; 25.01-40 -> 3%; 40.01-50 -> 5%
            // DEFECT D17 [boundary]: Mississippi middle bracket starts at 25.00 instead of 25.01
            if (defect == Defect.D17) {
                if (in.hourly() < 25.00) {
                    stateTaxRate = new BigDecimal("0.02");
                } else if (in.hourly() <= 40.00) {
                    stateTaxRate = new BigDecimal("0.03");
                } else {
                    stateTaxRate = new BigDecimal("0.05");
                }
            } else {
                if (in.hourly() <= 25.00) {
                    stateTaxRate = new BigDecimal("0.02");
                } else if (in.hourly() <= 40.00) {
                    stateTaxRate = new BigDecimal("0.03");
                } else {
                    stateTaxRate = new BigDecimal("0.05");
                }
            }
        }
        BigDecimal stateTax = gross.multiply(stateTaxRate);

        /* ── 6. Deductions ───────────────────────────────── */
        BigDecimal medDeduction = switch (in.medical()) {
            case 1 -> new BigDecimal("50.00");
            case 2 -> new BigDecimal("75.00");
            case 3 -> new BigDecimal("125.00");
            default -> BigDecimal.ZERO;
        };

        // DEFECT D7 [arithmetic]: Dental plan 2: 10 -> 12
        BigDecimal denDeduction = switch (in.dental()) {
            case 1 -> new BigDecimal("5.00");
            case 2 -> (defect == Defect.D7) ? new BigDecimal("12.00") : new BigDecimal("10.00");
            default -> BigDecimal.ZERO;
        };

        BigDecimal visDeduction = switch (in.vision()) {
            case 1 -> new BigDecimal("5.00");
            case 2 -> new BigDecimal("12.00");
            default -> BigDecimal.ZERO;
        };

        // N6: if h <= 20.00 then medical, dental, and vision deductions are 0
        // DEFECT D6 [conditional]: N6 removed
        if (defect != Defect.D6 && in.hourly() <= 20.00) {
            medDeduction = BigDecimal.ZERO;
            denDeduction = BigDecimal.ZERO;
            visDeduction = BigDecimal.ZERO;
        }

        BigDecimal ltdDeduction = (in.ltd() == 1) ? new BigDecimal("14.00") : BigDecimal.ZERO;
        BigDecimal stdDeduction = (in.std() == 1) ? new BigDecimal("9.00") : BigDecimal.ZERO;
        BigDecimal parkDeduction = (in.parking() == 1) ? new BigDecimal("39.50") : BigDecimal.ZERO;
        BigDecimal uwDeduction = BigDecimal.valueOf(in.unitedWay());

        // Pension: capped at 10%
        // DEFECT D18 [logic]: pension cap computed on base pay (h*regular) instead of gross
        BigDecimal pensionBasis = (defect == Defect.D18) ? basePay : gross;
        BigDecimal maxPension = pensionBasis.multiply(new BigDecimal("0.10"));
        BigDecimal requestedPension = BigDecimal.valueOf(in.pension());
        BigDecimal pensionDeduction = requestedPension.min(maxPension);

        BigDecimal totalDeductions = medDeduction
                .add(denDeduction)
                .add(visDeduction)
                .add(ltdDeduction)
                .add(stdDeduction)
                .add(parkDeduction)
                .add(uwDeduction)
                .add(pensionDeduction);

        /* ── 7. Net Pay before credits ───────────────────── */
        BigDecimal net = gross
                .subtract(fedTax)
                .subtract(stateTax)
                .subtract(ssTax)
                .subtract(medicareTax)
                .subtract(totalDeductions);

        /* ── 8. Credits (N7, N8, N10) ────────────────────── */
        // N7 credit: medical=3 and dental=2 and vision=2 -> +$15 to net
        // DEFECT D16 [conditional]: N7 removed
        if (defect != Defect.D16) {
            if (in.medical() == 3 && in.dental() == 2 && in.vision() == 2) {
                net = net.add(new BigDecimal("15.00"));
            }
        }

        // N8 credit: unitedWay >= 10 -> +15% of unitedWay to net
        // DEFECT D8 [boundary]: N8 minimum 10 -> 20
        double n8Min = (defect == Defect.D8) ? 20.0 : 10.0;
        if (in.unitedWay() >= n8Min) {
            BigDecimal uwCredit = BigDecimal.valueOf(in.unitedWay()).multiply(new BigDecimal("0.15"));
            net = net.add(uwCredit);
        }

        // N10 credit: LTD=1 and STD=1 -> +$5 to net
        // DEFECT D19 [conditional]: N10 requires LTD OR STD instead of both
        // DEFECT D10 [logic]: N10 credit is subtracted instead of added
        boolean n10Trigger = (defect == Defect.D19)
                ? (in.ltd() == 1 || in.std() == 1)
                : (in.ltd() == 1 && in.std() == 1);
        if (n10Trigger) {
            if (defect == Defect.D10) {
                net = net.subtract(new BigDecimal("5.00"));
            } else {
                net = net.add(new BigDecimal("5.00"));
            }
        }

        /* ── 9. Final Rounding ────────────────────────────── */
        BigDecimal grossFinal = gross.setScale(2, RoundingMode.HALF_UP);
        BigDecimal netFinal = net.setScale(2, RoundingMode.HALF_UP);

        return new PayrollResult(grossFinal, netFinal);
    }
}

