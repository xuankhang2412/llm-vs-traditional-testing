# Benchmark Testbed Specifications (v1.1.1)

This document consolidates the complete, pristine functional specifications and CSV schema definitions for the four benchmark applications.

## Table of Contents

- [1. BasicPackage Specification (v1.1.1)](#1-basicpackage-specification-v1-1-1)
- [2. BasicPayroll Specification (v1.1.1)](#2-basicpayroll-specification-v1-1-1)
- [3. EnhancedPackage Specification (v1.1.1)](#3-enhancedpackage-specification-v1-1-1)
- [4. EnhancedPayroll Specification (v1.1.1)](#4-enhancedpayroll-specification-v1-1-1)
- [5. CSV Format & Oracular Standards](#5-csv-format-oracular-standards)

---

## 1. BasicPackage Specification (v1.1.1)

# Basic Package Shipping Specification
Version 1.1.1

## 1. Overview
The Basic Package Shipping system calculates shipping costs for domestic parcel deliveries based on parcel weight, origin and destination zones, service speed options, and optional value-added surcharges.

## 2. Input Parameters

| Field Name | Type | Valid Domain | Description |
|---|---|---|---|
| `weight` | Floating-point | `0.0 < weight <= 60.0` | Parcel weight in pounds (lbs) |
| `origin` | Integer | `1` to `4` | Origin domestic zone |
| `destination` | Integer | `1` to `4` | Destination domestic zone |
| `deliveryOption` | Integer | `1` to `7` | Service delivery speed code |
| `signatureOption` | Integer | `0` to `2` | Signature requirement code |
| `deliveryLocationType` | Integer | `0` or `1` | Delivery destination location type |
| `saturdayDelivery` | Integer | `0` or `1` | Saturday delivery service flag |
| `holdAtLocation` | Integer | `0` or `1` | Hold at courier facility flag |
| `fragile` | Integer | `0` or `1` | Fragile package handling flag |
| `dangerous` | Integer | `0` or `1` | Hazardous / dangerous goods flag |

Any input values outside the specified ranges must result in an error rejection (`ERROR`).

## 3. Code Tables & Rate Structures

### 3.1 Base Rate Table (per pound, symmetric)
Shipping rates depend on origin and destination zone pairs. Rates are symmetric between zones (rate from zone A to B equals the rate from B to A):

| Zone Pair | Rate per Pound ($/lb) |
|---|---|
| Same Zone (1-1, 2-2, 3-3, 4-4) | $2.00 |
| Zones 1 and 2 | $2.50 |
| Zones 1 and 3 | $3.00 |
| Zones 1 and 4 | $3.75 |
| Zones 2 and 3 | $2.75 |
| Zones 2 and 4 | $3.25 |
| Zones 3 and 4 | $2.30 |

### 3.2 Delivery Speed Multipliers

| Code | Delivery Option Name | Multiplier |
|---|---|---|
| `1` | Same Day | 2.1 |
| `2` | First Overnight | 1.5 |
| `3` | Priority Overnight | 1.2 |
| `4` | Standard Overnight | 1.0 |
| `5` | 2-day AM | 0.9 |
| `6` | 2-day | 0.8 |
| `7` | Express Saver | 0.6 |

### 3.3 Service Surcharges

| Service | Condition / Code | Fee ($) |
|---|---|---|
| Signature | `signatureOption = 0` (None) | $0.00 |
| Signature | `signatureOption = 1` (Indirect) | $3.00 |
| Signature | `signatureOption = 2` (Direct) | $10.00 |
| Location Type | `deliveryLocationType = 0` (Business) | $0.00 |
| Location Type | `deliveryLocationType = 1` (Residence) | $10.00 |
| Saturday Delivery | `saturdayDelivery = 1` | $20.00 |
| Hold at Location | `holdAtLocation = 1` | $15.00 |
| Fragile Handling | `fragile = 1` | $25.00 |
| Dangerous Goods | `dangerous = 1` | $50.00 |

## 4. Calculation Procedure & Order of Operations

1. **Base Cost Calculation**:
   Look up the base rate per pound for the origin and destination zone pair.
   $$\text{baseCost} = \text{baseRate} \times \text{weight}$$

2. **Delivery Speed Adjustment**:
   Multiply base cost by the service delivery multiplier:
   $$\text{multipliedCost} = \text{baseCost} \times \text{deliveryMultiplier}$$

3. **Additive Surcharges**:
   Add all applicable service surcharges:
   $$\text{totalCost} = \text{multipliedCost} + \text{signatureSurcharge} + \text{residentialSurcharge} + \text{saturdaySurcharge} + \text{holdSurcharge} + \text{fragileSurcharge} + \text{dangerousSurcharge}$$

4. **Rounding Policy**:
   The final total cost is rounded to exactly two decimal places using standard half-up rounding.


---

## 5. Clarifications (v1.1)

- **Intermediate Precision and Final Rounding**: All intermediate calculation steps preserve full numerical precision without intermediate rounding. Only the final total cost is rounded to two decimal places using standard half-up rounding.
  - *Worked Example*: Weight = `1.05`, Origin = `1`, Destination = `2` (base rate $2.50), Delivery Option = `3` (multiplier 1.2), all other options = `0`. Base cost is $2.50 \times 1.05 = 2.625$. Multiplied cost is $2.625 \times 1.2 = 3.150$. Final rounded total is `$3.15` (intermediate rounding of base cost to $2.63 would incorrectly yield $3.16).
- **Validation Rule Priority and Multiple Violations**: If an input violates more than one domain or validation constraint, the request is rejected with `ERROR`. The evaluation order of validation checks does not affect the outcome.
- **Out-of-Domain and Malformed Inputs**: Any input value outside the specified domains (such as negative or zero weight, origin/destination outside 1–4, delivery options outside 1–7, non-integer values where integers are expected, or missing values) results in an error rejection (`ERROR`).
- **Rounding of Ties**: Ties at the third decimal are rounded away from zero, e.g. 12.345 -> 12.35 and -161.795 -> -161.80.

---

## 2. BasicPayroll Specification (v1.1.1)

# Basic Payroll Specification
Version 1.1.1

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


---

## 5. Clarifications (v1.1)

- **Intermediate Precision and Final Rounding**: All intermediate calculation steps preserve full numerical precision. Only final gross salary and final net salary are rounded to two decimal places using standard half-up rounding.
  - *Worked Example*: Hourly Salary = `17.35`, Regular Hours = `37.5`, all other inputs = `0`. Gross salary is $17.35 \times 37.5 = 650.625$. Federal tax (10%) is $65.0625$, Social Security (6.2%) is $40.33875$, Medicare (1.45%) is $9.4340625$. Total taxes are $114.8353125$. Net salary is $650.625 - 114.8353125 = 535.7896875$, rounding to `$535.79`.
- **Negative Net Salary**: No lower bound or zero-clamping is applied to net salary. If total taxes and deductions exceed gross pay, the resulting net salary is negative and is output as a negative amount.
- **Validation Rule Priority and Multiple Violations**: If an input violates more than one domain or validation constraint, the request is rejected with `ERROR`. The evaluation order of validation checks does not affect the outcome.
- **Out-of-Domain and Malformed Inputs**: Any input value outside the specified domains (such as hourly rate outside $15.00–$50.00, negative hours, negative contributions, plan codes outside specified integers, non-integer values where integers are expected, or missing values) results in an error rejection (`ERROR`).
- **Rounding of Ties**: Ties at the third decimal are rounded away from zero, e.g. 12.345 -> 12.35 and -161.795 -> -161.80.

---

## 3. EnhancedPackage Specification (v1.1.1)

# Enhanced Package Shipping Specification
Version 1.1.1

## 1. Overview
The Enhanced Package Shipping system calculates shipping costs for domestic parcels based on parcel weight, origin and destination zones, delivery service options, packaging types, and special service adjustments.

## 2. Input Parameters

| Field Name | Type | Valid Domain | Description |
|---|---|---|---|
| `weight` | Floating-point | `0.0 < weight <= 60.0` | Parcel weight in pounds (lbs) |
| `origin` | Integer | `1` to `4` | Origin domestic zone |
| `destination` | Integer | `1` to `4` | Destination domestic zone |
| `deliveryOption` | Integer | `1` to `7` | Delivery speed service option |
| `packaging` | Integer | `1` to `8` | Packaging type code |
| `signature` | Integer | `0` to `2` | Signature service requirement |
| `locationType` | Integer | `0` or `1` | Destination location type (0=Business, 1=Residence) |
| `saturday` | Integer | `0` or `1` | Saturday delivery service flag |
| `hold` | Integer | `0` or `1` | Hold at courier location flag |
| `fragile` | Integer | `0` or `1` | Fragile package handling flag |
| `dangerous` | Integer | `0` or `1` | Hazardous / dangerous goods flag |

Any input values violating the valid domains must result in an error rejection (`ERROR`).

## 3. Rate Tables & Option Schedules

### 3.1 Base Rate per Pound (Symmetric)
Rates are symmetric between origin and destination zones (rate from A to B equals rate from B to A):

| Zone Pair | Rate per Pound ($/lb) |
|---|---|
| Same Zone (1-1, 2-2, 3-3, 4-4) | $2.00 |
| Zones 1 and 2 | $2.50 |
| Zones 1 and 3 | $3.00 |
| Zones 1 and 4 | $3.75 |
| Zones 2 and 3 | $2.75 |
| Zones 2 and 4 | $3.25 |
| Zones 3 and 4 | $2.30 |

### 3.2 Delivery Service Options & Multipliers

| Code | Option Name | Standard Multiplier | Special Multiplier Condition |
|---|---|---|---|
| `1` | Same Day | 2.1 | 2.75 if fragile handling is active (`fragile = 1`) |
| `2` | First Overnight | 1.5 | - |
| `3` | Priority Overnight | 1.2 | - |
| `4` | Standard Overnight | 1.0 | - |
| `5` | 2-day AM | 0.9 | - |
| `6` | 2-day | 0.8 | - |
| `7` | Express Saver | 0.6 | - |

### 3.3 Packaging Types, Weight Limits, and Fees
Codes 6 through 8 represent customer-packaged dimension tiers; no dimension input is required:

| Code | Packaging Description | Weight Capacity Limit | Fee / Adjustment |
|---|---|---|---|
| `1` | Envelope | weight <= 2.0 lbs | +$0.00 |
| `2` | Small Box | weight <= 10.0 lbs | +$0.00 |
| `3` | Medium Box | weight <= 25.0 lbs | +$7.00 |
| `4` | Large Box | weight <= 40.0 lbs | +$18.00 |
| `5` | Tube | weight <= 10.0 lbs | +$5.00 |
| `6` | Customer Packaged (total dimensions <= 30 inches) | weight <= 60.0 lbs | +$0.00 |
| `7` | Customer Packaged (dimensions > 30 and <= 60 inches) | weight <= 60.0 lbs | Multiply running price by 1.15 |
| `8` | Customer Packaged (dimensions > 60 and <= 100 inches) | weight <= 60.0 lbs | Multiply running price by 1.30 |

If parcel weight exceeds the specified packaging capacity limit, the transaction must be rejected (`ERROR`).

### 3.4 Service Adjustments
- **Signature Service**:
  - `signature = 0` (None): $0.00
  - `signature = 1` (Indirect): +$3.00
  - `signature = 2` (Direct): +$10.00
- **Location Surcharge**:
  - `locationType = 1` (Residence): +$10.00 (Business `locationType = 0` has no surcharge)
- **Saturday Delivery**:
  - Standard Saturday fee is +$20.00.
  - Special Rule: If `locationType = 1` (Residence), destination zone is 4, and `saturday = 1`, the Saturday fee is reduced to +$10.00.
- **Hold at Location**:
  - Standard hold fee is +$15.00.
  - Special Rule: If `deliveryOption = 7` (Express Saver) and `hold = 1`, the hold fee is waived ($0.00).
- **Fragile Handling**:
  - Standard fragile fee is +$25.00.
  - Special Rule: If `fragile = 1` and `signature = 2` (Direct signature), an additional +$30.00 surcharge is assessed.
- **Dangerous Goods**:
  - Standard dangerous goods fee is +$50.00.
  - Special Rule: If `dangerous = 1` and `signature = 0`, an additional +$40.00 surcharge is assessed.

## 4. Special Business & Validation Rules

- **Rule 1 (Discount on Local Overnight/Express)**: If origin equals destination (`origin = destination`) and delivery option is in `{1, 2, 3}`, a 10% discount is applied to the final running total (price multiplied by 0.90).
- **Rule 2 (Fragile Same-Day Multiplier)**: When `fragile = 1` and `deliveryOption = 1`, the delivery multiplier is 2.75 instead of 2.1.
- **Rule 3 (Hold Fee Waiver)**: Express Saver (`deliveryOption = 7`) with hold at location incurs no hold fee.
- **Rule 4 (Business Signature Requirement)**: Business deliveries (`locationType = 0`) require signature option 1 or 2. If `signature = 0`, reject the request (`ERROR`).
- **Rule 5 (Heavy Parcel Signature Requirement)**: Parcels weighing over 50.0 lbs (`weight > 50.0`) require signature option 1 or 2. If `signature = 0`, reject the request (`ERROR`).
- **Rule 6 (Fragile Direct Signature Surcharge)**: When `fragile = 1` and `signature = 2`, add an extra $30.00 fee.
- **Rule 7 (Fragile and Dangerous Incompatibility)**: Fragile handling and dangerous goods cannot be combined. If `fragile = 1` and `dangerous = 1`, reject the request (`ERROR`).
- **Rule 8 (Dangerous Goods Permitted Services)**: Dangerous goods (`dangerous = 1`) are only accepted for delivery options 1, 2, 3, or 4. Options 5, 6, and 7 must be rejected (`ERROR`).
- **Rule 9 (Residential Zone 4 Saturday Concession)**: Saturday delivery to a residence in destination zone 4 is charged at +$10.00 instead of +$20.00.
- **Rule 10 (Dangerous Goods Unsigned Surcharge)**: When `dangerous = 1` and `signature = 0`, add an extra $40.00 fee.

## 5. Calculation Procedure & Order of Operations

The calculation follows a calibrated sequential order:

1. **Base Price**:
   $$\text{basePrice} = \text{baseRate}(\text{origin}, \text{destination}) \times \text{weight}$$

2. **Delivery Option Scaling**:
   Apply delivery multiplier (taking into account Rule 2 if fragile same-day):
   $$\text{scaledPrice} = \text{basePrice} \times \text{deliveryMultiplier}$$

3. **Packaging Tier Application**:
   - For packaging codes 1 through 6: Add the fixed fee (+$0, +$7, +$18, or +$5).
   - For packaging code 7: Multiply current price by 1.15.
   - For packaging code 8: Multiply current price by 1.30.

4. **Additive Adjustments**:
   Add each applicable service fee to the running price:
   - Signature fee ($3 for indirect, $10 for direct)
   - Residence fee ($10 if residential)
   - Saturday fee ($10 if residential in zone 4, else $20)
   - Hold fee ($0 if Express Saver, else $15)
   - Fragile fee ($25 if fragile)
   - Dangerous goods fee ($50 if dangerous)
   - Extra fragile direct signature fee ($30 if fragile and direct signature)
   - Extra dangerous unsigned fee ($40 if dangerous and signature 0)

5. **Discount Application**:
   If origin equals destination and delivery option is 1, 2, or 3 (Rule 1), apply a 10% discount to the total running price:
   $$\text{discountedPrice} = \text{runningPrice} \times 0.90$$

6. **Rounding Policy**:
   The final total price is rounded to two decimal places using standard half-up rounding.


---

## 6. Clarifications (v1.1)

- **Intermediate Precision and Final Rounding**: All intermediate calculation steps preserve full numerical precision without intermediate rounding. Only the final total price is rounded to two decimal places using standard half-up rounding.
  - *Worked Example*: Weight = `1.05`, Origin = `1`, Destination = `1` (base rate $2.00), Delivery Option = `3` (multiplier 1.2), Packaging = `7` (multiplier 1.15), Signature = `0`, Location Type = `1` (Residence, +$10.00), all other flags = `0`. Base price is $2.00 \times 1.05 = 2.10$. Scaled price is $2.10 \times 1.2 = 2.52$. Packaging adjusted price is $2.52 \times 1.15 = 2.898$. Running price with residence fee is $2.898 + 10.00 = 12.898$. Local discount (10%) yields $12.898 \times 0.90 = 11.6082$, rounding to `$11.61`.
- **Validation Rule Priority and Multiple Violations**: If an input vector violates more than one domain constraint or validation rule, the request is rejected with `ERROR`. The evaluation order of validation checks does not affect the outcome.
- **Out-of-Domain and Malformed Inputs**: Any input value outside the specified domains (such as weight <= 0 or > 60, zone codes outside 1–4, packaging outside 1–8, non-integer values where integers are expected, or missing values) results in an error rejection (`ERROR`).
- **Rounding of Ties**: Ties at the third decimal are rounded away from zero, e.g. 12.345 -> 12.35 and -161.795 -> -161.80.

---

## 4. EnhancedPayroll Specification (v1.1.1)

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

---

## 5. CSV Format & Oracular Standards

# Benchmark CSV Format Specification

This document defines the standard CSV input/output format for the benchmark test suites across all four target applications.

## 1. Test Suite Quotas
- **Basic Applications** (BasicPackage, BasicPayroll): **20 test cases per suite**
- **Enhanced Applications** (EnhancedPackage, EnhancedPayroll): **35 test cases per suite**

## 2. General Formatting Rules
- All files must be UTF-8 encoded text files using comma (`,`) as the delimiter.
- The first line must contain the exact header specified below.
- Numeric floating-point values use standard decimal point (`.`).
- The final column `expected` must contain either:
  - The expected numeric calculation result rounded to 2 decimal places (e.g. `131.40`, `553.82`, `20.00`).
  - The literal word `ERROR` if the input vector violates any domain constraint, dependency, or business validation rule.
  - In unlabelled suites (such as pure PICT generated suites before baseline oracle evaluation), the `expected` column is left empty.

---

## 3. Application Fixed CSV Headers

### 3.1 Basic Package (`BasicPackage`)
```csv
weight,origin,destination,deliveryOption,signatureOption,deliveryLocationType,saturdayDelivery,holdAtLocation,fragile,dangerous,expected
```

### 3.2 Basic Payroll (`BasicPayroll`)
```csv
hourlySalary,regularHours,overtimeHours,holidayHours,medicalPlan,dentalPlan,visionPlan,unitedWay,pension,longTermDisability,shortTermDisability,parking,expected
```

### 3.3 Enhanced Package (`EnhancedPackage`)
```csv
weight,origin,destination,deliveryOption,packaging,signature,locationType,saturday,hold,fragile,dangerous,expected
```

### 3.4 Enhanced Payroll (`EnhancedPayroll`)
```csv
hourly,regularHours,overtimeHours,holidayHours,state,medical,dental,vision,unitedWay,pension,ltd,std,parking,expected
```

---

