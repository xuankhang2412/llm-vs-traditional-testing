package spec_impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpecBasicPayroll {

    public static String calculate(double hourlySalary, double regularHours, double overtimeHours, double holidayHours,
                                   int medicalPlan, int dentalPlan, int visionPlan, double unitedWay,
                                   double pension, int longTermDisability, int shortTermDisability, int parking) {
        // Validation per Spec Section 2
        if (hourlySalary < 15.00 || hourlySalary > 50.00) return "ERROR";
        if (regularHours < 0.0) return "ERROR";
        if (overtimeHours < 0.0) return "ERROR";
        if (holidayHours < 0.0) return "ERROR";
        if (medicalPlan < 0 || medicalPlan > 3) return "ERROR";
        if (dentalPlan < 0 || dentalPlan > 2) return "ERROR";
        if (visionPlan < 0 || visionPlan > 2) return "ERROR";
        if (unitedWay < 0.0) return "ERROR";
        if (pension < 0.0) return "ERROR";
        if (longTermDisability < 0 || longTermDisability > 1) return "ERROR";
        if (shortTermDisability < 0 || shortTermDisability > 1) return "ERROR";
        if (parking < 0 || parking > 1) return "ERROR";

        BigDecimal h = BigDecimal.valueOf(hourlySalary);
        BigDecimal regPay = h.multiply(BigDecimal.valueOf(regularHours));
        BigDecimal otPay = h.multiply(BigDecimal.valueOf(overtimeHours)).multiply(new BigDecimal("1.5"));
        BigDecimal holPay = h.multiply(BigDecimal.valueOf(holidayHours)).multiply(new BigDecimal("2.0"));
        BigDecimal gross = regPay.add(otPay).add(holPay);

        // Federal tax rate per Section 3.1
        BigDecimal fedRate;
        if (hourlySalary <= 20.00) {
            fedRate = new BigDecimal("0.10");
        } else if (hourlySalary <= 40.00) {
            fedRate = new BigDecimal("0.22");
        } else {
            fedRate = new BigDecimal("0.35");
        }

        BigDecimal fedTax = gross.multiply(fedRate);
        BigDecimal ssTax = gross.multiply(new BigDecimal("0.062"));
        BigDecimal medTax = gross.multiply(new BigDecimal("0.0145"));
        BigDecimal totalTaxes = fedTax.add(ssTax).add(medTax);

        // Deductions per Section 3.3
        BigDecimal medDed = BigDecimal.ZERO;
        if (medicalPlan == 1) medDed = new BigDecimal("50.00");
        else if (medicalPlan == 2) medDed = new BigDecimal("75.00");
        else if (medicalPlan == 3) medDed = new BigDecimal("125.00");

        BigDecimal denDed = BigDecimal.ZERO;
        if (dentalPlan == 1) denDed = new BigDecimal("5.00");
        else if (dentalPlan == 2) denDed = new BigDecimal("10.00");

        BigDecimal visDed = BigDecimal.ZERO;
        if (visionPlan == 1) visDed = new BigDecimal("5.00");
        else if (visionPlan == 2) visDed = new BigDecimal("12.00");

        BigDecimal ltdDed = (longTermDisability == 1) ? new BigDecimal("14.00") : BigDecimal.ZERO;
        BigDecimal stdDed = (shortTermDisability == 1) ? new BigDecimal("9.00") : BigDecimal.ZERO;
        BigDecimal parkDed = (parking == 1) ? new BigDecimal("39.50") : BigDecimal.ZERO;
        BigDecimal uwDed = BigDecimal.valueOf(unitedWay);

        BigDecimal pensionReq = BigDecimal.valueOf(pension);
        BigDecimal pensionCap = gross.multiply(new BigDecimal("0.10"));
        BigDecimal pensionDed = pensionReq.min(pensionCap);

        BigDecimal totalDed = medDed.add(denDed).add(visDed).add(ltdDed).add(stdDed).add(parkDed).add(uwDed).add(pensionDed);
        BigDecimal net = gross.subtract(totalTaxes).subtract(totalDed);

        return net.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

