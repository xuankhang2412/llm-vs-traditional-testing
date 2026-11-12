package com.payroll;

public class Main {
    public static void main(String[] args) {
        String defectProp = System.getProperty("defect", "NONE").trim();
        PayrollDefect defect;
        try {
            defect = PayrollDefect.valueOf(defectProp.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown defect: " + defectProp + ". Defaulting to NONE.");
            defect = PayrollDefect.NONE;
        }

        if (args.length < 12) {
            System.out.println("Payroll Calculator (Active Defect: " + defect + ")");
            System.out.println("Usage: java [-Ddefect=D1..D10] -cp <classpath> com.payroll.Main <hourly> <regHours> <otHours> <holHours> <medPlan> <denPlan> <visPlan> <unitedWay> <pension> <ltd> <std> <parking>");
            System.out.println("Arguments:");
            System.out.println("  hourly    : (double) 15.00 <= value <= 50.00");
            System.out.println("  regHours  : (double) regular hours >= 0");
            System.out.println("  otHours   : (double) overtime hours >= 0");
            System.out.println("  holHours  : (double) holiday hours >= 0");
            System.out.println("  medPlan   : (int) 0=None, 1=P1($50), 2=P2($75), 3=P3($125)");
            System.out.println("  denPlan   : (int) 0=None, 1=P1($5), 2=P2($10)");
            System.out.println("  visPlan   : (int) 0=None, 1=P1($5), 2=P2($12)");
            System.out.println("  unitedWay : (double) fixed dollar amount >= 0");
            System.out.println("  pension   : (double) fixed dollar amount >= 0 (capped at 10% gross)");
            System.out.println("  ltd       : (int) 0 or 1 ($14)");
            System.out.println("  std       : (int) 0 or 1 ($9)");
            System.out.println("  parking   : (int) 0 or 1 ($39.50)");
            return;
        }

        try {
            double hourly = Double.parseDouble(args[0]);
            double regHours = Double.parseDouble(args[1]);
            double otHours = Double.parseDouble(args[2]);
            double holHours = Double.parseDouble(args[3]);
            int medPlan = Integer.parseInt(args[4]);
            int denPlan = Integer.parseInt(args[5]);
            int visPlan = Integer.parseInt(args[6]);
            double unitedWay = Double.parseDouble(args[7]);
            double pension = Double.parseDouble(args[8]);
            int ltd = Integer.parseInt(args[9]);
            int std = Integer.parseInt(args[10]);
            int parking = Integer.parseInt(args[11]);

            PayrollInput input = new PayrollInput(
                    hourly, regHours, otHours, holHours, medPlan, denPlan,
                    visPlan, unitedWay, pension, ltd, std, parking);

            PayrollCalculator calculator = new PayrollCalculator(defect);
            PayrollResult result = calculator.calculate(input);

            System.out.println("==========================================");
            System.out.println("Payroll Calculation Summary");
            System.out.println("Active Defect: " + defect);
            System.out.println("==========================================");
            System.out.println("Gross Salary: $" + result.getGrossSalary());
            System.out.println("  - Base Pay: $" + result.getBasePay());
            System.out.println("  - Overtime Pay: $" + result.getOvertimePay());
            System.out.println("  - Holiday Pay: $" + result.getHolidayPay());
            System.out.println("Taxes:");
            System.out.println("  - Federal Tax (Rate " + result.getFederalTaxRate() + "): $" + result.getFederalTax());
            System.out.println("  - Social Security: $" + result.getSocialSecurity());
            System.out.println("  - Medicare: $" + result.getMedicare());
            System.out.println("  - Total Taxes: $" + result.getTotalTaxes());
            System.out.println("Deductions:");
            System.out.println("  - Medical: $" + result.getMedicalDeduction());
            System.out.println("  - Dental: $" + result.getDentalDeduction());
            System.out.println("  - Vision: $" + result.getVisionDeduction());
            System.out.println("  - Pension: $" + result.getPensionDeduction());
            System.out.println("  - United Way: $" + result.getUnitedWayDeduction());
            System.out.println("  - LTD: $" + result.getLtdDeduction());
            System.out.println("  - STD: $" + result.getStdDeduction());
            System.out.println("  - Parking: $" + result.getParkingDeduction());
            System.out.println("  - Total Deductions: $" + result.getTotalDeductions());
            System.out.println("------------------------------------------");
            System.out.println("Net Salary: $" + result.getNetSalary());
            System.out.println("==========================================");
        } catch (IllegalArgumentException e) {
            System.err.println("Validation / Calculation Error: " + e.getMessage());
            System.exit(1);
        }
    }
}