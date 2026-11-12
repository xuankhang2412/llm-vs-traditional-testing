package com.payroll;

public final class PayrollInput {
    private final double hourlySalary;
    private final double regularHours;
    private final double overtimeHours;
    private final double holidayHours;
    private final int medicalPlan;
    private final int dentalPlan;
    private final int visionPlan;
    private final double unitedWay;
    private final double pension;
    private final int longTermDisability;
    private final int shortTermDisability;
    private final int parking;

    public PayrollInput(
            double hourlySalary,
            double regularHours,
            double overtimeHours,
            double holidayHours,
            int medicalPlan,
            int dentalPlan,
            int visionPlan,
            double unitedWay,
            double pension,
            int longTermDisability,
            int shortTermDisability,
            int parking) {
        this.hourlySalary = hourlySalary;
        this.regularHours = regularHours;
        this.overtimeHours = overtimeHours;
        this.holidayHours = holidayHours;
        this.medicalPlan = medicalPlan;
        this.dentalPlan = dentalPlan;
        this.visionPlan = visionPlan;
        this.unitedWay = unitedWay;
        this.pension = pension;
        this.longTermDisability = longTermDisability;
        this.shortTermDisability = shortTermDisability;
        this.parking = parking;
    }

    public void validate() {
        validate(PayrollDefect.NONE);
    }

    public void validate(PayrollDefect defect) {
        if (defect == null) {
            defect = PayrollDefect.NONE;
        }

        // Hourly salary validation: 15 <= value <= 50
        if (hourlySalary < 15.0) {
            throw new IllegalArgumentException("Hourly salary must be at least $15.00 (got " + hourlySalary + ")");
        }

        // DEFECT D1 [boundary]: Maximum hourly salary ($50) validation removed
        if (defect == PayrollDefect.D1) {
            // Maximum validation removed
        } else {
            if (hourlySalary > 50.0) {
                throw new IllegalArgumentException("Hourly salary cannot exceed $50.00 (got " + hourlySalary + ")");
            }
        }

        // Hours validation: >= 0
        if (regularHours < 0.0) {
            throw new IllegalArgumentException("Regular hours cannot be negative (got " + regularHours + ")");
        }
        if (overtimeHours < 0.0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative (got " + overtimeHours + ")");
        }
        if (holidayHours < 0.0) {
            throw new IllegalArgumentException("Holiday hours cannot be negative (got " + holidayHours + ")");
        }

        // Benefit plans validation
        if (medicalPlan < 0 || medicalPlan > 3) {
            throw new IllegalArgumentException("Medical plan must be between 0 and 3 (got " + medicalPlan + ")");
        }
        if (dentalPlan < 0 || dentalPlan > 2) {
            throw new IllegalArgumentException("Dental plan must be between 0 and 2 (got " + dentalPlan + ")");
        }
        if (visionPlan < 0 || visionPlan > 2) {
            throw new IllegalArgumentException("Vision plan must be between 0 and 2 (got " + visionPlan + ")");
        }

        // Fixed dollar deductions: >= 0
        if (unitedWay < 0.0) {
            throw new IllegalArgumentException("United Way contribution cannot be negative (got " + unitedWay + ")");
        }
        if (pension < 0.0) {
            throw new IllegalArgumentException("Pension contribution cannot be negative (got " + pension + ")");
        }

        // Disability & parking binary options: 0 or 1
        if (longTermDisability != 0 && longTermDisability != 1) {
            throw new IllegalArgumentException("Long-term disability must be 0 or 1 (got " + longTermDisability + ")");
        }
        if (shortTermDisability != 0 && shortTermDisability != 1) {
            throw new IllegalArgumentException("Short-term disability must be 0 or 1 (got " + shortTermDisability + ")");
        }
        if (parking != 0 && parking != 1) {
            throw new IllegalArgumentException("Parking must be 0 or 1 (got " + parking + ")");
        }
    }

    public double getHourlySalary() { return hourlySalary; }
    public double getRegularHours() { return regularHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getHolidayHours() { return holidayHours; }
    public int getMedicalPlan() { return medicalPlan; }
    public int getDentalPlan() { return dentalPlan; }
    public int getVisionPlan() { return visionPlan; }
    public double getUnitedWay() { return unitedWay; }
    public double getPension() { return pension; }
    public int getLongTermDisability() { return longTermDisability; }
    public int getShortTermDisability() { return shortTermDisability; }
    public int getParking() { return parking; }
}