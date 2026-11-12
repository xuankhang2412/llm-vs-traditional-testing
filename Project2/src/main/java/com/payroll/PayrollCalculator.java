package com.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PayrollCalculator {
    private final PayrollDefect defect;

    public PayrollCalculator() {
        this(PayrollDefect.NONE);
    }

    public PayrollCalculator(PayrollDefect defect) {
        this.defect = (defect != null) ? defect : PayrollDefect.NONE;
    }

    public PayrollDefect getDefect() {
        return defect;
    }

    public PayrollResult calculate(PayrollInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        // Spec A2 Validation
        input.validate(defect);

        BigDecimal hourly = BigDecimal.valueOf(input.getHourlySalary());

        // Spec A2 Step 1 - Gross pay calculation
        BigDecimal basePay = hourly.multiply(BigDecimal.valueOf(input.getRegularHours()));

        double otHours = input.getOvertimeHours();
        // DEFECT D10 [logic]: Overtime hours ignored (treated as 0)
        if (defect == PayrollDefect.D10) {
            otHours = 0.0;
        }

        BigDecimal otMultiplier = BigDecimal.valueOf(1.5);
        // DEFECT D3 [arithmetic]: Overtime multiplier changed from 1.5 to 1.25
        if (defect == PayrollDefect.D3) {
            otMultiplier = BigDecimal.valueOf(1.25);
        }

        BigDecimal overtimePay = hourly.multiply(BigDecimal.valueOf(otHours)).multiply(otMultiplier);
        BigDecimal holidayPay = hourly.multiply(BigDecimal.valueOf(input.getHolidayHours())).multiply(BigDecimal.valueOf(2.0));
        BigDecimal gross = basePay.add(overtimePay).add(holidayPay);

        // Spec A2 Step 2 - Taxes
        BigDecimal fedRate = getFederalTaxRate(input.getHourlySalary());
        BigDecimal federalTax = gross.multiply(fedRate);

        BigDecimal ssRate = BigDecimal.valueOf(0.062);
        // DEFECT D2 [arithmetic]: Social Security rate changed from 0.062 to 0.05
        if (defect == PayrollDefect.D2) {
            ssRate = BigDecimal.valueOf(0.05);
        }
        BigDecimal socialSecurity = gross.multiply(ssRate);

        BigDecimal medicare = gross.multiply(BigDecimal.valueOf(0.0145));
        BigDecimal totalTaxes = federalTax.add(socialSecurity).add(medicare);

        // Spec A2 Step 3 - Deductions
        BigDecimal medicalDeduction = getMedicalDeduction(input.getMedicalPlan());
        BigDecimal dentalDeduction = getDentalDeduction(input.getDentalPlan());
        BigDecimal visionDeduction = getVisionDeduction(input.getVisionPlan());

        BigDecimal maxPension = gross.multiply(BigDecimal.valueOf(0.10));
        BigDecimal requestedPension = BigDecimal.valueOf(input.getPension());
        BigDecimal pensionDeduction = requestedPension.min(maxPension);

        BigDecimal unitedWayDeduction = BigDecimal.valueOf(input.getUnitedWay());
        // DEFECT D9 [logic]: United Way contribution ignored
        if (defect == PayrollDefect.D9) {
            unitedWayDeduction = BigDecimal.ZERO;
        }

        BigDecimal ltdDeduction = (input.getLongTermDisability() == 1) ? BigDecimal.valueOf(14.00) : BigDecimal.ZERO;
        BigDecimal stdDeduction = (input.getShortTermDisability() == 1) ? BigDecimal.valueOf(9.00) : BigDecimal.ZERO;
        BigDecimal parkingDeduction = (input.getParking() == 1) ? BigDecimal.valueOf(39.50) : BigDecimal.ZERO;

        BigDecimal totalDeductions = medicalDeduction
                .add(dentalDeduction)
                .add(visionDeduction)
                .add(pensionDeduction)
                .add(unitedWayDeduction)
                .add(ltdDeduction)
                .add(stdDeduction)
                .add(parkingDeduction);

        // Spec A2 Step 4 - Net pay
        BigDecimal rawNet = gross.subtract(totalTaxes).subtract(totalDeductions);
        BigDecimal net = rawNet.setScale(2, RoundingMode.HALF_UP);

        return new PayrollResult(
                basePay,
                overtimePay,
                holidayPay,
                gross,
                fedRate,
                federalTax,
                socialSecurity,
                medicare,
                totalTaxes,
                medicalDeduction,
                dentalDeduction,
                visionDeduction,
                pensionDeduction,
                unitedWayDeduction,
                ltdDeduction,
                stdDeduction,
                parkingDeduction,
                totalDeductions,
                net);
    }

    private BigDecimal getFederalTaxRate(double hourlySalary) {
        double bracket2End = 40.00;
        // DEFECT D4 [boundary]: 22% bracket ends at $35 instead of $40 (so $35.01-$40 gets 35%)
        if (defect == PayrollDefect.D4) {
            bracket2End = 35.00;
        }

        if (hourlySalary <= 20.00) {
            return BigDecimal.valueOf(0.10);
        } else if (hourlySalary <= bracket2End) {
            return BigDecimal.valueOf(0.22);
        } else {
            return BigDecimal.valueOf(0.35);
        }
    }

    private BigDecimal getMedicalDeduction(int medicalPlan) {
        // DEFECT D8 [logic]: Medical deduction uses the LTD amount ($14) instead of plan amounts
        if (defect == PayrollDefect.D8) {
            if (medicalPlan > 0) {
                return BigDecimal.valueOf(14.00);
            }
            return BigDecimal.ZERO;
        }

        switch (medicalPlan) {
            case 1:
                return BigDecimal.valueOf(50.00);
            case 2:
                // DEFECT D6 [logic]: Medical Plan 2 and Plan 3 amounts swapped
                if (defect == PayrollDefect.D6) {
                    return BigDecimal.valueOf(125.00);
                } else {
                    return BigDecimal.valueOf(75.00);
                }
            case 3:
                // DEFECT D6 [logic]: Medical Plan 2 and Plan 3 amounts swapped
                if (defect == PayrollDefect.D6) {
                    return BigDecimal.valueOf(75.00);
                } else {
                    return BigDecimal.valueOf(125.00);
                }
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal getDentalDeduction(int dentalPlan) {
        // DEFECT D7 [logic]: Dental deduction uses the medical plan amounts
        if (defect == PayrollDefect.D7) {
            switch (dentalPlan) {
                case 1:
                    return BigDecimal.valueOf(50.00); // Medical P1 amount
                case 2:
                    return BigDecimal.valueOf(75.00); // Medical P2 amount
                default:
                    return BigDecimal.ZERO;
            }
        }

        switch (dentalPlan) {
            case 1:
                return BigDecimal.valueOf(5.00);
            case 2:
                return BigDecimal.valueOf(10.00);
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal getVisionDeduction(int visionPlan) {
        // DEFECT D5 [logic]: Vision insurance deduction never applied
        if (defect == PayrollDefect.D5) {
            return BigDecimal.ZERO;
        }

        switch (visionPlan) {
            case 1:
                return BigDecimal.valueOf(5.00);
            case 2:
                return BigDecimal.valueOf(12.00);
            default:
                return BigDecimal.ZERO;
        }
    }
}