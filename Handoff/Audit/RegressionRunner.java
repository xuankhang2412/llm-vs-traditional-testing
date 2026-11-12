package audit;

import spec_impl.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class RegressionRunner {

    public static void main(String[] args) {
        Random rng = new Random(54321);

        System.out.println("=======================================================");
        System.out.println("1. DIFFERENTIAL REGRESSION TESTING (500 cases per app)");
        System.out.println("=======================================================\n");

        runDiffBasicPackage(rng);
        runDiffBasicPayroll(rng);
        runDiffEnhancedPackage(rng);
        runDiffEnhancedPayroll(rng);

        System.out.println("\n=======================================================");
        System.out.println("2. SPECIFIC CLARIFICATION VERIFICATION CHECKS");
        System.out.println("=======================================================\n");

        runClarificationChecks();
    }

    private static void runDiffBasicPackage(Random rng) {
        int total = 500;
        int mismatches = 0;
        com.shipping.ShippingCalculator calc = new com.shipping.ShippingCalculator(com.shipping.PackageDefect.NONE);

        for (int i = 0; i < total; i++) {
            boolean makeInvalid = rng.nextDouble() < 0.10;
            double weight = makeInvalid && rng.nextBoolean() ? (rng.nextBoolean() ? -5.0 : 75.0) : Math.round((0.5 + rng.nextDouble() * 59.0) * 10.0) / 10.0;
            int origin = makeInvalid && rng.nextBoolean() ? 0 : 1 + rng.nextInt(4);
            int destination = makeInvalid && rng.nextBoolean() ? 5 : 1 + rng.nextInt(4);
            int deliveryOption = makeInvalid && rng.nextBoolean() ? 8 : 1 + rng.nextInt(7);
            int signatureOption = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            int deliveryLocationType = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int saturdayDelivery = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int holdAtLocation = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int fragile = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int dangerous = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);

            String specOut = SpecBasicPackage.calculate(weight, origin, destination, deliveryOption,
                    signatureOption, deliveryLocationType, saturdayDelivery, holdAtLocation, fragile, dangerous);

            String realOut;
            try {
                com.shipping.PackageShippingInput input = new com.shipping.PackageShippingInput(
                        weight, origin, destination, deliveryOption, signatureOption,
                        deliveryLocationType, saturdayDelivery, holdAtLocation, fragile, dangerous);
                com.shipping.ShippingResult res = calc.calculate(input);
                realOut = res.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception e) {
                realOut = "ERROR";
            }

            if (!specOut.equals(realOut)) {
                mismatches++;
            }
        }
        System.out.printf("BasicPackage Differential: %d / %d mismatches%n", mismatches, total);
    }

    private static void runDiffBasicPayroll(Random rng) {
        int total = 500;
        int mismatches = 0;
        com.payroll.PayrollCalculator calc = new com.payroll.PayrollCalculator(com.payroll.PayrollDefect.NONE);

        for (int i = 0; i < total; i++) {
            boolean makeInvalid = rng.nextDouble() < 0.10;
            double hourly = makeInvalid && rng.nextBoolean() ? (rng.nextBoolean() ? 10.0 : 60.0) : Math.round((15.0 + rng.nextDouble() * 35.0) * 100.0) / 100.0;
            double regularHours = makeInvalid && rng.nextBoolean() ? -5.0 : Math.round(rng.nextDouble() * 50.0 * 10.0) / 10.0;
            double overtimeHours = makeInvalid && rng.nextBoolean() ? -2.0 : Math.round(rng.nextDouble() * 20.0 * 10.0) / 10.0;
            double holidayHours = makeInvalid && rng.nextBoolean() ? -1.0 : Math.round(rng.nextDouble() * 16.0 * 10.0) / 10.0;
            int medicalPlan = makeInvalid && rng.nextBoolean() ? 4 : rng.nextInt(4);
            int dentalPlan = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            int visionPlan = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            double unitedWay = makeInvalid && rng.nextBoolean() ? -10.0 : Math.round(rng.nextDouble() * 30.0 * 100.0) / 100.0;
            double pension = makeInvalid && rng.nextBoolean() ? -50.0 : Math.round(rng.nextDouble() * 200.0 * 100.0) / 100.0;
            int ltd = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int std = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int parking = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);

            String specOut = SpecBasicPayroll.calculate(hourly, regularHours, overtimeHours, holidayHours,
                    medicalPlan, dentalPlan, visionPlan, unitedWay, pension, ltd, std, parking);

            String realOut;
            try {
                com.payroll.PayrollInput input = new com.payroll.PayrollInput(
                        hourly, regularHours, overtimeHours, holidayHours,
                        medicalPlan, dentalPlan, visionPlan, unitedWay, pension, ltd, std, parking);
                com.payroll.PayrollResult res = calc.calculate(input);
                realOut = res.getNetSalary().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception e) {
                realOut = "ERROR";
            }

            if (!specOut.equals(realOut)) {
                mismatches++;
            }
        }
        System.out.printf("BasicPayroll Differential: %d / %d mismatches%n", mismatches, total);
    }

    private static void runDiffEnhancedPackage(Random rng) {
        int total = 500;
        int mismatches = 0;
        com.shipping.enhanced.ShippingCalculator calc = new com.shipping.enhanced.ShippingCalculator(com.shipping.enhanced.Defect.NONE);

        for (int i = 0; i < total; i++) {
            boolean makeInvalid = rng.nextDouble() < 0.10;
            double weight = makeInvalid && rng.nextBoolean() ? (rng.nextBoolean() ? 0.0 : 65.0) : Math.round((0.5 + rng.nextDouble() * 59.0) * 10.0) / 10.0;
            int origin = makeInvalid && rng.nextBoolean() ? 0 : 1 + rng.nextInt(4);
            int destination = makeInvalid && rng.nextBoolean() ? 5 : 1 + rng.nextInt(4);
            int deliveryOption = makeInvalid && rng.nextBoolean() ? 8 : 1 + rng.nextInt(7);
            int packaging = makeInvalid && rng.nextBoolean() ? 9 : 1 + rng.nextInt(8);
            int signature = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            int locationType = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int saturday = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int hold = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int fragile = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int dangerous = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);

            String specOut = SpecEnhancedPackage.calculate(weight, origin, destination, deliveryOption,
                    packaging, signature, locationType, saturday, hold, fragile, dangerous);

            String realOut;
            try {
                com.shipping.enhanced.ShippingInput input = new com.shipping.enhanced.ShippingInput(
                        weight, origin, destination, deliveryOption,
                        packaging, signature, locationType, saturday, hold, fragile, dangerous);
                com.shipping.enhanced.ShippingResult res = calc.calculate(input);
                realOut = res.totalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception e) {
                realOut = "ERROR";
            }

            if (!specOut.equals(realOut)) {
                mismatches++;
            }
        }
        System.out.printf("EnhancedPackage Differential: %d / %d mismatches%n", mismatches, total);
    }

    private static void runDiffEnhancedPayroll(Random rng) {
        int total = 500;
        int mismatches = 0;
        com.payroll.enhanced.PayrollCalculator calc = new com.payroll.enhanced.PayrollCalculator(com.payroll.enhanced.Defect.NONE);

        for (int i = 0; i < total; i++) {
            boolean makeInvalid = rng.nextDouble() < 0.10;
            double hourly = makeInvalid && rng.nextBoolean() ? (rng.nextBoolean() ? 12.0 : 55.0) : Math.round((15.0 + rng.nextDouble() * 35.0) * 100.0) / 100.0;
            double regularHours = makeInvalid && rng.nextBoolean() ? -5.0 : Math.round(rng.nextDouble() * 50.0 * 10.0) / 10.0;
            double overtimeHours = makeInvalid && rng.nextBoolean() ? -2.0 : Math.round(rng.nextDouble() * 20.0 * 10.0) / 10.0;
            double holidayHours = makeInvalid && rng.nextBoolean() ? -1.0 : Math.round(rng.nextDouble() * 16.0 * 10.0) / 10.0;
            int state = makeInvalid && rng.nextBoolean() ? 4 : 1 + rng.nextInt(3);
            int medical = makeInvalid && rng.nextBoolean() ? 4 : rng.nextInt(4);
            int dental = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            int vision = makeInvalid && rng.nextBoolean() ? 3 : rng.nextInt(3);
            double unitedWay = makeInvalid && rng.nextBoolean() ? -10.0 : Math.round(rng.nextDouble() * 30.0 * 100.0) / 100.0;
            double pension = makeInvalid && rng.nextBoolean() ? -50.0 : Math.round(rng.nextDouble() * 200.0 * 100.0) / 100.0;
            int ltd = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int std = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);
            int parking = makeInvalid && rng.nextBoolean() ? 2 : rng.nextInt(2);

            String specOut = SpecEnhancedPayroll.calculate(hourly, regularHours, overtimeHours, holidayHours,
                    state, medical, dental, vision, unitedWay, pension, ltd, std, parking);

            String realOut;
            try {
                com.payroll.enhanced.PayrollInput input = new com.payroll.enhanced.PayrollInput(
                        hourly, regularHours, overtimeHours, holidayHours,
                        state, medical, dental, vision, unitedWay, pension, ltd, std, parking);
                com.payroll.enhanced.PayrollResult res = calc.calculate(input);
                realOut = res.netPay().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception e) {
                realOut = "ERROR";
            }

            if (!specOut.equals(realOut)) {
                mismatches++;
            }
        }
        System.out.printf("EnhancedPayroll Differential: %d / %d mismatches%n", mismatches, total);
    }

    private static void runClarificationChecks() {
        // (1) Negative-net input
        // Basic Payroll negative-net
        com.payroll.PayrollCalculator bpayCalc = new com.payroll.PayrollCalculator(com.payroll.PayrollDefect.NONE);
        String bpayNegInput = "hourly=15.00, reg=2.0, ot=0, hol=0, med=3, den=2, vis=2, uw=0, pen=0, ltd=0, std=0, park=1";
        String bpayNegExpected = "-161.80";
        String bpayNegActual;
        try {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(15.00, 2.0, 0.0, 0.0, 3, 2, 2, 0.0, 0.0, 0, 0, 1);
            bpayNegActual = bpayCalc.calculate(in).getNetSalary().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            bpayNegActual = "ERROR";
        }
        printCheckResult("BasicPayroll Negative Net", bpayNegInput, bpayNegExpected, bpayNegActual);

        // Enhanced Payroll negative-net
        com.payroll.enhanced.PayrollCalculator epayCalc = new com.payroll.enhanced.PayrollCalculator(com.payroll.enhanced.Defect.NONE);
        String epayNegInput = "hourly=22.00, reg=2.0, ot=0, hol=0, state=1, med=3, den=2, vis=2, uw=0, pen=0, ltd=0, std=0, park=1";
        String epayNegExpected = "-142.31";
        String epayNegActual;
        try {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(22.00, 2.0, 0.0, 0.0, 1, 3, 2, 2, 0.0, 0.0, 0, 0, 1);
            epayNegActual = epayCalc.calculate(in).netPay().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            epayNegActual = "ERROR";
        }
        printCheckResult("EnhancedPayroll Negative Net", epayNegInput, epayNegExpected, epayNegActual);

        // (2) Two-rule-violation input
        // Enhanced Package: Rule 4 (business loc 0 with sig 0) + Rule 7 (fragile 1 and dangerous 1)
        com.shipping.enhanced.ShippingCalculator epCalc = new com.shipping.enhanced.ShippingCalculator(com.shipping.enhanced.Defect.NONE);
        String epMultiInput = "weight=10.0, orig=1, dest=2, deliv=2, pack=2, sig=0, loc=0 (Rule 4), sat=0, hold=0, frag=1, dang=1 (Rule 7)";
        String epMultiExpected = "ERROR";
        String epMultiActual;
        try {
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(10.0, 1, 2, 2, 2, 0, 0, 0, 0, 1, 1);
            epMultiActual = epCalc.calculate(in).totalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            epMultiActual = "ERROR";
        }
        printCheckResult("EnhancedPackage Two-Rule Violation", epMultiInput, epMultiExpected, epMultiActual);

        // Enhanced Payroll: Rule 1 (hourly 18.0 with ot 15.0 > 10.0) + Rule 4 (med 1 with den 0)
        String epayMultiInput = "hourly=18.00, reg=40.0, ot=15.0 (Rule 1), hol=0, state=1, med=1, den=0 (Rule 4), vis=0, uw=0, pen=0, ltd=0, std=0, park=0";
        String epayMultiExpected = "ERROR";
        String epayMultiActual;
        try {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(18.00, 40.0, 15.0, 0.0, 1, 1, 0, 0, 0.0, 0.0, 0, 0, 0);
            epayMultiActual = epayCalc.calculate(in).netPay().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            epayMultiActual = "ERROR";
        }
        printCheckResult("EnhancedPayroll Two-Rule Violation", epayMultiInput, epayMultiExpected, epayMultiActual);

        // (3) Out-of-domain input
        // Basic Package: weight -5.0
        com.shipping.ShippingCalculator bpCalc = new com.shipping.ShippingCalculator(com.shipping.PackageDefect.NONE);
        String bpOutDomainInput = "weight=-5.0 (domain (0, 60]), orig=1, dest=1, deliv=1, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0";
        String bpOutDomainExpected = "ERROR";
        String bpOutDomainActual;
        try {
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(-5.0, 1, 1, 1, 0, 0, 0, 0, 0, 0);
            bpOutDomainActual = bpCalc.calculate(in).getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            bpOutDomainActual = "ERROR";
        }
        printCheckResult("BasicPackage Out-of-Domain Input", bpOutDomainInput, bpOutDomainExpected, bpOutDomainActual);

        // Basic Payroll: hourly 10.00 (< 15.00)
        String bpayOutDomainInput = "hourly=10.00 (domain [15, 50]), reg=40.0, ot=0, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, park=0";
        String bpayOutDomainExpected = "ERROR";
        String bpayOutDomainActual;
        try {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(10.00, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            bpayOutDomainActual = bpayCalc.calculate(in).getNetSalary().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            bpayOutDomainActual = "ERROR";
        }
        printCheckResult("BasicPayroll Out-of-Domain Input", bpayOutDomainInput, bpayOutDomainExpected, bpayOutDomainActual);
    }

    private static void printCheckResult(String name, String input, String expected, String actual) {
        boolean pass = expected.equals(actual);
        System.out.println("Check: " + name);
        System.out.println("  Input:    " + input);
        System.out.println("  Expected: " + expected);
        System.out.println("  Actual:   " + actual);
        System.out.println("  Status:   " + (pass ? "MATCH (PASS)" : "MISMATCH (FAIL)"));
        System.out.println();
    }
}

