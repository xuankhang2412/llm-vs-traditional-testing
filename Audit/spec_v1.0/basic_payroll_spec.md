# Basic Payroll Specification

## 1. Overview
The Basic Payroll system computes weekly gross salary, tax withholdings, voluntary benefit deductions, and final net pay for hourly employees.

## 2. Input Parameters

| Field Name | Type | Valid Domain | Description |
|---|---|---|---|
| `hourlySalary` | Floating-point | `15.00 <= hourlySalary <= 50.00` | Hourly wage in dollars |
| `regularHours` | Floating-point | `>= 0.0` | Regular hours worked in the workweek |
| `overtimeHours` | Floating-point | `>= 0.0` | Overtime hours worked (paid at 1.5x regular wage) |
| `holidayHours` | Floating-point | `>= 0.0` | Holiday hours worked (paid at 2.0x regular wage) |
| `medicalPlan` | Integer | `0` to `3` | Medical insurance plan choice |
| `dentalPlan` | Integer | `0` to `2` | Dental insurance plan choice |
| `visionPlan` | Integer | `0` to `2` | Vision insurance plan choice |
| `unitedWay` | Floating-point | `>= 0.0` | Fixed dollar contribution to United Way |
| `pension` | Floating-point | `>= 0.0` | Requested pension contribution (subject to cap) |
| `longTermDisability` | Integer | `0` or `1` | Long-term disability insurance flag |
| `shortTermDisability` | Integer | `0` or `1` | Short-term disability insurance flag |
| `parking` | Integer | `0` or `1` | Employer parking permit flag |

Any input values violating the valid domains must result in an error rejection (`ERROR`).

## 3. Rate Tables & Plan Schedules

### 3.1 Federal Income Tax Brackets
Federal withholding is computed against weekly gross salary according to the hourly wage tier:

| Hourly Salary Range | Federal Tax Rate |
|---|---|
| $15.00 to $20.00 | 10% |
| $20.01 to $40.00 | 22% |
| $40.01 to $50.00 | 35% |

### 3.2 Statutory Payroll Taxes (FICA)
- **Social Security Tax**: 6.20% of weekly gross salary
- **Medicare Tax**: 1.45% of weekly gross salary

### 3.3 Voluntary Benefit Deduction Schedules
- **Medical Insurance**:
  - Plan 0 (None): $0.00
  - Plan 1: $50.00
  - Plan 2: $75.00
  - Plan 3: $125.00
- **Dental Insurance**:
  - Plan 0 (None): $0.00
  - Plan 1: $5.00
  - Plan 2: $10.00
- **Vision Insurance**:
  - Plan 0 (None): $0.00
  - Plan 1: $5.00
  - Plan 2: $12.00
- **Disability Coverage**:
  - Long-Term Disability (`longTermDisability = 1`): $14.00 (else $0.00)
  - Short-Term Disability (`shortTermDisability = 1`): $9.00 (else $0.00)
- **Parking Facility Fee**:
  - Parking Permit (`parking = 1`): $39.50 (else $0.00)
- **United Way**:
  - Exact requested contribution amount.
- **Pension Contribution**:
  - Deducted amount is the requested pension contribution capped at 10% of gross salary:
    $$\text{pensionDeduction} = \min(\text{pension}, 0.10 \times \text{grossSalary})$$

## 4. Calculation Procedure & Order of Operations

1. **Gross Salary Calculation**:
   $$\text{basePay} = \text{hourlySalary} \times \text{regularHours}$$
   $$\text{overtimePay} = \text{hourlySalary} \times \text{overtimeHours} \times 1.5$$
   $$\text{holidayPay} = \text{hourlySalary} \times \text{holidayHours} \times 2.0$$
   $$\text{grossSalary} = \text{basePay} + \text{overtimePay} + \text{holidayPay}$$

2. **Tax Withholdings**:
   $$\text{federalTax} = \text{grossSalary} \times \text{federalTaxRate}$$
   $$\text{socialSecurity} = \text{grossSalary} \times 0.062$$
   $$\text{medicare} = \text{grossSalary} \times 0.0145$$
   $$\text{totalTaxes} = \text{federalTax} + \text{socialSecurity} + \text{medicare}$$

3. **Total Deductions**:
   $$\text{totalDeductions} = \text{medicalDeduction} + \text{dentalDeduction} + \text{visionDeduction} + \text{pensionDeduction} + \text{unitedWay} + \text{longTermDisabilityFee} + \text{shortTermDisabilityFee} + \text{parkingFee}$$

4. **Net Salary**:
   $$\text{netSalary} = \text{grossSalary} - \text{totalTaxes} - \text{totalDeductions}$$

5. **Rounding Policy**:
   Both final gross salary and final net salary are rounded to two decimal places using standard half-up rounding.
