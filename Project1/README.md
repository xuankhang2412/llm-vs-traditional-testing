# Project 1: Package Shipping (Basic)

Testbed application for software testing research benchmarking test-case generation methods with seeded defects.

## 1. Overview
Deterministic, rule-based Java application calculating overnight package shipping costs based on weight, origin/destination zones, delivery speed options, and special service surcharges.

- **Language:** Java 17
- **Build Tool:** Maven
- **Testing:** JUnit 5
- **No external frameworks or libraries.**

---

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

## 3. Build & Run Commands (PowerShell)

### Build and Run Tests
```powershell
# Set environment PATH if needed
$env:Path = [Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")

# Run Maven tests
mvn -q test -f "D:\SWT301_TEST\Project1\pom.xml"
```

### Running CLI with a Specific Defect
The defect is selected via system property `-Ddefect=<DEFECT_ID>` (default: `NONE`):
```powershell
# Compile
mvn compile -f "D:\SWT301_TEST\Project1\pom.xml"

# Run with defect NONE
java -cp "D:\SWT301_TEST\Project1\target\classes" com.shipping.Main 10.0 1 1 4 0 0 0 0 0 0

# Run with defect D3
java -Ddefect=D3 -cp "D:\SWT301_TEST\Project1\target\classes" com.shipping.Main 10.0 1 1 4 0 0 0 0 0 0
```

### Running the Defect Runner
The `DefectRunner` executes all test cases from `testcases/input.csv` against all defects (`NONE`, `D1`..`D10`), generates `results/defect_matrix.csv`, and displays which test cases fail for each defect:
```powershell
java -cp "D:\SWT301_TEST\Project1\target\classes" com.shipping.DefectRunner "D:\SWT301_TEST\Project1\testcases\input.csv" "D:\SWT301_TEST\Project1\results\defect_matrix.csv"
```

---

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