package com.payroll.enhanced;

/**
 * Value object holding all inputs for an enhanced weekly payroll calculation.
 *
 * @param hourly        Hourly rate in dollars (15.00 <= h <= 50.00)
 * @param regularHours  Regular hours worked (>= 0)
 * @param overtimeHours Overtime hours worked (>= 0)
 * @param holidayHours  Holiday hours worked (>= 0)
 * @param state         State code: 1 = Tennessee, 2 = Arkansas, 3 = Mississippi
 * @param medical       Medical plan: 0 = None, 1 = Plan 1 ($50), 2 = Plan 2 ($75), 3 = Plan 3 ($125)
 * @param dental        Dental plan: 0 = None, 1 = Plan 1 ($5), 2 = Plan 2 ($10)
 * @param vision        Vision plan: 0 = None, 1 = Plan 1 ($5), 2 = Plan 2 ($12)
 * @param unitedWay     United Way contribution in dollars (>= 0)
 * @param pension       Pension contribution in dollars (>= 0, capped at 10% of gross)
 * @param ltd           Long-Term Disability: 0 = No, 1 = Yes ($14)
 * @param std           Short-Term Disability: 0 = No, 1 = Yes ($9)
 * @param parking       Parking deduction: 0 = No, 1 = Yes ($39.50)
 */
public record PayrollInput(
        double hourly,
        double regularHours,
        double overtimeHours,
        double holidayHours,
        int state,
        int medical,
        int dental,
        int vision,
        double unitedWay,
        double pension,
        int ltd,
        int std,
        int parking) {
}

