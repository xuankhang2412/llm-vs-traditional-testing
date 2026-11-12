# Project 4: Enhanced Payroll Calculator

Deterministic, rule-based Java application calculating weekly payroll (gross pay, federal/state/FICA taxes, voluntary benefit deductions, and credits). Testbed application for SWT301 (Software Testing) benchmarking test-case generation with 20 seeded defects (D1–D20).

- **Language:** Java 17
- **Build Tool:** Maven
- **Testing:** JUnit 5 (JUnit Jupiter 5.10.2)
- **Package:** `com.payroll.enhanced`

---

## 1. Overview & How to Run

### Build and Run Tests
```powershell
mvn test -f "D:\SWT301_TEST\Project4\pom.xml"
```

### CLI Execution
```powershell
# Compile
mvn compile -f "D:\SWT301_TEST\Project4\pom.xml"

# Run with clean baseline (Defect.NONE)
java -cp "D:\SWT301_TEST\Project4\target\classes" com.payroll.enhanced.Main 25.0 40.0 8.0 0.0 2 1 1 0 0.0 0.0 0 0 0

# Run with defect D1 injected
java -Ddefect=D1 -cp "D:\SWT301_TEST\Project4\target\classes" com.payroll.enhanced.Main 25.0 40.0 8.0 0.0 2 1 1 0 0.0 0.0 0 0 0
```

### Defect Runner Execution
```powershell
java -cp "D:\SWT301_TEST\Project4\target\classes" com.payroll.enhanced.DefectRunner "D:\SWT301_TEST\Project4\testcases\sample_payroll_tests.csv" "D:\SWT301_TEST\Project4\results\defect_matrix.csv"
```

---

## 2. Inputs Specification

| # | Field | Range | Description |
|---|---|---|---|
| 1 | `hourly` | `15.00 <= h <= 50.00` | Hourly rate in dollars |
| 2 | `regularHours` | `>= 0` | Regular hours worked (straight time) |
| 3 | `overtimeHours` | `>= 0` | Overtime hours worked (1.5x regular pay) |
| 4 | `holidayHours` | `>= 0` | Holiday hours worked (2.0x regular pay) |
| 5 | `state` | `1–3` | 1=Tennessee, 2=Arkansas, 3=Mississippi |
| 6 | `medical` | `0–3` | 0=None, 1=Plan 1 ($50), 2=Plan 2 ($75), 3=Plan 3 ($125) |
| 7 | `dental` | `0–2` | 0=None, 1=Plan 1 ($5), 2=Plan 2 ($10) |
| 8 | `vision` | `0–2` | 0=None, 1=Plan 1 ($5), 2=Plan 2 ($12) |
| 9 | `unitedWay` | `>= 0` | Fixed dollar voluntary contribution |
| 10 | `pension` | `>= 0` | Fixed dollar contribution (capped at 10% of gross) |
| 11 | `ltd` | `0–1` | Long-Term Disability: 0=No, 1=Yes ($14.00) |
| 12 | `std` | `0–1` | Short-Term Disability: 0=No, 1=Yes ($9.00) |
| 13 | `parking` | `0–1` | Parking deduction: 0=No, 1=Yes ($39.50) |

---

## 3. Calculation Formulas & Tax Brackets

### Gross Pay Formula
`gross = basePay + overtimePay + holidayPay + bonuses`
- `basePay = hourly * regularHours`
- `overtimePay = hourly * overtimeHours * 1.5`
- `holidayPay = hourly * holidayHours * 2.0`
- `bonuses`:
  - N2: `overtimeHours > 5` -> +$50.00
  - N3: `holidayHours > 5` -> +$75.00

### Taxes
- **Federal Tax on Gross** (bracket by hourly rate):
  - $15.00 – $20.00: 10%
  - $20.01 – $40.00: 22%
  - $40.01 – $50.00: 35%
- **FICA**:
  - Social Security: 6.2% of gross
  - Medicare: 1.45% of gross
- **State Tax on Gross** (bracket by hourly rate):
  - **Tennessee (State 1)**:
    - h <= $20.00: 0%
    - $20.01 – $35.00: 4%
    - h > $35.00: 8%
  - **Arkansas (State 2)**:
    - $15.00 – $30.00: 3%
    - h > $30.00: 6%
  - **Mississippi (State 3)**:
    - $15.00 – $25.00: 2%
    - $25.01 – $40.00: 3%
    - $40.01 – $50.00: 5%

### Deductions
- Medical: P1=$50, P2=$75, P3=$125 (0 if h <= $20 under N6)
- Dental: P1=$5, P2=$10 (0 if h <= $20 under N6)
- Vision: P1=$5, P2=$12 (0 if h <= $20 under N6)
- LTD: $14.00 (if ltd == 1)
- STD: $9.00 (if std == 1)
- Parking: $39.50 (if parking == 1)
- United Way: exact contribution amount
- Pension: `min(pension, 10% of gross)`

### Net Pay Formula
`net = gross - federal - state - SS - Medicare - deductions + credits`
- **Credits**:
  - N7: `medical == 3 && dental == 2 && vision == 2` -> +$15.00 credit to net
  - N8: `unitedWay >= 10.00` -> +15% of unitedWay contribution added to net
  - N10: `ltd == 1 && std == 1` -> +$5.00 credit to net

---

## 4. Business Rules & Notes

- **N1**: If `hourly` in `[15.00, 20.00]`, then `overtimeHours <= 10` (reject if exceeded).
- **N2**: If `overtimeHours > 5` -> +$50.00 bonus added to gross.
- **N3**: If `holidayHours > 5` -> +$75.00 bonus added to gross.
- **N4**: `medical != 0` requires `dental != 0` (reject if medical enrolled without dental).
- **N5**: If `hourly >= 40.01`, medical must be plan 2 or 3 (reject plans 0 and 1).
- **N6**: If `hourly <= 20.00`, medical, dental, and vision deductions are $0.00.
- **N7**: If `medical == 3 && dental == 2 && vision == 2`, credit +$15.00 added to net pay.
- **N8**: If `unitedWay >= 10.00`, credit of 15% of United Way contribution added to net pay.
- **N9**: `ltd == 1` requires `std == 1` (reject if LTD elected without STD).
- **N10**: If `ltd == 1 && std == 1`, credit +$5.00 added to net pay.

---

## 5. Assumptions

1. **Pay Period**: Weekly pay period calculations.
2. **Taxable Bonuses**: Overtime bonus (N2) and holiday bonus (N3) are included in gross pay before calculating federal, state, and FICA taxes.
3. **Net Pay Credits**: Credits (N7, N8, N10) are applied directly to net pay after all taxes and deductions have been subtracted.
4. **Interpretation of N5**: N5 is interpreted literally: employees with hourly rate >= $40.01 must be enrolled in medical plan 2 or 3 (they cannot opt out with plan 0, nor choose plan 1).
5. **Tax Bracket Lookups**: Federal and state tax rates are determined based on the employee's base hourly wage, not total gross earnings.
6. **Monetary Precision**: All calculations use `BigDecimal` with final gross and net rounded HALF_UP to 2 decimal places.

---

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

## 7. Defect Type Distribution

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **conditional** | 6 | 30.0% | D2, D6, D9, D15, D16, D19 |
| **arithmetic** | 5 | 25.0% | D1, D3, D7, D13, D14 |
| **boundary** | 5 | 25.0% | D5, D8, D11, D12, D17 |
| **logic** | 4 | 20.0% | D4, D10, D18, D20 |
| **Total** | **20** | **100.0%** | D1–D20 |
