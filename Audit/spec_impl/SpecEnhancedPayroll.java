package spec_impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpecEnhancedPayroll {

    public static String calculate(double hourly, double regularHours, double overtimeHours, double holidayHours,
                                   int state, int medical, int dental, int vision,
                                   double unitedWay, double pension, int ltd, int std, int parking) {
        // Section 2: Input Parameter Validation
        if (hourly < 15.00 || hourly > 50.00) return "ERROR";
        if (regularHours < 0.0) return "ERROR";
        if (overtimeHours < 0.0) return "ERROR";
        if (holidayHours < 0.0) return "ERROR";
        if (state < 1 || state > 3) return "ERROR";
        if (medical < 0 || medical > 3) return "ERROR";
        if (dental < 0 || dental > 2) return "ERROR";
        if (vision < 0 || vision > 2) return "ERROR";
        if (unitedWay < 0.0) return "ERROR";
        if (pension < 0.0) return "ERROR";
        if (ltd < 0 || ltd > 1) return "ERROR";
        if (std < 0 || std > 1) return "ERROR";
        if (parking < 0 || parking > 1) return "ERROR";

        // Section 4 Rules:
        // Rule 1: Low-Wage Overtime Limit ($15-$20 limits overtime <= 10.0)
        if (hourly <= 20.00 && overtimeHours > 10.0) return "ERROR";

        // Rule 4: Medical requires Dental
        if (medical != 0 && dental == 0) return "ERROR";

        // Rule 5: High-Earner Medical Plan Mandate (h >= 40.01 requires medical 2 or 3)
        if (hourly >= 40.01 && (medical != 2 && medical != 3)) return "ERROR";

        // Rule 9: Disability Hierarchy (ltd=1 requires std=1)
        if (ltd == 1 && std == 0) return "ERROR";

        // Step 1: Gross Pay Calculation (Section 5.1)
        BigDecimal h = BigDecimal.valueOf(hourly);
        BigDecimal basePay = h.multiply(BigDecimal.valueOf(regularHours));
        BigDecimal overtimePay = h.multiply(BigDecimal.valueOf(overtimeHours)).multiply(new BigDecimal("1.5"));
        BigDecimal holidayPay = h.multiply(BigDecimal.valueOf(holidayHours)).multiply(new BigDecimal("2.0"));

        BigDecimal otBonus = (overtimeHours > 5.0) ? new BigDecimal("50.00") : BigDecimal.ZERO;
        BigDecimal holBonus = (holidayHours > 5.0) ? new BigDecimal("75.00") : BigDecimal.ZERO;

        BigDecimal grossPay = basePay.add(overtimePay).add(holidayPay).add(otBonus).add(holBonus);

        // Step 2: Taxes Withholding (Section 3.1, 3.2, 3.3 & 5.2)
        // Federal tax rate
        BigDecimal fedRate;
        if (hourly <= 20.00) {
            fedRate = new BigDecimal("0.10");
        } else if (hourly <= 40.00) {
            fedRate = new BigDecimal("0.22");
        } else {
            fedRate = new BigDecimal("0.35");
        }
        BigDecimal fedTax = grossPay.multiply(fedRate);
        BigDecimal ssTax = grossPay.multiply(new BigDecimal("0.062"));
        BigDecimal medTax = grossPay.multiply(new BigDecimal("0.0145"));

        // State tax rate
        BigDecimal stateRate = BigDecimal.ZERO;
        if (state == 1) { // Tennessee
            if (hourly <= 20.00) stateRate = BigDecimal.ZERO;
            else if (hourly <= 35.00) stateRate = new BigDecimal("0.04");
            else stateRate = new BigDecimal("0.08");
        } else if (state == 2) { // Arkansas
            if (hourly <= 30.00) stateRate = new BigDecimal("0.03");
            else stateRate = new BigDecimal("0.06");
        } else if (state == 3) { // Mississippi
            if (hourly <= 25.00) stateRate = new BigDecimal("0.02");
            else if (hourly <= 40.00) stateRate = new BigDecimal("0.03");
            else stateRate = new BigDecimal("0.05");
        }
        BigDecimal stateTax = grossPay.multiply(stateRate);
        BigDecimal totalTaxes = fedTax.add(ssTax).add(medTax).add(stateTax);

        // Step 3: Benefit Deductions (Section 3.4, 5.3 & Rule 6)
        boolean subsidized = (hourly <= 20.00); // Rule 6

        BigDecimal medDed = BigDecimal.ZERO;
        if (!subsidized) {
            if (medical == 1) medDed = new BigDecimal("50.00");
            else if (medical == 2) medDed = new BigDecimal("75.00");
            else if (medical == 3) medDed = new BigDecimal("125.00");
        }

        BigDecimal denDed = BigDecimal.ZERO;
        if (!subsidized) {
            if (dental == 1) denDed = new BigDecimal("5.00");
            else if (dental == 2) denDed = new BigDecimal("10.00");
        }

        BigDecimal visDed = BigDecimal.ZERO;
        if (!subsidized) {
            if (vision == 1) visDed = new BigDecimal("5.00");
            else if (vision == 2) visDed = new BigDecimal("12.00");
        }

        BigDecimal ltdFee = (ltd == 1) ? new BigDecimal("14.00") : BigDecimal.ZERO;
        BigDecimal stdFee = (std == 1) ? new BigDecimal("9.00") : BigDecimal.ZERO;
        BigDecimal parkFee = (parking == 1) ? new BigDecimal("39.50") : BigDecimal.ZERO;
        BigDecimal uwDed = BigDecimal.valueOf(unitedWay);

        BigDecimal pensionReq = BigDecimal.valueOf(pension);
        BigDecimal pensionCap = grossPay.multiply(new BigDecimal("0.10"));
        BigDecimal pensionDed = pensionReq.min(pensionCap);

        BigDecimal totalDeductions = medDed.add(denDed).add(visDed)
                .add(ltdFee).add(stdFee).add(parkFee).add(uwDed).add(pensionDed);

        // Step 4: Net Pay with Incentive Credits (Section 5.4 & Rules 7, 8, 10)
        BigDecimal netBeforeCredits = grossPay.subtract(totalTaxes).subtract(totalDeductions);

        BigDecimal wellnessCredit = BigDecimal.ZERO;
        if (medical == 3 && dental == 2 && vision == 2) {
            wellnessCredit = new BigDecimal("15.00");
        }

        BigDecimal communityCredit = BigDecimal.ZERO;
        if (unitedWay >= 10.00) {
            communityCredit = BigDecimal.valueOf(unitedWay).multiply(new BigDecimal("0.15"));
        }

        BigDecimal disabilityCredit = BigDecimal.ZERO;
        if (ltd == 1 && std == 1) {
            disabilityCredit = new BigDecimal("5.00");
        }

        BigDecimal netPay = netBeforeCredits.add(wellnessCredit).add(communityCredit).add(disabilityCredit);

        return netPay.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

