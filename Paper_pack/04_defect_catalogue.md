# Defect Catalogues, Type Distributions & System Assumptions

This document consolidates the verbatim defect catalogues, defect-type distributions, and system assumptions from the documentation of all four benchmark applications (Project1 through Project4).

## 1. Project 1: BasicPackage (Overnight Package Shipping)

### Assumptions (Verbatim from Project1 README)

## 2. Assumptions
1. **Distance Symmetry:** Base shipping rates are symmetric between origin and destination zones (`rate(A, B) == rate(B, A)`).
2. **Fallback Rate:** Unknown or unmapped zone pairs fall back to a default base rate of `$2.00/lb` (used by D4 and D5).
3. **Monetary Precision:** Calculations use `BigDecimal`. Intermediate values are unrounded; the final total price is rounded to 2 decimal places using `RoundingMode.HALF_UP`.
4. **Input Boundaries:**
   - `0 < weight <= 60.0` lbs.
   - Origin and Destination zones are integers `1..4`.
   - Delivery option is an integer `1..7`.
   - Signature option is `0` (None), `1` (Indirect), or `2` (Direct).
   - Delivery location type is `0` (Business) or `1` (Residence).
   - Surcharge flags (`saturdayDelivery`, `holdAtLocation`, `fragile`, `dangerous`) must be either `0` or `1`. Any other value throws `IllegalArgumentException`.

---

### Defect Catalogue (Verbatim from Project1 README)

## 4. Defect Catalogue

| ID | Description | Type | class.method | Example Input Exposing Defect |
|---|---|---|---|---|
| **D1** | Max weight limit reduced from 60 to 50 lbs (weights in (50, 60] wrongly rejected) | boundary | `PackageShippingInput.validate` | `weight=55.0, origin=1, dest=1, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE passes with $110.00; D1 throws `IllegalArgumentException`) |
| **D2** | Validation of weight <= 0 removed | boundary | `PackageShippingInput.validate` | `weight=-1.0, origin=1, dest=1, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE throws `IllegalArgumentException`; D2 computes -$2.00) |
| **D3** | Same-zone rate increased from $2.00 to $3.00 per lb | arithmetic | `ShippingCalculator.getBaseRate` | `weight=10.0, origin=1, dest=1, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$20.00, D3=$30.00) |
| **D4** | Zone pair (2,3)/(3,2) looked up with invalid key (2,5), falling back to default $2.00/lb | logic | `ShippingCalculator.getBaseRate` | `weight=10.0, origin=2, dest=3, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$27.50, D4=$20.00) |
| **D5** | Special handling of pair (1,4)/(4,1) removed, falling back to default $2.00/lb | logic | `ShippingCalculator.getBaseRate` | `weight=10.0, origin=1, dest=4, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$37.50, D5=$20.00) |
| **D6** | Priority Overnight multiplier increased from 1.2 to 2.2 | arithmetic | `ShippingCalculator.getDeliveryMultiplier` | `weight=10.0, origin=1, dest=1, deliv=3, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$24.00, D6=$44.00) |
| **D7** | Multipliers of Same Day (2.1) and Standard Overnight (1.0) swapped | logic | `ShippingCalculator.getDeliveryMultiplier` | `weight=10.0, origin=1, dest=1, deliv=1, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$42.00, D7=$20.00) |
| **D8** | Residential surcharge increased from $10.00 to $20.00 | arithmetic | `ShippingCalculator.getResidentialSurcharge` | `weight=10.0, origin=1, dest=1, deliv=4, sig=0, loc=1, sat=0, hold=0, frag=0, dang=0` (NONE=$30.00, D8=$40.00) |
| **D9** | Delivery location type interpreted inversely (Business <-> Residence) | logic | `ShippingCalculator.getResidentialSurcharge` | `weight=10.0, origin=1, dest=1, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$20.00, D9=$30.00) |
| **D10** | Fragile condition inverted (surcharge applied when fragile = 0) | logic | `ShippingCalculator.getFragileSurcharge` | `weight=10.0, origin=1, dest=1, deliv=4, sig=0, loc=0, sat=0, hold=0, frag=0, dang=0` (NONE=$20.00, D10=$45.00) |

### Defect Type Distribution (Derived from Catalogue)

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **boundary** | 2 | 20.0% | D1, D2 |
| **arithmetic** | 3 | 30.0% | D3, D6, D8 |
| **logic** | 5 | 50.0% | D4, D5, D7, D9, D10 |
| **Total** | **10** | **100.0%** | D1–D10 |

---

## 2. Project 2: BasicPayroll (Standard Payroll Calculation)

### Assumptions (Verbatim from Project2 README)

## 2. Assumptions
1. **Monetary Precision:** All money values use `BigDecimal`. Intermediate values (gross, taxes, individual deductions) remain unrounded; the final net salary is rounded to 2 decimal places using `RoundingMode.HALF_UP`.
2. **Pension Deduction Cap:** The deducted pension amount is capped at 10% of gross salary: `deductedPension = min(pension, 0.10 * gross)`.
3. **Fixed Benefit Deduction Table:**
   - **Medical:** Plan 0 = $0, Plan 1 = $50, Plan 2 = $75, Plan 3 = $125
   - **Dental:** Plan 0 = $0, Plan 1 = $5, Plan 2 = $10
   - **Vision:** Plan 0 = $0, Plan 1 = $5, Plan 2 = $12
   - **Long-Term Disability (LTD):** $14.00 if active (1), else $0.00
   - **Short-Term Disability (STD):** $9.00 if active (1), else $0.00
   - **Parking:** $39.50 if active (1), else $0.00
   - **United Way:** Exact dollar amount specified (>= 0)
4. **Federal Tax Brackets:**
   - `$15.00` to `$20.00` hourly -> 10% (0.10)
   - `$20.01` to `$40.00` hourly -> 22% (0.22)
   - `$40.01` to `$50.00` hourly -> 35% (0.35)
5. **FICA Taxes:**
   - Social Security: 6.2% (0.062)
   - Medicare: 1.45% (0.0145)
6. **Input Boundaries:**
   - `15.00 <= hourlySalary <= 50.00`
   - `regularHours >= 0`, `overtimeHours >= 0`, `holidayHours >= 0`
   - `medicalPlan` in `[0, 3]`, `dentalPlan` in `[0, 2]`, `visionPlan` in `[0, 2]`
   - `unitedWay >= 0.0`, `pension >= 0.0`
   - `longTermDisability`, `shortTermDisability`, `parking` must be either `0` or `1`.

---

### Defect Catalogue (Verbatim from Project2 README)

## 5. Defect Catalogue

| ID | Description | Type | class.method | Example Input Exposing Defect |
|---|---|---|---|---|
| **D1** | Maximum hourly salary ($50) validation removed | boundary | `PayrollInput.validate` | `hourly=55.00, reg=40, ot=0, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE throws `IllegalArgumentException`; D1 allows calculation) |
| **D2** | Social Security rate changed from 0.062 to 0.05 | arithmetic | `PayrollCalculator.calculate` | `hourly=20.00, reg=40, ot=0, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$658.80, D2 Net=$668.40) |
| **D3** | Overtime multiplier changed from 1.5 to 1.25 | arithmetic | `PayrollCalculator.calculate` | `hourly=20.00, reg=40, ot=10, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$905.85, D3 Net=$864.68) |
| **D4** | 22% bracket ends at $35 instead of $40 (so $35.01-$40 gets 35%) | boundary | `PayrollCalculator.getFederalTaxRate` | `hourly=38.00, reg=40, ot=0, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$1069.32, D4 Net=$871.72) |
| **D5** | Vision insurance deduction never applied | logic | `PayrollCalculator.getVisionDeduction` | `hourly=20.00, reg=40, ot=0, hol=0, med=0, den=0, vis=2, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$646.80, D5 Net=$658.80) |
| **D6** | Medical Plan 2 and Plan 3 amounts swapped | logic | `PayrollCalculator.getMedicalDeduction` | `hourly=20.00, reg=40, ot=0, hol=0, med=2, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$583.80, D6 Net=$533.80) |
| **D7** | Dental deduction uses the medical plan amounts | logic | `PayrollCalculator.getDentalDeduction` | `hourly=20.00, reg=40, ot=0, hol=0, med=0, den=2, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$648.80, D7 Net=$583.80) |
| **D8** | Medical deduction uses the LTD amount ($14) instead of plan amounts | logic | `PayrollCalculator.getMedicalDeduction` | `hourly=20.00, reg=40, ot=0, hol=0, med=2, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$583.80, D8 Net=$644.80) |
| **D9** | United Way contribution ignored | logic | `PayrollCalculator.calculate` | `hourly=20.00, reg=40, ot=0, hol=0, med=0, den=0, vis=0, uw=50.0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$608.80, D9 Net=$658.80) |
| **D10** | Overtime hours ignored (treated as 0) | logic | `PayrollCalculator.calculate` | `hourly=20.00, reg=40, ot=10, hol=0, med=0, den=0, vis=0, uw=0, pen=0, ltd=0, std=0, pkg=0` (NONE Net=$905.85, D10 Net=$658.80) |

### Defect Type Distribution (Derived from Catalogue)

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **boundary** | 2 | 20.0% | D1, D4 |
| **arithmetic** | 2 | 20.0% | D2, D3 |
| **logic** | 6 | 60.0% | D5, D6, D7, D8, D9, D10 |
| **Total** | **10** | **100.0%** | D1–D10 |

---

## 3. Project 3: EnhancedPackage (Multi-Factor Package Shipping)

### Assumptions (Verbatim from Project3 README)

## 6. Assumptions

1. **Operation Order**: Base rate * weight, multiplied by delivery multiplier (incorporating N2), adjusted by packaging (additive or multiplier), additive surcharges (signature, residence, Saturday N9, hold N3, fragile, dangerous, N6, N10), and finally N1 discount applied to the total running price.
2. **Rate Symmetry**: Rates between origin and destination are symmetric (`rate(A, B) == rate(B, A)`), except as specifically modified by Defect D19.
3. **Packaging Dimensions**: Codes 6–8 encode dimension tiers directly without explicit dimension measurement inputs.
4. **Signature Requirements**: N4 requires signature 1 or 2 for business; N5 requires signature 1 or 2 for packages weighing more than 50 lbs.
5. **Rounding**: All monetary calculations use `BigDecimal` with final result rounded HALF_UP to 2 decimal places.

---

### Defect Catalogue (Verbatim from Project3 README)

## 7. Defect Catalogue (20 Seeded Defects)

| ID | Description | Type | Note | class.method | Exposing Input (NONE vs Dk) |
|---|---|---|---|---|---|
| **D1** | Zone 3-4 rate 2.30 -> 2.50 | arithmetic | - | `ShippingCalculator.getBaseRate` | `w=10 o=3 d=4 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$26.00, D1=$28.00) |
| **D2** | Priority Overnight multiplier 1.2 -> 1.5 | arithmetic | - | `ShippingCalculator.getDeliveryMultiplier` | `w=10 o=1 d=2 del=3 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$33.00, D2=$40.50) |
| **D3** | N1 no longer applies to Same Day (option 1) | conditional | N1 | `ShippingCalculator.calculate` | `w=10 o=2 d=2 del=1 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$40.50, D3=$45.00) |
| **D4** | Small Box limit w<=10 becomes w<10 | boundary | - | `ShippingCalculator.calculate` | `w=10.0 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D4=Reject) |
| **D5** | Indirect signature fee 3 -> 5 | arithmetic | - | `ShippingCalculator.calculate` | `w=5 o=1 d=2 del=4 pkg=6 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$15.50, D5=$17.50) |
| **D6** | N4: direct signature no longer accepted for business | conditional | N4 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=2 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D6=Reject) |
| **D7** | N6 applies to indirect signature instead of direct | conditional | N6 | `ShippingCalculator.calculate` | `w=5 o=1 d=2 del=4 pkg=2 sig=2 loc=0 sat=0 hold=0 frag=1 dang=0` (NONE=$77.50, D7=$47.50) |
| **D8** | Hold-at-location fee never charged | logic | - | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=1 frag=0 dang=0` (NONE=$43.00, D8=$28.00) |
| **D9** | N7 removed (fragile+dangerous allowed) | conditional | N7 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=1 dang=1` (NONE=Reject, D9=$103.00) |
| **D10** | N10 removed | conditional | N10 | `ShippingCalculator.calculate` | `w=20 o=1 d=2 del=4 pkg=3 sig=0 loc=1 sat=0 hold=0 frag=0 dang=1` (NONE=$157.00, D10=$117.00) |
| **D11** | N5 threshold weight>50 becomes weight>55 | boundary | N5 | `ShippingCalculator.calculate` | `w=55.0 o=1 d=2 del=4 pkg=8 sig=0 loc=1 sat=0 hold=0 frag=0 dang=0` (NONE=Reject, D11=$188.75) |
| **D12** | Envelope limit w<=2 becomes w<2 | boundary | - | `ShippingCalculator.calculate` | `w=2.0 o=1 d=1 del=4 pkg=1 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D12=Reject) |
| **D13** | Packaging code 7 multiplier 1.15 -> 1.25 | arithmetic | - | `ShippingCalculator.applyPackaging` | `w=20 o=1 d=3 del=4 pkg=7 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$72.00, D13=$78.00) |
| **D14** | Medium Box limit w<=25 becomes w<25 | boundary | - | `ShippingCalculator.calculate` | `w=25.0 o=1 d=2 del=4 pkg=3 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D14=Reject) |
| **D15** | N9 applies to destination zone 3 instead of 4 | conditional | N9 | `ShippingCalculator.calculate` | `w=10 o=1 d=3 del=4 pkg=2 sig=1 loc=1 sat=1 hold=0 frag=0 dang=0` (NONE=$63.00, D15=$53.00) |
| **D16** | N1 discount 10% -> 5% | arithmetic | N1 | `ShippingCalculator.calculate` | `w=10 o=2 d=2 del=2 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$29.70, D16=$31.35) |
| **D17** | N3 exception removed (hold fee charged for option 7) | conditional | N3 | `ShippingCalculator.calculate` | `w=8 o=2 d=3 del=7 pkg=2 sig=1 loc=0 sat=0 hold=1 frag=0 dang=0` (NONE=$16.20, D17=$31.20) |
| **D18** | N8 allows deliveryOption 5 | conditional | N8 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=5 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=1` (NONE=Reject, D18=$75.50) |
| **D19** | Rate for pair (4,2) falls back to 2.00 while (2,4) stays correct | logic | - | `ShippingCalculator.getBaseRate` | `w=10 o=4 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$35.50, D19=$23.00) |
| **D20** | Residence fee not charged when Saturday=1 | logic | - | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=1 sat=1 hold=0 frag=0 dang=0` (NONE=$58.00, D20=$48.00) |

---

### Defect Type Distribution (Verbatim from Project3 README)

## 8. Defect Type Distribution

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **conditional** | 8 | 40.0% | D3, D6, D7, D9, D10, D15, D17, D18 |
| **arithmetic** | 5 | 25.0% | D1, D2, D5, D13, D16 |
| **boundary** | 4 | 20.0% | D4, D11, D12, D14 |
| **logic** | 3 | 15.0% | D8, D19, D20 |
| **Total** | **20** | **100.0%** | D1–D20 |

---

## 4. Project 4: EnhancedPayroll (Multi-State & Multi-Deduction Payroll)

### Assumptions (Verbatim from Project4 README)

## 5. Assumptions

1. **Pay Period**: Weekly pay period calculations.
2. **Taxable Bonuses**: Overtime bonus (N2) and holiday bonus (N3) are included in gross pay before calculating federal, state, and FICA taxes.
3. **Net Pay Credits**: Credits (N7, N8, N10) are applied directly to net pay after all taxes and deductions have been subtracted.
4. **Interpretation of N5**: N5 is interpreted literally: employees with hourly rate >= $40.01 must be enrolled in medical plan 2 or 3 (they cannot opt out with plan 0, nor choose plan 1).
5. **Tax Bracket Lookups**: Federal and state tax rates are determined based on the employee's base hourly wage, not total gross earnings.
6. **Monetary Precision**: All calculations use `BigDecimal` with final gross and net rounded HALF_UP to 2 decimal places.

---

### Defect Catalogue (Verbatim from Project4 README)

## 6. Defect Catalogue (20 Seeded Defects)

| ID | Description | Type | Note | class.method | Exposing Input (NONE vs Dk) |
|---|---|---|---|---|---|
| **D1** | Overtime multiplier 1.5 -> 1.7 | arithmetic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=4 hol=0 state=2 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$774.53, D1=$788.00) |
| **D2** | N3 removed (no $75 holiday bonus) | conditional | N3 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=8 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$978.66, D2=$928.84) |
| **D3** | Federal rate for 20.01-40 changes 22% -> 25% | arithmetic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$663.50, D3=$633.50) |
| **D4** | Arkansas: no withholding for lower range 15-30 | logic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=2 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$673.50, D4=$703.50) |
| **D5** | N5 starts at 40.00 instead of 40.01 | boundary | N5 | `PayrollCalculator.calculate` | `h=40.00 reg=40 ot=0 hol=0 state=1 med=1 den=1 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=Pass, D5=Reject) |
| **D6** | N6 removed (health deductions charged at h<=20) | conditional | N6 | `PayrollCalculator.calculate` | `h=18 reg=40 ot=0 hol=0 state=1 med=3 den=2 vis=2 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$607.92, D6=$460.92) |
| **D7** | Dental plan 2: 10 -> 12 | arithmetic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=2 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$653.50, D7=$651.50) |
| **D8** | N8 minimum 10 -> 20 | boundary | N8 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=15 pen=0 ltd=0 std=0 park=0` (NONE=$650.75, D8=$648.50) |
| **D9** | N9 removed (LTD without STD allowed) | conditional | N9 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=1 std=0 park=0` (NONE=Reject, D9=$649.50) |
| **D10** | N10 credit is subtracted instead of added | logic | N10 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=1 std=1 park=0` (NONE=$645.50, D10=$635.50) |
| **D11** | N2 triggers at overtime>=5 instead of >5 | boundary | N2 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=5 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$787.91, D11=$821.08) |
| **D12** | N1 overtime limit 10 -> 12 | boundary | N1 | `PayrollCalculator.calculate` | `h=18 reg=40 ot=11 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=Reject, D12=$878.67) |
| **D13** | Medicare 1.45% -> 1.5% | arithmetic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$663.50, D13=$663.00) |
| **D14** | Tennessee upper rate 8% -> 6% | arithmetic | - | `PayrollCalculator.calculate` | `h=40 reg=40 ot=0 hol=0 state=1 med=2 den=1 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$917.60, D14=$949.60) |
| **D15** | N4 removed | conditional | N4 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=1 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=Reject, D15=$613.50) |
| **D16** | N7 removed | conditional | N7 | `PayrollCalculator.calculate` | `h=30 reg=40 ot=0 hol=0 state=2 med=3 den=2 vis=2 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$676.20, D16=$661.20) |
| **D17** | Mississippi middle bracket starts at 25.00 instead of 25.01 | boundary | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=3 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$683.50, D17=$673.50) |
| **D18** | Pension cap computed on base pay instead of gross | logic | - | `PayrollCalculator.calculate` | `h=25 reg=30 ot=5 hol=3 state=2 med=0 den=0 vis=0 uw=0 pen=200 ltd=0 std=0 park=0` (NONE=$623.68, D18=$657.43) |
| **D19** | N10 requires LTD OR STD instead of both | conditional | N10 | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=1 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=1 park=0` (NONE=$654.50, D19=$659.50) |
| **D20** | States 2 and 3 are swapped in state-tax lookup | logic | - | `PayrollCalculator.calculate` | `h=25 reg=40 ot=0 hol=0 state=2 med=0 den=0 vis=0 uw=0 pen=0 ltd=0 std=0 park=0` (NONE=$673.50, D20=$683.50) |

---

### Defect Type Distribution (Verbatim from Project4 README)

## 7. Defect Type Distribution

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **conditional** | 6 | 30.0% | D2, D6, D9, D15, D16, D19 |
| **arithmetic** | 5 | 25.0% | D1, D3, D7, D13, D14 |
| **boundary** | 5 | 25.0% | D5, D8, D11, D12, D17 |
| **logic** | 4 | 20.0% | D4, D10, D18, D20 |
| **Total** | **20** | **100.0%** | D1–D20 |
