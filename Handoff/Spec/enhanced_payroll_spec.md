# Enhanced Payroll Specification
Version 1.1.1

## 1. Overview
The Enhanced Payroll system computes weekly gross pay, federal, state, and payroll taxes, voluntary insurance and benefit deductions, and net pay with applicable credits for hourly employees across three states.

## 2. Input Parameters

| Field Name | Type | Valid Domain | Description |
|---|---|---|---|
| `hourly` | Floating-point | `15.00 <= hourly <= 50.00` | Hourly pay rate in dollars |
| `regularHours` | Floating-point | `>= 0.0` | Regular hours worked in the weekly pay period |
| `overtimeHours` | Floating-point | `>= 0.0` | Overtime hours worked (paid at 1.5x regular pay) |
| `holidayHours` | Floating-point | `>= 0.0` | Holiday hours worked (paid at 2.0x regular pay) |
| `state` | Integer | `1`, `2`, or `3` | State code: 1 = Tennessee, 2 = Arkansas, 3 = Mississippi |
| `medical` | Integer | `0` to `3` | Medical insurance plan choice |
| `dental` | Integer | `0` to `2` | Dental insurance plan choice |
| `vision` | Integer | `0` to `2` | Vision insurance plan choice |
| `unitedWay` | Floating-point | `>= 0.0` | United Way voluntary contribution in dollars |
| `pension` | Floating-point | `>= 0.0` | Pension contribution in dollars (capped at 10% of gross) |
| `ltd` | Integer | `0` or `1` | Long-Term Disability insurance flag |
| `std` | Integer | `0` or `1` | Short-Term Disability insurance flag |
| `parking` | Integer | `0` or `1` | Parking facility permit flag |

Any input values violating the valid domains must result in an error rejection (`ERROR`).

## 3. Rate Schedules & Benefit Plans

### 3.1 Federal Income Tax Brackets
Federal withholding is assessed on weekly gross pay based on the employee's hourly wage:

| Hourly Rate Range | Federal Withholding Rate |
|---|---|
| $15.00 to $20.00 | 10% |
| $20.01 to $40.00 | 22% |
| $40.01 to $50.00 | 35% |

### 3.2 Statutory FICA Payroll Taxes
- **Social Security**: 6.20% of weekly gross pay
- **Medicare**: 1.45% of weekly gross pay

### 3.3 State Income Tax Schedules
State withholding is assessed on weekly gross pay based on state code and the employee's hourly wage:

- **Tennessee (`state = 1`)**:
  - Hourly rate <= $20.00: 0% (no withholding)
  - Hourly rate $20.01 to $35.00: 4%
  - Hourly rate > $35.00: 8%
- **Arkansas (`state = 2`)**:
  - Hourly rate $15.00 to $30.00: 3%
  - Hourly rate > $30.00: 6%
- **Mississippi (`state = 3`)**:
  - Hourly rate $15.00 to $25.00: 2%
  - Hourly rate $25.01 to $40.00: 3%
  - Hourly rate $40.01 to $50.00: 5%

### 3.4 Voluntary Benefit Deductions
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
  - Long-Term Disability (`ltd = 1`): $14.00 (else $0.00)
  - Short-Term Disability (`std = 1`): $9.00 (else $0.00)
- **Parking Facility**:
  - Parking Permit (`parking = 1`): $39.50 (else $0.00)
- **United Way**:
  - Exact requested voluntary contribution amount.
- **Pension Contribution**:
  - Requested pension amount capped at 10% of total weekly gross pay:
    $$\text{pensionDeduction} = \min(\text{pension}, 0.10 \times \text{grossPay})$$

## 4. Special Business & Validation Rules

- **Rule 1 (Low-Wage Overtime Limit)**: If hourly rate is between $15.00 and $20.00 inclusive, overtime hours cannot exceed 10.0 hours (`overtimeHours <= 10.0`). If exceeded, reject the request (`ERROR`).
- **Rule 2 (Overtime Production Bonus)**: If overtime hours strictly exceed 5.0 hours (`overtimeHours > 5.0`), add a taxable $50.00 bonus to gross pay.
- **Rule 3 (Holiday Attendance Bonus)**: If holiday hours strictly exceed 5.0 hours (`holidayHours > 5.0`), add a taxable $75.00 bonus to gross pay.
- **Rule 4 (Medical-Dental Dependency)**: Enrolling in any medical insurance plan (`medical != 0`) requires enrolling in a dental plan (`dental != 0`). If violated, reject the request (`ERROR`).
- **Rule 5 (High-Earner Medical Plan Mandate)**: Employees with an hourly rate of $40.01 or greater (`hourly >= 40.01`) must enroll in medical plan 2 or plan 3 (`medical` must be 2 or 3). Enrolling in plan 0 or plan 1 must be rejected (`ERROR`).
- **Rule 6 (Entry-Wage Health Benefit Subsidy)**: If hourly rate is $20.00 or lower (`hourly <= 20.00`), medical, dental, and vision insurance deductions are fully subsidized by the employer ($0.00 deducted).
- **Rule 7 (Comprehensive Health Wellness Credit)**: If an employee enrolls in medical plan 3, dental plan 2, and vision plan 2 (`medical = 3` and `dental = 2` and `vision = 2`), a wellness credit of +$15.00 is added directly to net pay.
- **Rule 8 (Community Incentive Credit)**: If United Way contribution is $10.00 or greater (`unitedWay >= 10.00`), a credit equal to 15% of the United Way contribution is added directly to net pay.
- **Rule 9 (Disability Hierarchy)**: Long-term disability coverage requires having short-term disability coverage. If `ltd = 1` and `std = 0`, reject the request (`ERROR`).
- **Rule 10 (Dual Disability Credit)**: If both long-term and short-term disability coverages are active (`ltd = 1` and `std = 1`), a credit of +$5.00 is added directly to net pay.

## 5. Calculation Procedure & Order of Operations

1. **Gross Pay Calculation**:
   $$\text{basePay} = \text{hourly} \times \text{regularHours}$$
   $$\text{overtimePay} = \text{hourly} \times \text{overtimeHours} \times 1.5$$
   $$\text{holidayPay} = \text{hourly} \times \text{holidayHours} \times 2.0$$
   $$\text{overtimeBonus} = 50.00 \quad \text{if } \text{overtimeHours} > 5.0 \text{ else } 0.00$$
   $$\text{holidayBonus} = 75.00 \quad \text{if } \text{holidayHours} > 5.0 \text{ else } 0.00$$
   $$\text{grossPay} = \text{basePay} + \text{overtimePay} + \text{holidayPay} + \text{overtimeBonus} + \text{holidayBonus}$$

2. **Taxes Withholding**:
   $$\text{federalTax} = \text{grossPay} \times \text{federalTaxRate}$$
   $$\text{socialSecurity} = \text{grossPay} \times 0.062$$
   $$\text{medicare} = \text{grossPay} \times 0.0145$$
   $$\text{stateTax} = \text{grossPay} \times \text{stateTaxRate}$$
   $$\text{totalTaxes} = \text{federalTax} + \text{socialSecurity} + \text{medicare} + \text{stateTax}$$

3. **Benefit Deductions**:
   Apply Rule 6 subsidy if hourly rate <= $20.00 (zeroing medical, dental, vision deductions).
   $$\text{pensionDeduction} = \min(\text{pension}, 0.10 \times \text{grossPay})$$
   $$\text{totalDeductions} = \text{medicalDeduction} + \text{dentalDeduction} + \text{visionDeduction} + \text{ltdFee} + \text{stdFee} + \text{parkingFee} + \text{unitedWay} + \text{pensionDeduction}$$

4. **Net Pay with Incentive Credits**:
   $$\text{netBeforeCredits} = \text{grossPay} - \text{totalTaxes} - \text{totalDeductions}$$
   $$\text{wellnessCredit} = 15.00 \quad \text{if } (\text{medical}=3 \land \text{dental}=2 \land \text{vision}=2) \text{ else } 0.00$$
   $$\text{communityCredit} = 0.15 \times \text{unitedWay} \quad \text{if } \text{unitedWay} \ge 10.00 \text{ else } 0.00$$
   $$\text{disabilityCredit} = 5.00 \quad \text{if } (\text{ltd}=1 \land \text{std}=1) \text{ else } 0.00$$
   $$\text{netPay} = \text{netBeforeCredits} + \text{wellnessCredit} + \text{communityCredit} + \text{disabilityCredit}$$

5. **Rounding Policy**:
   Both final gross pay and final net pay are rounded to two decimal places using standard half-up rounding.


---

## 6. Clarifications (v1.1)

- **Intermediate Precision and Final Rounding**: All intermediate calculation steps preserve full numerical precision. Only final gross pay and final net pay are rounded to two decimal places using standard half-up rounding.
  - *Worked Example*: Hourly Rate = `24.35`, Regular Hours = `37.5`, State = `1` (Tennessee, 4% rate for $24.35), all other inputs = `0`. Gross pay is $24.35 \times 37.5 = 913.125$. Federal tax (22%) is $200.8875$, Social Security (6.2%) is $56.61375$, Medicare (1.45%) is $13.2403125$, State tax (4%) is $36.525$. Total taxes are $307.2665625$. Net pay is $913.125 - 307.2665625 = 605.8584375$, rounding to `$605.86`.
- **Negative Net Pay**: No lower bound or zero-clamping is applied to net pay. If total taxes and deductions exceed gross earnings, the resulting net pay is negative and is output as a negative amount.
- **Independence of Subsidy and Credits**: The entry-wage subsidy (Rule 6) and the wellness credit (Rule 7) operate independently. An employee with an hourly rate <= $20.00 enrolled in Medical 3, Dental 2, and Vision 2 incurs $0.00 in deductions and still receives the +$15.00 wellness credit.
- **Validation Rule Priority and Multiple Violations**: If an input violates more than one domain or validation constraint, the request is rejected with `ERROR`. The evaluation order of validation checks does not affect the outcome.
- **Out-of-Domain and Malformed Inputs**: Any input value outside the specified domains (such as hourly rate outside $15.00–$50.00, negative hours, negative contributions, state codes outside 1–3, plan codes outside specified integers, non-integer values where integers are expected, or missing values) results in an error rejection (`ERROR`).
- **Rounding of Ties**: Ties at the third decimal are rounded away from zero, e.g. 12.345 -> 12.35 and -161.795 -> -161.80.
