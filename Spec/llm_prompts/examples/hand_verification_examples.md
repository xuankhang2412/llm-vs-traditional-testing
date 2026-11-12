# Hand Verification of Benchmark Examples

This document records the step-by-step hand calculations for all example test cases used in the few-shot prompting conditions, verifying consistency against the baseline specification and the clean application execution.

---

## 1. Basic Package Examples

### Example 1 (One-Shot): `20.0, 2, 3, 4, 1, 0, 0, 0, 0, 0`
- Input: weight=20.0 lbs, origin=2, destination=3, deliveryOption=4 (Standard Overnight), signature=1 (Indirect), location=0 (Business), saturday=0, hold=0, fragile=0, dangerous=0.
- Base rate: Zone pair (2, 3) = $2.75 / lb.
- Base cost = 2.75 * 20.0 = $55.00.
- Delivery multiplier: Option 4 = x1.0 -> multiplied cost = $55.00.
- Surcharges: Indirect signature (option 1) = +$3.00.
- Total cost = 55.00 + 3.00 = $58.00.
- **Expected: 58.00**

### Example 2: `15.0, 1, 2, 2, 0, 1, 0, 0, 0, 0`
- Input: weight=15.0 lbs, origin=1, destination=2, deliveryOption=2 (First Overnight), signature=0, location=1 (Residence), saturday=0, hold=0, fragile=0, dangerous=0.
- Base rate: Zone pair (1, 2) = $2.50 / lb.
- Base cost = 2.50 * 15.0 = $37.50.
- Delivery multiplier: Option 2 = x1.5 -> multiplied cost = 37.50 * 1.5 = $56.25.
- Surcharges: Residence location = +$10.00.
- Total cost = 56.25 + 10.00 = $66.25.
- **Expected: 66.25**

### Example 3: `25.0, 1, 3, 3, 2, 0, 1, 0, 0, 0`
- Input: weight=25.0 lbs, origin=1, destination=3, deliveryOption=3 (Priority Overnight), signature=2 (Direct), location=0, saturday=1, hold=0, fragile=0, dangerous=0.
- Base rate: Zone pair (1, 3) = $3.00 / lb.
- Base cost = 3.00 * 25.0 = $75.00.
- Delivery multiplier: Option 3 = x1.2 -> multiplied cost = 75.00 * 1.2 = $90.00.
- Surcharges: Direct signature = +$10.00, Saturday = +$20.00.
- Total cost = 90.00 + 10.00 + 20.00 = $120.00.
- **Expected: 120.00**

---

## 2. Basic Payroll Examples

### Example 1 (One-Shot): `28.0, 40.0, 0.0, 0.0, 1, 1, 0, 15.0, 50.0, 0, 0, 0`
- Input: hourly=28.00, regularHours=40.0, overtimeHours=0.0, holidayHours=0.0, med=1, den=1, vis=0, unitedWay=15.00, pension=50.00, ltd=0, std=0, parking=0.
- Base pay: 28.00 * 40.0 = $1120.00. Gross = $1120.00.
- Federal tax: hourly=28.00 falls in $20.01-$40.00 bracket -> 22% -> 1120.00 * 0.22 = $246.40.
- Social Security: 1120.00 * 0.062 = $69.44.
- Medicare: 1120.00 * 0.0145 = $16.24.
- Total taxes = 246.40 + 69.44 + 16.24 = $332.08.
- Deductions: Medical Plan 1 = $50.00, Dental Plan 1 = $5.00, Vision Plan 0 = $0.00, United Way = $15.00, Pension = min(50.00, 112.00) = $50.00. Total deductions = $120.00.
- Net pay = 1120.00 - 332.08 - 120.00 = $667.92.
- **Expected: 667.92**

### Example 2: `32.0, 35.0, 6.0, 0.0, 2, 1, 1, 0.0, 80.0, 1, 1, 0`
- Input: hourly=32.00, reg=35.0, ot=6.0, hol=0.0, med=2, den=1, vis=1, uw=0.0, pen=80.0, ltd=1, std=1, park=0.
- Base pay: 32.00 * 35.0 = $1120.00. Overtime pay: 32.00 * 6.0 * 1.5 = $288.00. Gross = $1408.00.
- Federal tax: hourly=32.00 -> 22% -> 1408.00 * 0.22 = $309.76.
- Social Security: 1408.00 * 0.062 = $87.296.
- Medicare: 1408.00 * 0.0145 = $20.416.
- Total taxes = 309.76 + 87.296 + 20.416 = $417.472.
- Deductions: Medical Plan 2 = $75.00, Dental Plan 1 = $5.00, Vision Plan 1 = $5.00, LTD = $14.00, STD = $9.00, Pension = min(80.00, 140.80) = $80.00. Total deductions = $188.00.
- Net pay = 1408.00 - 417.472 - 188.00 = 802.528 -> rounded HALF_UP = $802.53.
- **Expected: 802.53**

### Example 3: `18.0, 40.0, 4.0, 0.0, 0, 0, 0, 10.0, 0.0, 0, 0, 1`
- Input: hourly=18.00, reg=40.0, ot=4.0, hol=0.0, med=0, den=0, vis=0, uw=10.0, pen=0.0, ltd=0, std=0, park=1.
- Base pay: 18.00 * 40.0 = $720.00. Overtime pay: 18.00 * 4.0 * 1.5 = $108.00. Gross = $828.00.
- Federal tax: hourly=18.00 in $15-$20 bracket -> 10% -> 828.00 * 0.10 = $82.80.
- Social Security: 828.00 * 0.062 = $51.336.
- Medicare: 828.00 * 0.0145 = $12.006.
- Total taxes = 82.80 + 51.336 + 12.006 = $146.142.
- Deductions: United Way = $10.00, Parking = $39.50. Total deductions = $49.50.
- Net pay = 828.00 - 146.142 - 49.50 = 632.358 -> rounded HALF_UP = $632.36.
- **Expected: 632.36**

---

## 3. Enhanced Package Examples

### Example 1 (One-Shot - Source Paper Calibration Case): `35.0, 3, 3, 2, 4, 1, 0, 1, 0, 0, 0`
- Input: weight=35.0 lbs, origin=3, destination=3, deliveryOption=2 (First Overnight), packaging=4 (Large Box), signature=1 (Indirect), location=0 (Business), saturday=1, hold=0, fragile=0, dangerous=0.
- Base rate: Zone pair (3, 3) = $2.00 / lb. Base price = 2.00 * 35.0 = $70.00.
- Delivery multiplier: Option 2 = x1.5 -> scaled price = 70.00 * 1.5 = $105.00.
- Packaging: Code 4 (Large Box) = +$18.00 -> 105.00 + 18.00 = $123.00.
- Surcharges: Indirect signature = +$3.00, Saturday = +$20.00 -> 123.00 + 3.00 + 20.00 = $146.00.
- Rule 1 Discount: Origin==Destination (3==3) and deliveryOption=2 in {1,2,3} -> 10% discount: 146.00 * 0.90 = $131.40.
- **Expected: 131.40**

### Example 2: `15.0, 1, 2, 4, 3, 1, 1, 0, 0, 0, 0`
- Input: weight=15.0 lbs, origin=1, destination=2, deliveryOption=4 (Standard Overnight), packaging=3 (Medium Box), signature=1, location=1 (Residence), saturday=0, hold=0, fragile=0, dangerous=0.
- Base rate: Zone pair (1, 2) = $2.50 / lb. Base price = 2.50 * 15.0 = $37.50.
- Delivery multiplier: Option 4 = x1.0 -> $37.50.
- Packaging: Code 3 (Medium Box, w=15<=25) = +$7.00 -> $44.50.
- Surcharges: Indirect signature = +$3.00, Residence = +$10.00 -> 44.50 + 3.00 + 10.00 = $57.50.
- Discount: Not applicable (origin 1 != dest 2).
- **Expected: 57.50**

### Example 3: `8.0, 2, 3, 6, 2, 1, 0, 0, 1, 0, 0`
- Input: weight=8.0 lbs, origin=2, destination=3, deliveryOption=6 (2-day), packaging=2 (Small Box), signature=1, location=0, saturday=0, hold=1, fragile=0, dangerous=0.
- Base rate: Zone pair (2, 3) = $2.75 / lb. Base price = 2.75 * 8.0 = $22.00.
- Delivery multiplier: Option 6 = x0.8 -> $17.60.
- Packaging: Code 2 = +$0.00 -> $17.60.
- Surcharges: Indirect signature = +$3.00, Hold at location = +$15.00 (charged since deliveryOption is 6, not 7) -> 17.60 + 3.00 + 15.00 = $35.60.
- Discount: Not applicable.
- **Expected: 35.60**

---

## 4. Enhanced Payroll Examples

### Example 1 (One-Shot): `28.0, 40.0, 0.0, 0.0, 1, 1, 1, 0, 0.0, 50.0, 0, 0, 0`
- Input: hourly=28.00, reg=40.0, ot=0.0, hol=0.0, state=1 (TN), med=1, den=1, vis=0, uw=0.0, pen=50.0, ltd=0, std=0, park=0.
- Base pay: 28.00 * 40.0 = $1120.00. Gross = $1120.00.
- Federal tax: hourly=28.00 in $20.01-$40.00 -> 22% -> 1120.00 * 0.22 = $246.40.
- Social Security: 1120.00 * 0.062 = $69.44.
- Medicare: 1120.00 * 0.0145 = $16.24.
- State tax TN: hourly=28.00 in $20.01-$35.00 -> 4% -> 1120.00 * 0.04 = $44.80.
- Total taxes = 246.40 + 69.44 + 16.24 + 44.80 = $376.88.
- Deductions: Medical Plan 1 = $50.00, Dental Plan 1 = $5.00, Pension = min(50.00, 112.00) = $50.00. Total deductions = $105.00.
- Net pay = 1120.00 - 376.88 - 105.00 = $638.12.
- **Expected: 638.12**

### Example 2: `24.0, 35.0, 4.0, 0.0, 2, 2, 1, 1, 12.0, 0.0, 0, 0, 0`
- Input: hourly=24.00, reg=35.0, ot=4.0, hol=0.0, state=2 (AR), med=2, den=1, vis=1, uw=12.0, pen=0.0, ltd=0, std=0, park=0.
- Base pay: 24.00 * 35.0 = $840.00. Overtime pay: 24.00 * 4.0 * 1.5 = $144.00 (no Rule 2 bonus since ot=4.0 is not > 5.0). Gross = $984.00.
- Federal tax: hourly=24.00 -> 22% -> 984.00 * 0.22 = $216.48.
- Social Security: 984.00 * 0.062 = $61.008.
- Medicare: 984.00 * 0.0145 = $14.268.
- State tax AR: hourly=24.00 in $15-$30 -> 3% -> 984.00 * 0.03 = $29.52.
- Total taxes = 216.48 + 61.008 + 14.268 + 29.52 = $321.276.
- Deductions: Medical Plan 2 = $75.00, Dental Plan 1 = $5.00, Vision Plan 1 = $5.00, United Way = $12.00. Total deductions = $97.00.
- Net before credits = 984.00 - 321.276 - 97.00 = $565.724.
- Rule 8 Credit: unitedWay=12.00 >= 10.00 -> +15% of 12.00 = +$1.80.
- Final net pay = 565.724 + 1.80 = 567.524 -> rounded HALF_UP = $567.52.
- **Expected: 567.52**

### Example 3: `32.0, 40.0, 0.0, 0.0, 3, 1, 1, 0, 0.0, 100.0, 1, 1, 1`
- Input: hourly=32.00, reg=40.0, ot=0.0, hol=0.0, state=3 (MS), med=1, den=1, vis=0, uw=0.0, pen=100.0, ltd=1, std=1, park=1.
- Base pay: 32.00 * 40.0 = $1280.00. Gross = $1280.00.
- Federal tax: hourly=32.00 -> 22% -> 1280.00 * 0.22 = $281.60.
- Social Security: 1280.00 * 0.062 = $79.36.
- Medicare: 1280.00 * 0.0145 = $18.56.
- State tax MS: hourly=32.00 in $25.01-$40.00 -> 3% -> 1280.00 * 0.03 = $38.40.
- Total taxes = 281.60 + 79.36 + 18.56 + 38.40 = $417.92.
- Deductions: Medical Plan 1 = $50.00, Dental Plan 1 = $5.00, LTD = $14.00, STD = $9.00, Parking = $39.50, Pension = min(100.00, 128.00) = $100.00. Total deductions = $217.50.
- Net before credits = 1280.00 - 417.92 - 217.50 = $644.58.
- Rule 10 Credit: ltd=1 and std=1 -> +$5.00.
- Final net pay = 644.58 + 5.00 = $649.58.
- **Expected: 649.58**
