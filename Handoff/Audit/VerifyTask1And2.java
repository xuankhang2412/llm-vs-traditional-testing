package audit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VerifyTask1And2 {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("TASK 1 VERIFICATION: ROUNDING OF TIES (HALF_UP AWAY FROM ZERO)");
        System.out.println("===============================================================================");
        testRoundingTies();

        System.out.println("\n===============================================================================");
        System.out.println("TASK 2 VERIFICATION: OUT-OF-DOMAIN BEHAVIOUR ACROSS 4 APPLICATIONS");
        System.out.println("===============================================================================");
        testOutOfDomainBasicPackage();
        testOutOfDomainBasicPayroll();
        testOutOfDomainEnhancedPackage();
        testOutOfDomainEnhancedPayroll();
    }

    private static void testRoundingTies() {
        System.out.println("Testing Java BigDecimal HALF_UP rounding rule:");
        BigDecimal pos = new BigDecimal("12.345").setScale(2, RoundingMode.HALF_UP);
        BigDecimal neg = new BigDecimal("-161.795").setScale(2, RoundingMode.HALF_UP);
        System.out.println("  12.345   -> " + pos + " (Expected: 12.35)");
        System.out.println("  -161.795 -> " + neg + " (Expected: -161.80)");

        System.out.println("\nRunning Basic Payroll Reference Calculator with concrete negative tie input:");
        // Basic Payroll:
        // hourly = 15.00, regularHours = 2.0 -> gross = 30.00
        // fed tax (10%) = 3.00, soc sec (6.2%) = 1.86, medicare (1.45%) = 0.435 -> total tax = 5.295
        // deductions: medical=3 ($100), dental=2 ($20), vision=2 ($15), parking=1 ($51.50) -> total ded = 186.50
        // unrounded net = 30.00 - 5.295 - 186.50 = -161.795
        com.payroll.PayrollCalculator bpCalc = new com.payroll.PayrollCalculator(com.payroll.PayrollDefect.NONE);
        com.payroll.PayrollInput bpInput = new com.payroll.PayrollInput(
                15.00, 2.0, 0.0, 0.0, 3, 2, 2, 0.0, 0.0, 0, 0, 1
        );
        com.payroll.PayrollResult bpResult = bpCalc.calculate(bpInput);
        System.out.println("  Input: hourly=15.00, reg=2.0, ot=0, hol=0, med=3, den=2, vis=2, park=1");
        System.out.println("  Gross Salary: " + bpResult.getGrossSalary());
        System.out.println("  Net Salary:   " + bpResult.getNetSalary() + " (Unrounded was -161.795)");

        System.out.println("\nRunning Enhanced Payroll Reference Calculator with concrete negative tie input:");
        // Enhanced Payroll:
        // hourly = 20.00, regularHours = 1.0 -> gross = 20.00
        // state = 1 (TN <= 20: 2%): fed tax (15%) = 3.00, ss (6.2%) = 1.24, med (1.45%) = 0.29, state (2%) = 0.40 -> taxes = 4.93
        // parking = 1 ($51.50), unitedWay = 10.10 -> deductions = 61.60
        // netBeforeCredits = 20.00 - 4.93 - 61.60 = -46.53
        // unitedWay >= 10.00 -> communityCredit = 0.15 * 10.10 = 1.515
        // unrounded net = -46.53 + 1.515 = -45.015
        // HALF_UP away from zero -> -45.02
        com.payroll.enhanced.PayrollCalculator epCalc = new com.payroll.enhanced.PayrollCalculator(com.payroll.enhanced.Defect.NONE);
        com.payroll.enhanced.PayrollInput epInput = new com.payroll.enhanced.PayrollInput(
                20.00, 1.0, 0.0, 0.0, 1, 0, 0, 0, 10.10, 0.0, 0, 0, 1
        );
        com.payroll.enhanced.PayrollResult epResult = epCalc.calculate(epInput);
        System.out.println("  Input: hourly=20.00, reg=1.0, ot=0, hol=0, state=1, unitedWay=10.10, park=1");
        System.out.println("  Gross Pay: " + epResult.grossPay());
        System.out.println("  Net Pay:   " + epResult.netPay() + " (Unrounded was -45.015)");
    }

    private static void testOutOfDomainBasicPackage() {
        System.out.println("\n--- 1. Basic Package (Project1) ---");
        com.shipping.ShippingCalculator calc = new com.shipping.ShippingCalculator(com.shipping.PackageDefect.NONE);

        // a) Zero numeric value (weight = 0.0)
        runTestCase("Zero numeric (weight=0.0)", () -> {
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(0.0, 1, 2, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).getTotalCost().toPlainString();
        });

        // b) Negative numeric value (weight = -5.0)
        runTestCase("Negative numeric (weight=-5.0)", () -> {
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(-5.0, 1, 2, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).getTotalCost().toPlainString();
        });

        // c) Code outside allowed set (origin = 5)
        runTestCase("Code outside allowed set (origin=5)", () -> {
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(10.0, 5, 2, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).getTotalCost().toPlainString();
        });

        // d) Non-integer value in coded field (parsed from CSV: "1.5" into deliveryOption)
        runTestCase("Non-integer in coded field (\"1.5\")", () -> {
            int deliveryOption = Integer.parseInt("1.5");
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(10.0, 1, 2, deliveryOption, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).getTotalCost().toPlainString();
        });

        // e) Missing value (parsed from CSV: "" into weight)
        runTestCase("Missing value (empty string \"\")", () -> {
            double weight = Double.parseDouble("");
            com.shipping.PackageShippingInput in = new com.shipping.PackageShippingInput(weight, 1, 2, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).getTotalCost().toPlainString();
        });
    }

    private static void testOutOfDomainBasicPayroll() {
        System.out.println("\n--- 2. Basic Payroll (Project2) ---");
        com.payroll.PayrollCalculator calc = new com.payroll.PayrollCalculator(com.payroll.PayrollDefect.NONE);

        // a) Zero numeric value where min > 0 (hourlySalary = 0.0)
        runTestCase("Zero numeric (hourlySalary=0.0)", () -> {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(0.0, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });

        // b) Negative numeric value (hourlySalary = -10.0)
        runTestCase("Negative numeric (hourlySalary=-10.0)", () -> {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(-10.0, 40.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });

        // c) Hours negative (regularHours = -5.0)
        runTestCase("Hours negative (regularHours=-5.0)", () -> {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(20.0, -5.0, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });

        // d) Non-integer in coded field (medicalPlan = "1.5")
        runTestCase("Non-integer in coded field (\"1.5\")", () -> {
            int med = Integer.parseInt("1.5");
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(20.0, 40.0, 0.0, 0.0, med, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });

        // e) Code outside allowed set (medicalPlan = 4)
        runTestCase("Code outside allowed set (medicalPlan=4)", () -> {
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(20.0, 40.0, 0.0, 0.0, 4, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });

        // f) Missing value (empty string "")
        runTestCase("Missing value (empty string \"\")", () -> {
            double hrs = Double.parseDouble("");
            com.payroll.PayrollInput in = new com.payroll.PayrollInput(20.0, hrs, 0.0, 0.0, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).getNetSalary().toPlainString();
        });
    }

    private static void testOutOfDomainEnhancedPackage() {
        System.out.println("\n--- 3. Enhanced Package (Project3) ---");
        com.shipping.enhanced.ShippingCalculator calc = new com.shipping.enhanced.ShippingCalculator(com.shipping.enhanced.Defect.NONE);

        // a) Zero numeric value (weight = 0.0)
        runTestCase("Zero numeric (weight=0.0)", () -> {
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(0.0, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).totalCost().toPlainString();
        });

        // b) Negative numeric value (weight = -2.5)
        runTestCase("Negative numeric (weight=-2.5)", () -> {
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(-2.5, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).totalCost().toPlainString();
        });

        // c) Code outside allowed set (packaging = 9)
        runTestCase("Code outside allowed set (packaging=9)", () -> {
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(10.0, 1, 2, 1, 9, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).totalCost().toPlainString();
        });

        // d) Non-integer in coded field (packaging = "2.5")
        runTestCase("Non-integer in coded field (\"2.5\")", () -> {
            int pkg = Integer.parseInt("2.5");
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(10.0, 1, 2, 1, pkg, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).totalCost().toPlainString();
        });

        // e) Missing value (empty string "")
        runTestCase("Missing value (empty string \"\")", () -> {
            double w = Double.parseDouble("");
            com.shipping.enhanced.ShippingInput in = new com.shipping.enhanced.ShippingInput(w, 1, 2, 1, 1, 0, 0, 0, 0, 0, 0);
            return calc.calculate(in).totalCost().toPlainString();
        });
    }

    private static void testOutOfDomainEnhancedPayroll() {
        System.out.println("\n--- 4. Enhanced Payroll (Project4) ---");
        com.payroll.enhanced.PayrollCalculator calc = new com.payroll.enhanced.PayrollCalculator(com.payroll.enhanced.Defect.NONE);

        // a) Zero numeric value (hourly = 0.0)
        runTestCase("Zero numeric (hourly=0.0)", () -> {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(0.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });

        // b) Negative numeric value (hourly = -15.0)
        runTestCase("Negative numeric (hourly=-15.0)", () -> {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(-15.0, 40.0, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });

        // c) Hours negative (overtimeHours = -3.0)
        runTestCase("Hours negative (overtimeHours=-3.0)", () -> {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(25.0, 40.0, -3.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });

        // d) Non-integer in coded field (state = "1.5")
        runTestCase("Non-integer in coded field (\"1.5\")", () -> {
            int st = Integer.parseInt("1.5");
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(25.0, 40.0, 0.0, 0.0, st, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });

        // e) Code outside allowed set (state = 4)
        runTestCase("Code outside allowed set (state=4)", () -> {
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(25.0, 40.0, 0.0, 0.0, 4, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });

        // f) Missing value (empty string "")
        runTestCase("Missing value (empty string \"\")", () -> {
            double hrs = Double.parseDouble("");
            com.payroll.enhanced.PayrollInput in = new com.payroll.enhanced.PayrollInput(25.0, hrs, 0.0, 0.0, 1, 0, 0, 0, 0.0, 0.0, 0, 0, 0);
            return calc.calculate(in).netPay().toPlainString();
        });
    }

    @FunctionalInterface
    interface TestAction {
        String run() throws Exception;
    }

    private static void runTestCase(String caseName, TestAction action) {
        try {
            String res = action.run();
            System.out.println("  [FAIL] " + caseName + " did NOT produce error! Output was: " + res);
        } catch (NumberFormatException e) {
            System.out.println("  [PASS - ERROR] " + caseName + " -> NumberFormatException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("  [PASS - ERROR] " + caseName + " -> IllegalArgumentException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [PASS - ERROR] " + caseName + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
