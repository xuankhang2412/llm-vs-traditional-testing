package com.payroll.enhanced;

/**
 * CLI entry point for Enhanced Payroll Calculator.
 * Reads thirteen positional arguments:
 *   hourly regularHours overtimeHours holidayHours state medical dental vision unitedWay pension ltd std parking
 *
 * Inject a defect with  -Ddefect=D1  (etc.)
 */
public class Main {

    public static void main(String[] args) {
        String defectProp = System.getProperty("defect", "NONE").trim();
        Defect defect = Defect.fromSystemProperty();

        if (args.length < 13) {
            System.out.println("Enhanced Payroll Calculator (Active Defect: " + defect + ")");
            System.out.println("Usage: java [-Ddefect=D1..D20] -cp <classpath> com.payroll.enhanced.Main "
                    + "<hourly> <regularHours> <overtimeHours> <holidayHours> <state> <medical> <dental> <vision> <unitedWay> <pension> <ltd> <std> <parking>");
            System.out.println("Arguments:");
            System.out.println("  hourly        : (double) 15.00 <= h <= 50.00");
            System.out.println("  regularHours  : (double) >= 0");
            System.out.println("  overtimeHours : (double) >= 0 (1.5x pay)");
            System.out.println("  holidayHours  : (double) >= 0 (2.0x pay)");
            System.out.println("  state         : (int) 1=Tennessee, 2=Arkansas, 3=Mississippi");
            System.out.println("  medical       : (int) 0=None, 1=P1($50), 2=P2($75), 3=P3($125)");
            System.out.println("  dental        : (int) 0=None, 1=P1($5), 2=P2($10)");
            System.out.println("  vision        : (int) 0=None, 1=P1($5), 2=P2($12)");
            System.out.println("  unitedWay     : (double) contribution amount >= 0");
            System.out.println("  pension       : (double) contribution amount >= 0 (capped at 10% gross)");
            System.out.println("  ltd           : (int) 0=No, 1=Yes ($14)");
            System.out.println("  std           : (int) 0=No, 1=Yes ($9)");
            System.out.println("  parking       : (int) 0=No, 1=Yes ($39.50)");
            return;
        }

        try {
            double hourly = Double.parseDouble(args[0]);
            double regularHours = Double.parseDouble(args[1]);
            double overtimeHours = Double.parseDouble(args[2]);
            double holidayHours = Double.parseDouble(args[3]);
            int state = Integer.parseInt(args[4]);
            int medical = Integer.parseInt(args[5]);
            int dental = Integer.parseInt(args[6]);
            int vision = Integer.parseInt(args[7]);
            double unitedWay = Double.parseDouble(args[8]);
            double pension = Double.parseDouble(args[9]);
            int ltd = Integer.parseInt(args[10]);
            int std = Integer.parseInt(args[11]);
            int parking = Integer.parseInt(args[12]);

            PayrollInput input = new PayrollInput(
                    hourly, regularHours, overtimeHours, holidayHours, state,
                    medical, dental, vision, unitedWay, pension, ltd, std, parking);

            PayrollCalculator calculator = new PayrollCalculator(defect);
            PayrollResult result = calculator.calculate(input);

            System.out.println("==========================================");
            System.out.println("Enhanced Payroll Calculation Summary");
            System.out.println("Active Defect: " + defect);
            System.out.println("==========================================");
            System.out.println("Gross Pay: $" + result.grossPay());
            System.out.println("Net Pay:   $" + result.netPay());
            System.out.println("==========================================");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

