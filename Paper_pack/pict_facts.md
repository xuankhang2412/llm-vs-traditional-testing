# PICT Pairwise Test Generation: Model Specifications, Constraints, Suite Sizing, and Invalid Test Audit

**Document Purpose:** This document records the empirical and structural facts regarding the six Microsoft PICT pairwise test suites across the four benchmark applications: `BasicPackage`, `BasicPayroll`, `EnhancedPackage`, and `EnhancedPayroll`. All contents are derived directly from the authoritative model files (`Tools/models/*.model`), test suite CSV files (`Suites/*/PICT_*`), audit logs, and project specifications.

---

## 1. Parameters and Representative Values Across the 6 PICT Models

In pairwise combinatorial testing via Microsoft PICT (`pict.exe`), continuous numeric domains must be partitioned into discrete representative values. Across all six models, discrete parameters encompass their full valid domain, while continuous parameters were selected either as **range midpoints** of specification-defined tiers or as domain-representative operational constants.

### 1.1 BasicPackage (`PICT_plain`)
* **Model File:** `Tools/models/basic_package.model`
* **Total Parameters:** 10 (1 continuous, 9 discrete/boolean)

| Parameter Name | Specification Domain | Type | Representative Values in PICT Model | Range Midpoint? / Selection Rationale |
|---|---|---|---|---|
| `weight` | `0.0 < weight <= 60.0` | Continuous (Float) | `30.0` | **Yes (Range Midpoint)**: $(0.0 + 60.0) / 2 = 30.0$, the exact mathematical midpoint of the entire allowable parcel weight domain. |
| `origin` | `1` to `4` | Discrete (Integer) | `1, 2, 3, 4` | Full enumeration of all 4 domestic shipping zones. |
| `destination` | `1` to `4` | Discrete (Integer) | `1, 2, 3, 4` | Full enumeration of all 4 domestic shipping zones. |
| `deliveryOption` | `1` to `7` | Discrete (Integer) | `1, 2, 3, 4, 5, 6, 7` | Full enumeration of all 7 delivery speed options. |
| `signatureOption` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration (0 = None, 1 = Indirect, 2 = Direct). |
| `deliveryLocationType` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration (0 = Business, 1 = Residence). |
| `saturdayDelivery` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `holdAtLocation` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `fragile` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `dangerous` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |

---

### 1.2 BasicPayroll (`PICT_plain`)
* **Model File:** `Tools/models/basic_payroll.model`
* **Total Parameters:** 12 (6 continuous, 6 discrete/boolean)

| Parameter Name | Specification Domain | Type | Representative Values in PICT Model | Range Midpoint? / Selection Rationale |
|---|---|---|---|---|
| `hourlySalary` | `$15.00 <= hourlySalary <= $50.00` | Continuous (Float) | `17.50, 30.00, 45.00` | **Yes (Range Midpoints)**: The specification defines three federal tax tiers. The chosen values are the exact mathematical midpoints of each tier:<br>• Bracket 1 ($15.00–$20.00): $(15.00 + 20.00) / 2 = 17.50$<br>• Bracket 2 ($20.01–$40.00): $(20.00 + 40.00) / 2 = 30.00$<br>• Bracket 3 ($40.01–$50.00): $(40.00 + 50.00) / 2 = 45.00$ |
| `regularHours` | `>= 0.0` | Continuous (Float) | `40.0` | **No (Domain Constant)**: Standard full-time workweek representative constant. |
| `overtimeHours` | `>= 0.0` | Continuous (Float) | `0.0, 5.0` | **No (Domain Representatives)**: Lower boundary zero (0.0) and typical moderate overtime shift (5.0). |
| `holidayHours` | `>= 0.0` | Continuous (Float) | `0.0, 8.0` | **No (Domain Representatives)**: Lower boundary zero (0.0) and standard full holiday shift (8.0). |
| `medicalPlan` | `0` to `3` | Discrete (Integer) | `0, 1, 2, 3` | Full enumeration of all 4 medical insurance tiers. |
| `dentalPlan` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration of all 3 dental insurance tiers. |
| `visionPlan` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration of all 3 vision insurance tiers. |
| `unitedWay` | `>= 0.0` | Continuous (Float) | `0.0, 15.0` | **No (Domain Representatives)**: Lower boundary zero ($0.00) and representative voluntary deduction ($15.00). |
| `pension` | `>= 0.0` | Continuous (Float) | `0.0, 50.0` | **No (Domain Representatives)**: Lower boundary zero ($0.00) and representative voluntary contribution ($50.00). |
| `longTermDisability`| `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `shortTermDisability`| `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `parking` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |

---

### 1.3 EnhancedPackage (`PICT_plain` and `PICT_constrained`)
* **Model Files:** `Tools/models/enhanced_package_plain.model` and `Tools/models/enhanced_package_constrained.model`
* **Total Parameters:** 11 (1 continuous, 10 discrete/boolean)

| Parameter Name | Specification Domain | Type | Representative Values in PICT Model | Range Midpoint? / Selection Rationale |
|---|---|---|---|---|
| `weight` | `0.0 < weight <= 60.0` | Continuous (Float) | `1.0, 6.0, 17.5, 32.5, 45.0, 55.0` | **Yes (Range Midpoints)**: The 6 values correspond to the exact mathematical midpoints of the packaging capacity tiers and heavy parcel threshold:<br>• Envelope ($0.0 < w \le 2.0$): $(0.0 + 2.0) / 2 = 1.0$<br>• Small Box / Tube ($2.0 < w \le 10.0$): $(2.0 + 10.0) / 2 = 6.0$<br>• Medium Box ($10.0 < w \le 25.0$): $(10.0 + 25.0) / 2 = 17.5$<br>• Large Box ($25.0 < w \le 40.0$): $(25.0 + 40.0) / 2 = 32.5$<br>• Customer Packaged normal ($40.0 < w \le 50.0$): $(40.0 + 50.0) / 2 = 45.0$<br>• Heavy parcel ($50.0 < w \le 60.0$): $(50.0 + 60.0) / 2 = 55.0$ |
| `origin` | `1` to `4` | Discrete (Integer) | `1, 2, 3, 4` | Full enumeration of all 4 shipping zones. |
| `destination` | `1` to `4` | Discrete (Integer) | `1, 2, 3, 4` | Full enumeration of all 4 shipping zones. |
| `deliveryOption` | `1` to `7` | Discrete (Integer) | `1, 2, 3, 4, 5, 6, 7` | Full enumeration of all 7 delivery speed options. |
| `packaging` | `1` to `8` | Discrete (Integer) | `1, 2, 3, 4, 5, 6, 7, 8` | Full enumeration of all 8 packaging categories. |
| `signature` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration (0 = None, 1 = Indirect, 2 = Direct). |
| `locationType` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration (0 = Business, 1 = Residence). |
| `saturday` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `hold` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `fragile` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `dangerous` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |

---

### 1.4 EnhancedPayroll (`PICT_plain` and `PICT_constrained`)
* **Model Files:** `Tools/models/enhanced_payroll_plain.model` and `Tools/models/enhanced_payroll_constrained.model`
* **Total Parameters:** 13 (6 continuous, 7 discrete/boolean)

| Parameter Name | Specification Domain | Type | Representative Values in PICT Model | Range Midpoint? / Selection Rationale |
|---|---|---|---|---|
| `hourly` | `$15.00 <= hourly <= $50.00` | Continuous (Float) | `17.50, 22.50, 27.50, 32.50, 37.50, 45.00` | **Yes (Range Midpoints)**: The 6 values represent exact mathematical midpoints across composite state and federal tax tiers:<br>• $15.00–$20.00: $(15 + 20) / 2 = 17.50$ (Fed Tier 1, TN Tier 1, AR Tier 1, MS Tier 1)<br>• $20.01–$25.00: $(20 + 25) / 2 = 22.50$ (MS Tier 1, TN Tier 2)<br>• $25.01–$30.00: $(25 + 30) / 2 = 27.50$ (MS Tier 2, AR Tier 1)<br>• $30.01–$35.00: $(30 + 35) / 2 = 32.50$ (AR Tier 2, TN Tier 2, MS Tier 2)<br>• $35.01–$40.00: $(35 + 40) / 2 = 37.50$ (TN Tier 3, MS Tier 2)<br>• $40.01–$50.00: $(40 + 50) / 2 = 45.00$ (Fed Tier 3, MS Tier 3, High-Earner Rule) |
| `regularHours` | `>= 0.0` | Continuous (Float) | `40.0` | **No (Domain Constant)**: Standard full-time workweek representative constant. |
| `overtimeHours` | `>= 0.0` | Continuous (Float) | `0.0, 3.0, 8.0, 12.0` | **No (Strategic Threshold Representatives)**:<br>• `0.0`: Zero overtime baseline.<br>• `3.0`: Non-zero overtime strictly below the 5.0 bonus threshold.<br>• `8.0`: Overtime strictly above 5.0 (qualifies for bonus) but below 10.0.<br>• `12.0`: Overtime exceeding the 10.0 low-wage threshold (violates Rule 1 if low wage). |
| `holidayHours` | `>= 0.0` | Continuous (Float) | `0.0, 3.0, 8.0` | **No (Strategic Threshold Representatives)**:<br>• `0.0`: Zero holiday baseline.<br>• `3.0`: Holiday hours below the 5.0 attendance bonus threshold.<br>• `8.0`: Full holiday shift above the 5.0 bonus threshold. |
| `state` | `1, 2, 3` | Discrete (Integer) | `1, 2, 3` | Full enumeration of all 3 state jurisdictions (1 = TN, 2 = AR, 3 = MS). |
| `medical` | `0` to `3` | Discrete (Integer) | `0, 1, 2, 3` | Full enumeration of all 4 medical insurance plans. |
| `dental` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration of all 3 dental insurance plans. |
| `vision` | `0` to `2` | Discrete (Integer) | `0, 1, 2` | Full enumeration of all 3 vision insurance plans. |
| `unitedWay` | `>= 0.0` | Continuous (Float) | `0.0, 5.0, 20.0` | **No (Strategic Threshold Representatives)**:<br>• `0.0`: Zero donation baseline.<br>• `5.0`: Contribution below the $10.00 community incentive credit threshold.<br>• `20.0`: Contribution meeting/exceeding the $10.00 credit threshold. |
| `pension` | `>= 0.0` | Continuous (Float) | `0.0, 50.0, 200.0` | **No (Strategic Threshold Representatives)**:<br>• `0.0`: Zero pension contribution.<br>• `50.0`: Moderate contribution within the 10% gross salary cap.<br>• `200.0`: High contribution designed to test the 10% gross salary cap. |
| `ltd` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `std` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |
| `parking` | `0` or `1` | Boolean (Integer) | `0, 1` | Full enumeration of flag (0 = No, 1 = Yes). |

---

## 2. Constraints in the Two Constrained Models

The two constrained models (`enhanced_package_constrained.model` and `enhanced_payroll_constrained.model`) enforce specification validation rules as hard constraints during pairwise combination.

### 2.1 EnhancedPackage Constrained Model
* **Model File:** `Tools/models/enhanced_package_constrained.model` (Lines 13–30)

```pict
# Packaging capacity limits
IF [packaging] = 1 THEN [weight] = 1.0;
IF [packaging] = 2 THEN [weight] IN {1.0, 6.0};
IF [packaging] = 3 THEN [weight] IN {1.0, 6.0, 17.5};
IF [packaging] = 4 THEN [weight] IN {1.0, 6.0, 17.5, 32.5};
IF [packaging] = 5 THEN [weight] IN {1.0, 6.0};

# Rule 4: Business location requires signature 1 or 2
IF [locationType] = 0 THEN [signature] IN {1, 2};

# Rule 5: Weight > 50 requires signature 1 or 2
IF [weight] = 55.0 THEN [signature] IN {1, 2};

# Rule 7: Fragile and dangerous cannot be combined
NOT ([fragile] = 1 AND [dangerous] = 1);

# Rule 8: Dangerous requires deliveryOption in {1,2,3,4}
IF [dangerous] = 1 THEN [deliveryOption] IN {1, 2, 3, 4};
```

#### Detailed Rule-by-Rule Encoding:
1. `IF [packaging] = 1 THEN [weight] = 1.0;`
   - **Specification Rule:** Packaging Code 1 (Envelope) has a maximum capacity of $\le 2.0$ lbs.
   - **Encoding Mechanism:** Among the model's weight choices (`1.0, 6.0, 17.5, 32.5, 45.0, 55.0`), only `1.0` satisfies $w \le 2.0$.
2. `IF [packaging] = 2 THEN [weight] IN {1.0, 6.0};`
   - **Specification Rule:** Packaging Code 2 (Small Box) has a maximum capacity of $\le 10.0$ lbs.
   - **Encoding Mechanism:** Restricts weight to the available choices $\le 10.0$ (`1.0, 6.0`).
3. `IF [packaging] = 3 THEN [weight] IN {1.0, 6.0, 17.5};`
   - **Specification Rule:** Packaging Code 3 (Medium Box) has a maximum capacity of $\le 25.0$ lbs.
   - **Encoding Mechanism:** Restricts weight to the available choices $\le 25.0$ (`1.0, 6.0, 17.5`).
4. `IF [packaging] = 4 THEN [weight] IN {1.0, 6.0, 17.5, 32.5};`
   - **Specification Rule:** Packaging Code 4 (Large Box) has a maximum capacity of $\le 40.0$ lbs.
   - **Encoding Mechanism:** Restricts weight to the available choices $\le 40.0$ (`1.0, 6.0, 17.5, 32.5`).
5. `IF [packaging] = 5 THEN [weight] IN {1.0, 6.0};`
   - **Specification Rule:** Packaging Code 5 (Tube) has a maximum capacity of $\le 10.0$ lbs.
   - **Encoding Mechanism:** Restricts weight to the available choices $\le 10.0$ (`1.0, 6.0`).
   - *(Note: Packaging codes 6, 7, and 8 represent customer packaging with limits up to 60.0 lbs; all 6 weight choices are valid, so no restriction is required).*
6. `IF [locationType] = 0 THEN [signature] IN {1, 2};`
   - **Specification Rule:** Special Rule 4 / Note N4 (Business Signature Requirement). Deliveries to commercial addresses (`locationType = 0`) require indirect (`1`) or direct (`2`) signature. Unsigned delivery (`signature = 0`) must be rejected.
7. `IF [weight] = 55.0 THEN [signature] IN {1, 2};`
   - **Specification Rule:** Special Rule 5 / Note N5 (Heavy Parcel Signature Requirement). Parcels exceeding 50.0 lbs (`weight > 50.0`) require signature option 1 or 2. Since `55.0` is the only model weight choice $> 50.0$, it is restricted to signature `1` or `2`.
8. `NOT ([fragile] = 1 AND [dangerous] = 1);`
   - **Specification Rule:** Special Rule 7 / Note N7 (Fragile and Dangerous Incompatibility). Simultaneous fragile handling and hazardous goods shipment is illegal.
9. `IF [dangerous] = 1 THEN [deliveryOption] IN {1, 2, 3, 4};`
   - **Specification Rule:** Special Rule 8 / Note N8 (Dangerous Goods Permitted Services). Hazardous shipments are only permitted on Same Day (`1`), First Overnight (`2`), Priority Overnight (`3`), or Standard Overnight (`4`). Options 5, 6, and 7 are prohibited.

---

### 2.2 EnhancedPayroll Constrained Model
* **Model File:** `Tools/models/enhanced_payroll_constrained.model` (Lines 15–25)

```pict
# Rule 1: Low-Wage Overtime Limit
IF [hourly] = 17.50 THEN [overtimeHours] IN {0.0, 3.0, 8.0};

# Rule 4: Medical requires Dental
IF [medical] IN {1, 2, 3} THEN [dental] IN {1, 2};

# Rule 5: High-Earner Medical Plan
IF [hourly] = 45.00 THEN [medical] IN {2, 3};

# Rule 9: LTD requires STD
IF [ltd] = 1 THEN [std] = 1;
```

#### Detailed Rule-by-Rule Encoding:
1. `IF [hourly] = 17.50 THEN [overtimeHours] IN {0.0, 3.0, 8.0};`
   - **Specification Rule:** Special Rule 1 / Note N1 (Low-Wage Overtime Limit). Hourly employees earning $\$15.00 \le \text{hourly} \le \$20.00$ are prohibited from working more than 10.0 overtime hours.
   - **Encoding Mechanism:** `17.50` is the only model choice within $\$15.00–\$20.00$. The constraint forbids `overtimeHours = 12.0`, permitting only `0.0, 3.0, 8.0`.
2. `IF [medical] IN {1, 2, 3} THEN [dental] IN {1, 2};`
   - **Specification Rule:** Special Rule 4 / Note N4 (Medical-Dental Dependency). Enrollment in any medical insurance plan (`medical != 0`) requires enrollment in a dental plan (`dental != 0`).
   - **Encoding Mechanism:** When medical is 1, 2, or 3, dental cannot be 0.
3. `IF [hourly] = 45.00 THEN [medical] IN {2, 3};`
   - **Specification Rule:** Special Rule 5 / Note N5 (High-Earner Medical Plan Mandate). Employees earning $\ge \$40.01/\text{hr}$ must enroll in medical plan 2 or 3.
   - **Encoding Mechanism:** `45.00` is the only model choice $\ge \$40.01$. The constraint restricts medical to `2` or `3`, prohibiting plan 0 and plan 1.
4. `IF [ltd] = 1 THEN [std] = 1;`
   - **Specification Rule:** Special Rule 9 / Note N9 (Disability Hierarchy). Long-term disability coverage requires having short-term disability coverage.
   - **Encoding Mechanism:** Forbids the invalid combination `ltd = 1, std = 0`.

---

## 3. Raw Rows Generated vs. Final Suite Rows and Sampling Seeds

Benchmark test quotas were established as $N = 20$ for Basic applications and $N = 35$ for Enhanced applications.

### 3.1 Suite Sizing and Sampling Summary

| Application | Suite Name | Test Quota | Raw PICT Size | Final Suite Size | Saturated / Downsampled | Sampling Seed Used |
|---|---|:---:|:---:|:---:|---|:---:|
| **BasicPackage** | `PICT_plain` | 20 | 32 | **20** | Downsampled | **Seed 42** |
| **BasicPayroll** | `PICT_plain` | 20 | 16 | **16** | Saturated below quota | *None (No sampling)* |
| **EnhancedPackage** | `PICT_plain` | 35 | 61 | **35** | Downsampled | **Seed 42** |
| **EnhancedPackage** | `PICT_constrained` | 35 | 63 | **35** | Downsampled | **Seed 42** |
| **EnhancedPayroll** | `PICT_plain` | 35 | 27 | **27** | Saturated below quota | *None (No sampling)* |
| **EnhancedPayroll** | `PICT_constrained` | 35 | 32 | **32** | Saturated below quota | *None (No sampling)* |

### 3.2 Methodological Sizing Rules
1. **Downsampling via Seed 42:** For test suites where Microsoft PICT generated more test vectors than the target quota (`BasicPackage plain` [32 $\to$ 20], `EnhancedPackage plain` [61 $\to$ 35], and `EnhancedPackage constrained` [63 $\to$ 35]), a seeded pseudo-random subsample was taken using **seed 42** to preserve testbed reproducibility while strictly maintaining quota comparability.
2. **Saturation Below Quota:** For test suites where pairwise combinatorial interaction coverage was fully achieved with fewer rows than the quota (`BasicPayroll plain` [16 rows], `EnhancedPayroll plain` [27 rows], and `EnhancedPayroll constrained` [32 rows]), **all raw generated rows were retained without padding or downsampling**. In pairwise testing, generating additional rows once 100% 2-way tuple coverage is satisfied would add redundant tuples without providing combinatorial value.

---

## 4. PICT Execution Time Measurement

* **Measurement Status:** **Not individually measured / Not recorded via benchmark timer.**
* **Recorded Value in Benchmark Records:**
  - In [`Results/cost_comparison.csv`](file:///D:/SWT301_TEST/Results/cost_comparison.csv), all six PICT suites are assigned a nominal value of `0.01` minutes (equivalent to 0.6 seconds), labeled under `time_type = automated_execution` with the note: `"Pairwise combinatorial model generation <1s"`.
  - In [`Results/scripts/MetricsCalculator.java`](file:///D:/SWT301_TEST/Results/scripts/MetricsCalculator.java) (line 582), the execution time is hardcoded as `0.01` min.
  - In [`Results/summary.md`](file:///D:/SWT301_TEST/Results/summary.md) Section 10 and Table 8, PICT wall-clock time is characterized as `<0.01 min` per application and `<0.04 min` across all 4 applications.
* **Explanation:** PICT CLI execution completes virtually instantaneously (sub-second) on standard desktop hardware. Because high-precision OS process timing was not integrated into the generation script, a nominal conservative ceiling of $<1\text{s}$ was documented rather than an exact millisecond measurement.

---

## 5. Invalid Row Audit in Enhanced `PICT_plain` Suites

Because `PICT_plain` generates all pairwise parameter tuples without domain constraints, it inadvertently generates invalid inputs that violate application validation rules.

### 5.1 EnhancedPackage `PICT_plain`
* **Total Rows in Suite:** 35
* **Total Invalid Rows:** **25** (71.43%)
* **Total Valid Rows:** **10** (28.57%)

#### Breakdown by Violated Rule:
1. **Packaging Weight Capacity Exceeded:** **14 tests** (56.0% of invalid tests)
2. **Note N4 (Business location `locationType=0` requires signature 1 or 2; got `sig=0`):** **6 tests** (24.0% of invalid tests)
3. **Note N7 (Incompatibility: `fragile=1` and `dangerous=1` cannot be combined):** **5 tests** (20.0% of invalid tests)

#### Complete Row-by-Row Listing:

| Test ID | Test Vector (`[w, orig, dest, deliv, pack, sig, loc, sat, hold, frag, dang]`) | Violated Rule | Reference Implementation Error Message |
|---|---|---|---|
| **TC01** | `[1.0, 4, 1, 2, 5, 0, 1, 1, 0, 1, 1]` | Note N7 (Fragile & Dangerous) | `Fragile and dangerous cannot be combined (N7)` |
| **TC03** | `[17.5, 4, 2, 4, 5, 2, 1, 0, 0, 1, 1]` | Packaging Weight Limit | `Tube weight limit exceeded (got 17.5)` |
| **TC04** | `[6.0, 3, 1, 4, 3, 0, 0, 1, 1, 0, 0]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC05** | `[32.5, 3, 3, 5, 1, 0, 1, 1, 0, 1, 1]` | Packaging Weight Limit | `Envelope weight limit exceeded (got 32.5)` |
| **TC06** | `[6.0, 1, 2, 5, 3, 2, 1, 0, 0, 1, 1]` | Note N7 (Fragile & Dangerous) | `Fragile and dangerous cannot be combined (N7)` |
| **TC07** | `[45.0, 1, 1, 3, 8, 2, 1, 1, 0, 1, 1]` | Note N7 (Fragile & Dangerous) | `Fragile and dangerous cannot be combined (N7)` |
| **TC08** | `[55.0, 3, 4, 5, 2, 2, 1, 0, 1, 1, 0]` | Packaging Weight Limit | `Small Box weight limit exceeded (got 55.0)` |
| **TC10** | `[45.0, 4, 4, 5, 4, 1, 1, 0, 1, 1, 1]` | Packaging Weight Limit | `Large Box weight limit exceeded (got 45.0)` |
| **TC11** | `[55.0, 3, 2, 2, 4, 2, 0, 1, 1, 1, 0]` | Packaging Weight Limit | `Large Box weight limit exceeded (got 55.0)` |
| **TC15** | `[32.5, 3, 2, 3, 5, 2, 0, 1, 1, 0, 1]` | Packaging Weight Limit | `Tube weight limit exceeded (got 32.5)` |
| **TC18** | `[6.0, 4, 2, 4, 2, 0, 0, 0, 1, 0, 1]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC20** | `[1.0, 3, 1, 6, 1, 2, 0, 1, 1, 1, 1]` | Note N7 (Fragile & Dangerous) | `Fragile and dangerous cannot be combined (N7)` |
| **TC21** | `[17.5, 1, 1, 2, 6, 0, 0, 0, 1, 0, 0]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC23** | `[55.0, 4, 4, 6, 8, 0, 0, 1, 1, 1, 0]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC24** | `[32.5, 4, 4, 7, 2, 0, 1, 0, 0, 0, 0]` | Packaging Weight Limit | `Small Box weight limit exceeded (got 32.5)` |
| **TC25** | `[17.5, 2, 2, 3, 2, 1, 0, 0, 0, 1, 0]` | Packaging Weight Limit | `Small Box weight limit exceeded (got 17.5)` |
| **TC27** | `[32.5, 4, 3, 3, 4, 0, 0, 0, 0, 1, 1]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC28** | `[45.0, 4, 1, 3, 3, 0, 0, 0, 0, 1, 0]` | Packaging Weight Limit | `Medium Box weight limit exceeded (got 45.0)` |
| **TC29** | `[45.0, 1, 1, 7, 1, 2, 0, 0, 0, 0, 1]` | Packaging Weight Limit | `Envelope weight limit exceeded (got 45.0)` |
| **TC30** | `[45.0, 2, 1, 2, 1, 1, 0, 1, 0, 0, 1]` | Packaging Weight Limit | `Envelope weight limit exceeded (got 45.0)` |
| **TC31** | `[55.0, 1, 1, 3, 1, 0, 1, 0, 1, 0, 0]` | Packaging Weight Limit | `Envelope weight limit exceeded (got 55.0)` |
| **TC32** | `[32.5, 4, 1, 6, 6, 1, 1, 1, 1, 1, 1]` | Note N7 (Fragile & Dangerous) | `Fragile and dangerous cannot be combined (N7)` |
| **TC33** | `[1.0, 4, 1, 3, 2, 0, 0, 1, 1, 0, 1]` | Note N4 (Business Signature) | `Business location requires signature 1 or 2 (got 0)` |
| **TC34** | `[55.0, 3, 3, 4, 5, 1, 1, 1, 0, 0, 1]` | Packaging Weight Limit | `Tube weight limit exceeded (got 55.0)` |
| **TC35** | `[32.5, 4, 3, 4, 1, 0, 1, 0, 1, 0, 0]` | Packaging Weight Limit | `Envelope weight limit exceeded (got 32.5)` |

---

### 5.2 EnhancedPayroll `PICT_plain`
* **Total Rows in Suite:** 27
* **Total Invalid Rows:** **11** (40.74%)
* **Total Valid Rows:** **16** (59.26%)

#### Breakdown by Violated Rule:
1. **Note N4 (Medical requires Dental; `medical > 0` but `dental = 0`):** **5 tests** (45.45% of invalid tests)
2. **Note N9 (Disability Hierarchy; `ltd = 1` requires `std = 1`; got `ltd=1, std=0`):** **3 tests** (27.27% of invalid tests)
3. **Note N5 (High-Earner Medical Plan Mandate; `hourly >= 40.01` requires plan 2 or 3; got plan 0 or 1):** **2 tests** (18.18% of invalid tests)
4. **Note N1 (Low-Wage Overtime Limit; hourly $\$15–\$20$ limits overtime $\le 10.0\text{h}$; got 12.0h):** **1 test** (9.09% of invalid tests)

#### Complete Row-by-Row Listing:

| Test ID | Test Vector (`[h, reg, ot, hol, st, med, den, vis, uw, pen, ltd, std, park]`) | Violated Rule | Reference Implementation Error Message |
|---|---|---|---|
| **TC04** | `[27.50, 40.0, 12.0, 8.0, 3, 1, 1, 0, 5.0, 0.0, 1, 0, 1]` | Note N9 (LTD requires STD) | `Long-term disability requires short-term disability (N9)` |
| **TC05** | `[45.00, 40.0, 12.0, 0.0, 1, 0, 0, 1, 20.0, 50.0, 1, 0, 1]`| Note N5 (High-Earner Medical) | `Hourly rate >= $40.01 requires medical plan 2 or 3 (got 0)` |
| **TC09** | `[32.50, 40.0, 8.0, 8.0, 2, 1, 0, 2, 20.0, 0.0, 1, 1, 0]` | Note N4 (Medical requires Dental) | `Medical enrollment requires dental enrollment (N4)` |
| **TC12** | `[17.50, 40.0, 0.0, 3.0, 1, 1, 0, 0, 0.0, 0.0, 0, 0, 1]` | Note N4 (Medical requires Dental) | `Medical enrollment requires dental enrollment (N4)` |
| **TC17** | `[32.50, 40.0, 0.0, 3.0, 3, 3, 0, 1, 0.0, 200.0, 0, 0, 0]`| Note N4 (Medical requires Dental) | `Medical enrollment requires dental enrollment (N4)` |
| **TC19** | `[22.50, 40.0, 3.0, 0.0, 1, 3, 0, 2, 20.0, 50.0, 1, 1, 0]`| Note N4 (Medical requires Dental) | `Medical enrollment requires dental enrollment (N4)` |
| **TC21** | `[17.50, 40.0, 12.0, 8.0, 2, 3, 2, 2, 5.0, 0.0, 0, 1, 0]` | Note N1 (Low-Wage Overtime Limit)| `Hourly rate $15-20 limits overtime to 10.0 hours (got 12.0)` |
| **TC22** | `[22.50, 40.0, 12.0, 8.0, 1, 2, 0, 1, 0.0, 200.0, 1, 1, 1]`| Note N4 (Medical requires Dental) | `Medical enrollment requires dental enrollment (N4)` |
| **TC24** | `[17.50, 40.0, 8.0, 8.0, 3, 3, 1, 2, 20.0, 50.0, 1, 0, 1]` | Note N9 (LTD requires STD) | `Long-term disability requires short-term disability (N9)` |
| **TC25** | `[27.50, 40.0, 12.0, 3.0, 2, 3, 2, 1, 5.0, 0.0, 1, 0, 0]` | Note N9 (LTD requires STD) | `Long-term disability requires short-term disability (N9)` |
| **TC27** | `[45.00, 40.0, 3.0, 8.0, 2, 1, 2, 2, 0.0, 50.0, 0, 0, 0]`  | Note N5 (High-Earner Medical) | `Hourly rate >= $40.01 requires medical plan 2 or 3 (got 1)` |

