# Project 2: Payroll (Basic)

Testbed application for software testing research benchmarking test-case generation methods with seeded defects.

## 1. Overview
Deterministic, rule-based Java application calculating employee net salary based on hourly rate, work hours (regular, overtime, holiday), tax brackets, and pre-tax benefit deductions.

- **Language:** Java 17
- **Build Tool:** Maven
- **Testing:** JUnit 5
- **No external frameworks or libraries.**

---

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

## 3. Paper Sample Verification & Difference Analysis
**Sample Input:**
- `hourly = 24.00`, `regularHours = 36.0`, `overtimeHours = 0`, `holidayHours = 0`
- `medical = 0`, `dental = 1` ($5.00), `vision = 0`, `unitedWay = 0`
- `pension = 40.00`, `LTD = 0`, `STD = 1` ($9.00), `parking = 0`

**Hand-Calculated Step-by-Step Values:**
1. `Gross = 24.00 * 36 = 864.00`
2. `Federal Tax (22%) = 864.00 * 0.22 = 190.08`
3. `Social Security (6.2%) = 864.00 * 0.062 = 53.568`
4. `Medicare (1.45%) = 864.00 * 0.0145 = 12.528`
5. `Total Taxes = 190.08 + 53.568 + 12.528 = 256.176`
6. `Total Deductions = $5.00 (Dental) + $40.00 (Pension) + $9.00 (STD) = $54.00`
7. `Exact Unrounded Net = 864.00 - 256.176 - 54.00 = 553.824`
8. `Final Net (HALF_UP, 2 decimals) = 553.82`

**Comparison with Paper:**
- The paper reported `553.8231`.
- The exact unrounded calculation yields `553.824`. The difference between the two values is below 0.001 (0.0009), and both values round identically to **`$553.82`**.

---

## 4. Build & Run Commands (PowerShell)

### Build and Run Tests
```powershell
# Set environment PATH if needed
$env:Path = [Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")

# Run Maven tests
mvn -q test -f "D:\SWT301_TEST\Project2\pom.xml"
```

### Running CLI with a Specific Defect
Defect selection is made via system property `-Ddefect=<DEFECT_ID>` (default: `NONE`):
```powershell
# Compile
mvn compile -f "D:\SWT301_TEST\Project2\pom.xml"

# Run with defect NONE
java -cp "D:\SWT301_TEST\Project2\target\classes" com.payroll.Main 24.00 36 0 0 0 1 0 0 40.0 0 1 0

# Run with defect D2
java -Ddefect=D2 -cp "D:\SWT301_TEST\Project2\target\classes" com.payroll.Main 24.00 36 0 0 0 1 0 0 40.0 0 1 0
```

### Running the Defect Runner
The `DefectRunner` runs all test cases from `testcases/input.csv` against all defects (`NONE`, `D1`..`D10`), generates `results/defect_matrix.csv`, and displays failing cases for each defect:
```powershell
java -cp "D:\SWT301_TEST\Project2\target\classes" com.payroll.DefectRunner "D:\SWT301_TEST\Project2\testcases\input.csv" "D:\SWT301_TEST\Project2\results\defect_matrix.csv"
```

---

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