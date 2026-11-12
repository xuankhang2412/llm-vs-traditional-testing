package com.payroll;

import java.math.BigDecimal;

public final class PayrollResult {
    private final BigDecimal basePay;
    private final BigDecimal overtimePay;
    private final BigDecimal holidayPay;
    private final BigDecimal grossSalary;

    private final BigDecimal federalTaxRate;
    private final BigDecimal federalTax;
    private final BigDecimal socialSecurity;
    private final BigDecimal medicare;
    private final BigDecimal totalTaxes;

    private final BigDecimal medicalDeduction;
    private final BigDecimal dentalDeduction;
    private final BigDecimal visionDeduction;
    private final BigDecimal pensionDeduction;
    private final BigDecimal unitedWayDeduction;
    private final BigDecimal ltdDeduction;
    private final BigDecimal stdDeduction;
    private final BigDecimal parkingDeduction;
    private final BigDecimal totalDeductions;

    private final BigDecimal netSalary;

    public PayrollResult(
            BigDecimal basePay,
            BigDecimal overtimePay,
            BigDecimal holidayPay,
            BigDecimal grossSalary,
            BigDecimal federalTaxRate,
            BigDecimal federalTax,
            BigDecimal socialSecurity,
            BigDecimal medicare,
            BigDecimal totalTaxes,
            BigDecimal medicalDeduction,
            BigDecimal dentalDeduction,
            BigDecimal visionDeduction,
            BigDecimal pensionDeduction,
            BigDecimal unitedWayDeduction,
            BigDecimal ltdDeduction,
            BigDecimal stdDeduction,
            BigDecimal parkingDeduction,
            BigDecimal totalDeductions,
            BigDecimal netSalary) {
        this.basePay = basePay;
        this.overtimePay = overtimePay;
        this.holidayPay = holidayPay;
        this.grossSalary = grossSalary;
        this.federalTaxRate = federalTaxRate;
        this.federalTax = federalTax;
        this.socialSecurity = socialSecurity;
        this.medicare = medicare;
        this.totalTaxes = totalTaxes;
        this.medicalDeduction = medicalDeduction;
        this.dentalDeduction = dentalDeduction;
        this.visionDeduction = visionDeduction;
        this.pensionDeduction = pensionDeduction;
        this.unitedWayDeduction = unitedWayDeduction;
        this.ltdDeduction = ltdDeduction;
        this.stdDeduction = stdDeduction;
        this.parkingDeduction = parkingDeduction;
        this.totalDeductions = totalDeductions;
        this.netSalary = netSalary;
    }

    public BigDecimal getBasePay() { return basePay; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public BigDecimal getHolidayPay() { return holidayPay; }
    public BigDecimal getGrossSalary() { return grossSalary; }

    public BigDecimal getFederalTaxRate() { return federalTaxRate; }
    public BigDecimal getFederalTax() { return federalTax; }
    public BigDecimal getSocialSecurity() { return socialSecurity; }
    public BigDecimal getMedicare() { return medicare; }
    public BigDecimal getTotalTaxes() { return totalTaxes; }

    public BigDecimal getMedicalDeduction() { return medicalDeduction; }
    public BigDecimal getDentalDeduction() { return dentalDeduction; }
    public BigDecimal getVisionDeduction() { return visionDeduction; }
    public BigDecimal getPensionDeduction() { return pensionDeduction; }
    public BigDecimal getUnitedWayDeduction() { return unitedWayDeduction; }
    public BigDecimal getLtdDeduction() { return ltdDeduction; }
    public BigDecimal getStdDeduction() { return stdDeduction; }
    public BigDecimal getParkingDeduction() { return parkingDeduction; }
    public BigDecimal getTotalDeductions() { return totalDeductions; }

    public BigDecimal getNetSalary() { return netSalary; }

    @Override
    public String toString() {
        return String.format(
                "PayrollResult[gross=%s, fedTax=%s, ss=%s, med=%s, totalDed=%s, net=%s]",
                grossSalary, federalTax, socialSecurity, medicare, totalDeductions, netSalary);
    }
}