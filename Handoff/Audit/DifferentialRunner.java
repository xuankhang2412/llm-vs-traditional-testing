package audit;

import spec_impl.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DifferentialRunner {

    public static void main(String[] args) {
        Random rng = new Random(12345);

        System.out.println("=======================================================");
        System.out.println("STARTING DIFFERENTIAL TESTING (500 inputs per app)");
        System.out.println("=======================================================\n");

        testBasicPackage(rng);
        testBasicPayroll(rng);
        testEnhancedPackage(rng);
        testEnhancedPayroll(rng);

        System.out.println("\n=======================================================");
        System.out.println("DIFFERENTIAL TESTING COMPLETE");
        System.out.println("=======================================================");
    }

    private static void testBasicPackage(Random rng) {
        int total = 500;
        int mismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
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
                if (mismatchDetails.size() < 10) {
                    mismatchDetails.add(String.format("Input [w=%.1f, orig=%d, dest=%d, opt=%d, sig=%d, loc=%d, sat=%d, hold=%d, frag=%d, dang=%d] -> Spec='%s', Real='%s'",
                            weight, origin, destination, deliveryOption, signatureOption, deliveryLocationType, saturdayDelivery, holdAtLocation, fragile, dangerous, specOut, realOut));
                }
            }
        }

        System.out.printf("BasicPackage: %d / %d mismatches%n", mismatches, total);
        for (String m : mismatchDetails) {
            System.out.println("  Mismatch: " + m);
        }
    }

    private static void testBasicPayroll(Random rng) {
        int total = 500;
        int mismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
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
                if (mismatchDetails.size() < 10) {
                    mismatchDetails.add(String.format("Input [h=%.2f, reg=%.1f, ot=%.1f, hol=%.1f, med=%d, den=%d, vis=%d, uw=%.2f, pen=%.2f, ltd=%d, std=%d, park=%d] -> Spec='%s', Real='%s'",
                            hourly, regularHours, overtimeHours, holidayHours, medicalPlan, dentalPlan, visionPlan, unitedWay, pension, ltd, std, parking, specOut, realOut));
                }
            }
        }

        System.out.printf("BasicPayroll: %d / %d mismatches%n", mismatches, total);
        for (String m : mismatchDetails) {
            System.out.println("  Mismatch: " + m);
        }
    }

    private static void testEnhancedPackage(Random rng) {
        int total = 500;
        int mismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
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
                if (mismatchDetails.size() < 10) {
                    mismatchDetails.add(String.format("Input [w=%.1f, orig=%d, dest=%d, deliv=%d, pack=%d, sig=%d, loc=%d, sat=%d, hold=%d, frag=%d, dang=%d] -> Spec='%s', Real='%s'",
                            weight, origin, destination, deliveryOption, packaging, signature, locationType, saturday, hold, fragile, dangerous, specOut, realOut));
                }
            }
        }

        System.out.printf("EnhancedPackage: %d / %d mismatches%n", mismatches, total);
        for (String m : mismatchDetails) {
            System.out.println("  Mismatch: " + m);
        }
    }

    private static void testEnhancedPayroll(Random rng) {
        int total = 500;
        int mismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
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
                if (mismatchDetails.size() < 10) {
                    mismatchDetails.add(String.format("Input [h=%.2f, reg=%.1f, ot=%.1f, hol=%.1f, st=%d, med=%d, den=%d, vis=%d, uw=%.2f, pen=%.2f, ltd=%d, std=%d, park=%d] -> Spec='%s', Real='%s'",
                            hourly, regularHours, overtimeHours, holidayHours, state, medical, dental, vision, unitedWay, pension, ltd, std, parking, specOut, realOut));
                }
            }
        }

        System.out.printf("EnhancedPayroll: %d / %d mismatches%n", mismatches, total);
        for (String m : mismatchDetails) {
            System.out.println("  Mismatch: " + m);
        }
    }
}

