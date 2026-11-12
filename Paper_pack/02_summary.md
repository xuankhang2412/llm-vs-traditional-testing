# Benchmark Summary Report: Black-Box Test Suite Evaluation Across Four Applications

**Benchmark Suite:** SWT301 Black-Box Test Generation Benchmark  
**Date of Execution:** 2026-11-05  
**Artifact Location:** `D:\SWT301_TEST\Results\`  
**Target Subject Systems:**
1. `BasicPackage` (Project1: Basic Package Shipping - 10 Seeded Defect Mutations)
2. `BasicPayroll` (Project2: Basic Payroll Calculator - 10 Seeded Defect Mutations)
3. `EnhancedPackage` (Project3: Enhanced Package Shipping - 20 Seeded Defect Mutations)
4. `EnhancedPayroll` (Project4: Enhanced Payroll Calculator - 20 Seeded Defect Mutations)

**Total Seeded Defects Pool ($N$):** 60 defects  
**Total Test Suites Evaluated:** 34 suites (4 TFD, 6 PICT, 24 Gemini 3.1 Pro LLM)  
**Total Test Execution Pairs Evaluated:** 15,540 test-defect execution evaluations (recorded in 34 `matrix_long.csv` files)

---

## 1. Executive Summary

This report synthesizes the empirical results of an end-to-end black-box test generation benchmark evaluating three distinct test suite creation paradigms:
1. **Human Specification-Based Testing (TFD):** Designed by an external tester solely from specification requirements ($N=20$ tests for Basic apps, $N=35$ tests for Enhanced apps).
   > *Caveat:* The tester states that no AI tool was used; this statement and the effort times are self-reported and could not be independently verified.
2. **Combinatorial Pairwise Testing (PICT):** Generated using Microsoft PICT covering all 2-way parameter interactions (`PICT_plain` across all 4 apps; `PICT_constrained` incorporating business validation constraints on Enhanced apps).
3. **Large Language Model Generation (Gemini 3.1 Pro):** Generated under 6 controlled prompt conditions factorializing specification detail level (Low $C_1..C_3$ vs High $C_4..C_6$) and few-shot exemplars (Zero-shot $C_1, C_4$; One-shot $C_2, C_5$; Three-shot $C_3, C_6$).

### Key Findings
* **Defect Detection Across Methods:** Across all 60 seeded defects in the four applications, Human TFD detected **100.0% (60/60)** of seeded faults. However, the difference between TFD and the post hoc best LLM condition was not statistically significant after Holm-Bonferroni correction (exact two-sided McNemar Holm-adjusted $p = 0.125$).
* **Competitive Performance of LLM Suites:** Gemini 3.1 Pro achieved **100.0% (10/10)** detection on both Basic applications. In complex Enhanced systems, single LLM suites achieved **90.0% (18/20)** on Enhanced Package and **85.0% (17/20)** on Enhanced Payroll.
* **LLM Ensemble Synergy:** Pooling all 6 LLM conditions yielded **100.0% (20/20)** on Enhanced Package and **95.0% (19/20)** on Enhanced Payroll, missing only defect `D19` (a delicate boolean condition on short-term vs long-term disability).
* **Pairwise Blindspots:** Pairwise PICT plain detected **75.0% (45/60)** overall (80% BasicPackage, 80% BasicPayroll, 65% EnhancedPackage, 80% EnhancedPayroll). Adding constraints improved validity but did not uniformly increase defect detection (14/20 EnhancedPackage, 13/20 EnhancedPayroll) due to avoiding out-of-domain space where boundary faults lurk.
* **Hybrid Synergy:** Combining PICT with LLM yielded **100.0% (20/20)** detection in Enhanced Package and **100.0% (20/20)** detection in Enhanced Payroll, completely closing the residual gap of both standalone automated methods.
* **Cost Disparity:** Human TFD required **280.0 minutes** (4 hours 40 minutes, self-reported). In contrast, Gemini 3.1 Pro generated 24 full test suites in **19.69 minutes** cumulative wall-clock time (~0.82 minutes per suite), and PICT ran sub-second.

---

## 2. Primary Metrics: Detection Rate & Defect Efficiency

The primary evaluation oracle is `HIT_REF` (behavioral discordance between the defect mutant and the pristine `NONE` reference implementation). `defects_per_test` measures test efficiency as detected defects divided by total suite test count (reported as a non-percentage metric).

| Application | Test Suite | Method | Condition | Total Defects | Detected Defects | Detection Rate | Total Tests | defects_per_test |
| :--- | :--- | :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **BasicPackage** | `TFD` | TFD | TFD | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `PICT_plain` | PICT | plain | 10 | 8 | 80.00% | 20 | 0.4000 |
| BasicPackage | `LLM_C1` | LLM | C1 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `LLM_C2` | LLM | C2 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `LLM_C3` | LLM | C3 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `LLM_C4` | LLM | C4 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `LLM_C5` | LLM | C5 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPackage | `LLM_C6` | LLM | C6 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| **BasicPayroll** | `TFD` | TFD | TFD | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `PICT_plain` | PICT | plain | 10 | 8 | 80.00% | 16 | 0.5000 |
| BasicPayroll | `LLM_C1` | LLM | C1 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `LLM_C2` | LLM | C2 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `LLM_C3` | LLM | C3 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `LLM_C4` | LLM | C4 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `LLM_C5` | LLM | C5 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| BasicPayroll | `LLM_C6` | LLM | C6 | 10 | 10 | **100.00%** | 20 | 0.5000 |
| **EnhancedPackage** | `TFD` | TFD | TFD | 20 | 20 | **100.00%** | 35 | 0.5714 |
| EnhancedPackage | `PICT_plain` | PICT | plain | 20 | 13 | 65.00% | 35 | 0.3714 |
| EnhancedPackage | `PICT_constrained` | PICT | constrained | 20 | 14 | 70.00% | 35 | 0.4000 |
| EnhancedPackage | `LLM_C1` | LLM | C1 | 20 | 18 | 90.00% | 35 | 0.5143 |
| EnhancedPackage | `LLM_C2` | LLM | C2 | 20 | 18 | 90.00% | 35 | 0.5143 |
| EnhancedPackage | `LLM_C3` | LLM | C3 | 20 | 18 | 90.00% | 35 | 0.5143 |
| EnhancedPackage | `LLM_C4` | LLM | C4 | 20 | 18 | 90.00% | 35 | 0.5143 |
| EnhancedPackage | `LLM_C5` | LLM | C5 | 20 | 18 | 90.00% | 35 | 0.5143 |
| EnhancedPackage | `LLM_C6` | LLM | C6 | 20 | 17 | 85.00% | 35 | 0.4857 |
| **EnhancedPayroll** | `TFD` | TFD | TFD | 20 | 20 | **100.00%** | 35 | 0.5714 |
| EnhancedPayroll | `PICT_plain` | PICT | plain | 20 | 16 | 80.00% | 27 | 0.5926 |
| EnhancedPayroll | `PICT_constrained` | PICT | constrained | 20 | 13 | 65.00% | 32 | 0.4063 |
| EnhancedPayroll | `LLM_C1` | LLM | C1 | 20 | 17 | 85.00% | 35 | 0.4857 |
| EnhancedPayroll | `LLM_C2` | LLM | C2 | 20 | 17 | 85.00% | 35 | 0.4857 |
| EnhancedPayroll | `LLM_C3` | LLM | C3 | 20 | 17 | 85.00% | 35 | 0.4857 |
| EnhancedPayroll | `LLM_C4` | LLM | C4 | 20 | 16 | 80.00% | 35 | 0.4571 |
| EnhancedPayroll | `LLM_C5` | LLM | C5 | 20 | 16 | 80.00% | 35 | 0.4571 |
| EnhancedPayroll | `LLM_C6` | LLM | C6 | 20 | 16 | 80.00% | 35 | 0.4571 |

*Raw data source:* [`Results/primary_metrics.csv`](file:///D:/SWT301_TEST/Results/primary_metrics.csv)

---

## 3. Secondary Metrics: Test Validity, Expected Value Accuracy & Oracles

### Definitions & Oracular Verification
* **Invalid-Test Rate:** Fraction of test cases in the suite where the input values violate business validation rules, triggering `ERROR` (exception) on the pristine reference implementation.
* **Expected-Value Accuracy:** Concordance between the test creator's provided `expected` column and the pristine reference implementation's calculated result. Split into **Valid** tests ($Expected == Reference$) and **ERROR** tests ($Expected == "ERROR"$).
* **Sensitivity Analysis (`HIT_EXP` on `BASELINE_OK=1`):** Defect detection measured using creator-provided expected values strictly on test cases where the baseline reference implementation matches the creator's expectation.

| Application | Suite | Invalid Tests | Invalid Rate | Expected Acc (Valid) | Expected Acc (ERROR) | Expected Acc (Overall) | Sum Failing Tests | Sensitivity Detected |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **BasicPackage** | `TFD` | 5 | 25.00% | 100.0% (15/15) | 100.0% (5/5) | 100.0% (20/20) | 50 | 10 / 10 (100.0%) |
| BasicPackage | `PICT_plain` | 0 | 0.00% | N/A | N/A | N/A | 73 | N/A |
| BasicPackage | `LLM_C1` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 42 | 10 / 10 (100.0%) |
| BasicPackage | `LLM_C2` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| BasicPackage | `LLM_C3` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| BasicPackage | `LLM_C4` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| BasicPackage | `LLM_C5` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| BasicPackage | `LLM_C6` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| **BasicPayroll** | `TFD` | 6 | 30.00% | 100.0% (14/14) | 100.0% (6/6) | 100.0% (20/20) | 58 | 10 / 10 (100.0%) |
| BasicPayroll | `PICT_plain` | 0 | 0.00% | N/A | N/A | N/A | 75 | N/A |
| BasicPayroll | `LLM_C1` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 25 | 10 / 10 (100.0%) |
| BasicPayroll | `LLM_C2` | 8 | 40.00% | 100.0% (12/12) | 100.0% (8/8) | 100.0% (20/20) | 35 | 10 / 10 (100.0%) |
| BasicPayroll | `LLM_C3` | 10 | 50.00% | 100.0% (10/10) | 100.0% (10/10) | 100.0% (20/20) | 40 | 10 / 10 (100.0%) |
| BasicPayroll | `LLM_C4` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 50 | 10 / 10 (100.0%) |
| BasicPayroll | `LLM_C5` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 39 | 10 / 10 (100.0%) |
| BasicPayroll | `LLM_C6` | 9 | 45.00% | 100.0% (11/11) | 100.0% (9/9) | 100.0% (20/20) | 50 | 10 / 10 (100.0%) |
| **EnhancedPackage** | `TFD` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 50 | 20 / 20 (100.0%) |
| EnhancedPackage | `PICT_plain` | 25 | 71.43% | N/A | N/A | N/A | 29 | N/A |
| EnhancedPackage | `PICT_constrained` | 0 | 0.00% | N/A | N/A | N/A | 82 | N/A |
| EnhancedPackage | `LLM_C1` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 52 | 18 / 20 (90.0%) |
| EnhancedPackage | `LLM_C2` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 52 | 18 / 20 (90.0%) |
| EnhancedPackage | `LLM_C3` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 56 | 18 / 20 (90.0%) |
| EnhancedPackage | `LLM_C4` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 52 | 18 / 20 (90.0%) |
| EnhancedPackage | `LLM_C5` | 14 | 40.00% | 100.0% (21/21) | 100.0% (14/14) | 100.0% (35/35) | 50 | 18 / 20 (90.0%) |
| EnhancedPackage | `LLM_C6` | 15 | 42.86% | 100.0% (20/20) | 100.0% (15/15) | 100.0% (35/35) | 44 | 17 / 20 (85.0%) |
| **EnhancedPayroll** | `TFD` | 16 | 45.71% | 100.0% (19/19) | 100.0% (16/16) | 100.0% (35/35) | 95 | 20 / 20 (100.0%) |
| EnhancedPayroll | `PICT_plain` | 11 | 40.74% | N/A | N/A | N/A | 95 | N/A |
| EnhancedPayroll | `PICT_constrained` | 0 | 0.00% | N/A | N/A | N/A | 158 | N/A |
| EnhancedPayroll | `LLM_C1` | 12 | 34.29% | 100.0% (23/23) | 100.0% (12/12) | 100.0% (35/35) | 65 | 17 / 20 (85.0%) |
| EnhancedPayroll | `LLM_C2` | 12 | 34.29% | 100.0% (23/23) | 100.0% (12/12) | 100.0% (35/35) | 65 | 17 / 20 (85.0%) |
| EnhancedPayroll | `LLM_C3` | 12 | 34.29% | 100.0% (23/23) | 100.0% (12/12) | 100.0% (35/35) | 64 | 17 / 20 (85.0%) |
| EnhancedPayroll | `LLM_C4` | 11 | 31.43% | 100.0% (24/24) | 100.0% (11/11) | 100.0% (35/35) | 67 | 16 / 20 (80.0%) |
| EnhancedPayroll | `LLM_C5` | 11 | 31.43% | 100.0% (24/24) | 100.0% (11/11) | 100.0% (35/35) | 67 | 16 / 20 (80.0%) |
| EnhancedPayroll | `LLM_C6` | 11 | 31.43% | 100.0% (24/24) | 100.0% (11/11) | 100.0% (35/35) | 65 | 16 / 20 (80.0%) |

*Observations on Oracles & Expected Values:*
1. **100% Expected Value Accuracy:** Across all 24 Gemini LLM suites and all 4 TFD suites, **every single test case** ($100.0\%$) matched the pristine reference calculation (`BASELINE_OK = 1`). There were zero baseline calculation discrepancies.
2. **Complete Sensitivity Robustness:** Because $BASELINE\_OK = 1$ for all tests with expected values, the sensitivity oracle (`HIT_EXP` restricted to `BASELINE_OK = 1`) yielded **identical defect detection counts** to `HIT_REF` across all suites.
3. **Invalid Tests as Fault Exposing Tests:** Between 25% and 45% of tests in TFD and LLM suites purposefully exercised invalid domain boundaries (e.g., negative hours, excessive weights, illegal option codes). These negative tests were vital for detecting boundary defects such as missing validation checks (`D1`, `D2` in Basic systems; `D11`, `D12`, `D14` in Enhanced systems).
4. **Constrained PICT Limitation:** In `PICT_constrained`, all generated tests were 100% valid (0 invalid tests). However, because negative inputs were excluded by constraints, `PICT_constrained` could not detect defects where validations were removed or altered.

*Raw data source:* [`Results/secondary_metrics.csv`](file:///D:/SWT301_TEST/Results/secondary_metrics.csv)

---

## 4. Defect Category & Application Domain Breakdown

Across the 60 seeded defects, four functional mutation types were catalogued from the authoritative project specifications:
* **Boundary (13 defects):** Range limits, inequality strictness ($<$ vs $\le$), threshold boundaries.
* **Arithmetic (15 defects):** Rate coefficients, multipliers, percentages, deduction scalers.
* **Logic (18 defects):** Swapped lookup keys, inverted boolean conditions, ignored fields, wrong mappings.
* **Conditional (14 defects):** Premise conditions, multi-rule conjunctions/disjunctions (Enhanced only).

### Pooled Detection by Category ($N=60$ Defects)

| Defect Category | Total Seeded | Human TFD Detected | PICT (Plain) Detected | Gemini LLM (Best) | Gemini LLM (Union) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Boundary** | 13 | **13 / 13 (100.0%)** | 1 / 13 (7.69%) | 9 / 13 (69.23%) | **13 / 13 (100.0%)** |
| **Arithmetic** | 15 | **15 / 15 (100.0%)** | 14 / 15 (93.33%) | **15 / 15 (100.0%)** | **15 / 15 (100.0%)** |
| **Logic** | 18 | **18 / 18 (100.0%)** | 17 / 18 (94.44%) | **18 / 18 (100.0%)** | **18 / 18 (100.0%)** |
| **Conditional** | 14 | **14 / 14 (100.0%)** | 13 / 14 (92.86%) | 13 / 14 (92.86%) | 13 / 14 (92.86%) |
| **Total** | **60** | **60 / 60 (100.0%)** | **45 / 60 (75.00%)** | **55 / 60 (91.67%)** | **59 / 60 (98.33%)** |

### Breakdown by Application Domain
* **Package Shipping Domain (30 defects: 10 Basic, 20 Enhanced):**
  * TFD: 30 / 30 (100.0%)
  * PICT_plain: 21 / 30 (70.0%)
  * LLM Best: 28 / 30 (93.33%)
  * LLM Union: 30 / 30 (100.0%)
* **Payroll Domain (30 defects: 10 Basic, 20 Enhanced):**
  * TFD: 30 / 30 (100.0%)
  * PICT_plain: 24 / 30 (80.0%)
  * LLM Best: 27 / 30 (90.0%)
  * LLM Union: 29 / 30 (96.67%) - only missing EnhancedPayroll `D19`

*Category Insights:*
1. **Arithmetic and Logic Strength:** LLM suites exhibited flawless detection ($100\%$) across all arithmetic and logic mutations, identifying inverted conditions, swapped deduction plans, and erroneous rate coefficients.
2. **Boundary Vulnerabilities in Automated Generation:** Boundary faults were the primary blindspot for automated generation. Specifically:
   * In `EnhancedPackage`, single LLM suites (C1..C4) missed boundary defects `D12` (envelope limit $\le 2$ vs $<2$) and `D14` (medium box limit $\le 25$ vs $<25$). However, condition `C5` successfully tested these boundary values!
   * In `EnhancedPayroll`, LLM suites struggled with `D11` (overtime bonus threshold $\ge 5$ vs $>5$) and `D17` (Mississippi tax bracket starting at 25.00 vs 25.01).
   * Human TFD explicitly designed tests targeting boundary ties (e.g. 2.0 lbs, 25.0 lbs, 5.0 hrs, $25.00/hr) as documented in tester rationale notes.

*Raw data source:* [`Results/defect_type_and_domain_breakdown.csv`](file:///D:/SWT301_TEST/Results/defect_type_and_domain_breakdown.csv)

---

## 5. Hybrid Analysis: Synergistic Combinations

Evaluating multi-method test suites reveals whether combining lightweight automated techniques can bridge the defect detection deficit without human intervention.

| Application | Total Defects | Combination | Methods Included | Total Tests | Detected Count | Detection Rate | Best Single Count | Absolute Gain | Relative Gain |
| :--- | :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| BasicPackage | 10 | TFD + Best LLM | TFD,LLM_C1 | 40 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | TFD + PICT_plain | TFD,PICT_plain | 40 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | Best LLM + PICT_plain | LLM_C1,PICT_plain | 40 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | TFD + Best LLM + PICT_plain | TFD,LLM_C1,PICT_plain | 60 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | TFD + LLM_Union | TFD,LLM_Union | 140 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | LLM_Union + PICT_plain | LLM_Union,PICT_plain | 140 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPackage | 10 | TFD + LLM_Union + PICT_plain | TFD,LLM_Union,PICT_plain | 160 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | TFD + Best LLM | TFD,LLM_C1 | 40 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | TFD + PICT_plain | TFD,PICT_plain | 36 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | Best LLM + PICT_plain | LLM_C1,PICT_plain | 36 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | TFD + Best LLM + PICT_plain | TFD,LLM_C1,PICT_plain | 56 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | TFD + LLM_Union | TFD,LLM_Union | 140 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | LLM_Union + PICT_plain | LLM_Union,PICT_plain | 136 | 10 | 100.0% | 10 | 0 | 0.00% |
| BasicPayroll | 10 | TFD + LLM_Union + PICT_plain | TFD,LLM_Union,PICT_plain | 156 | 10 | 100.0% | 10 | 0 | 0.00% |
| EnhancedPackage | 20 | TFD + Best LLM | TFD,LLM_C1 | 70 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPackage | 20 | TFD + PICT_plain | TFD,PICT_plain | 70 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPackage | 20 | Best LLM + PICT_plain | LLM_C1,PICT_plain | 70 | 18 | 90.0% | 18 | 0 | 0.00% |
| EnhancedPackage | 20 | TFD + Best LLM + PICT_plain | TFD,LLM_C1,PICT_plain | 105 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPackage | 20 | TFD + LLM_Union | TFD,LLM_Union | 245 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPackage | 20 | LLM_Union + PICT_plain | LLM_Union,PICT_plain | 245 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPackage | 20 | TFD + LLM_Union + PICT_plain | TFD,LLM_Union,PICT_plain | 280 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPayroll | 20 | TFD + Best LLM | TFD,LLM_C1 | 70 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPayroll | 20 | TFD + PICT_plain | TFD,PICT_plain | 62 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPayroll | 20 | Best LLM + PICT_plain | LLM_C1,PICT_plain | 62 | 18 | 90.0% | 17 | **+1** | **+5.88%** |
| EnhancedPayroll | 20 | TFD + Best LLM + PICT_plain | TFD,LLM_C1,PICT_plain | 97 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPayroll | 20 | TFD + LLM_Union | TFD,LLM_Union | 245 | 20 | 100.0% | 20 | 0 | 0.00% |
| EnhancedPayroll | 20 | LLM_Union + PICT_plain | LLM_Union,PICT_plain | 237 | 20 | 100.0% | 19 | **+1** | **+5.26%** |
| EnhancedPayroll | 20 | TFD + LLM_Union + PICT_plain | TFD,LLM_Union,PICT_plain | 272 | 20 | 100.0% | 20 | 0 | 0.00% |

*Note: "Best LLM" is selected post hoc among the six prompt conditions for each application. LLM_Union represents the union of all six conditions ($6 \times 20 = 120$ tests for Basic apps; $6 \times 35 = 210$ tests for Enhanced apps).*

### Key Synergy Highlights
1. **Closing the Defect Gap in Enhanced Payroll:** In `EnhancedPayroll`, LLM Union detected 19/20 defects, missing only conditional defect `D19`. PICT plain detected 16/20 defects. However, PICT plain successfully detected `D19` across 4 distinct test cases (`TC03`, `TC07`, `TC13`, `TC16`)! Consequently, the union **LLM_Union + PICT_plain** achieves **20/20 (100.0%)** detection.
2. **Fully Automated 100% Coverage:** In both Enhanced systems, pooling the LLM suite conditions or combining LLM with Pairwise PICT achieves **100.0% defect exposure**, matching the performance of the expert human tester without manual test case design.

*Raw data source:* [`Results/hybrid_analysis.csv`](file:///D:/SWT301_TEST/Results/hybrid_analysis.csv)

---

## 6. LLM Prompt Factors Analysis

The benchmark examined two orthogonal prompt dimensions across 24 generation runs (4 applications $\times$ 6 conditions):
* **Specification Detail Level:**
  * *Low Detail ($C_1..C_3$):* Concise functional rules, condensed domain tables.
  * *High Detail ($C_4..C_6$):* Full edge-case enumerations, explicit notes, detailed notes explanations.
* **Example Shots:**
  * *Zero-shot ($C_1, C_4$):* Specification only; no worked input/output examples.
  * *One-shot ($C_2, C_5$):* Specification plus 1 valid example.
  * *Three-shot ($C_3, C_6$):* Specification plus 3 diverse valid/invalid examples.

Because only ONE repetition was generated per condition, results are presented as descriptive statistics (both pooled defect-opportunity counts and the unweighted mean of per-suite detection rates across the 4 applications). Note that in the **mean of per-suite rates**, Basic applications (10 defects each) and Enhanced applications (20 defects each) receive equal weight ($0.25$ each), whereas pooled metrics weight Enhanced defects twice as heavily as Basic defects.

| Factor | Level | Suite Count | Pooled Defects | Pooled Detection Rate | Mean of Per-Suite Rates | Mean defects_per_test | Characterization |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **Detail Level** | Low (C1-C3) | 12 | 165 / 180 | 91.67% | 93.75% | 0.5000 | Descriptive Mean |
| **Detail Level** | High (C4-C6) | 12 | 161 / 180 | 89.44% | 92.08% | 0.4905 | Descriptive Mean |
| **Example Shots** | 0-shot (C1,C4) | 8 | 109 / 120 | 90.83% | 93.13% | 0.4964 | Descriptive Mean |
| **Example Shots** | 1-shot (C2,C5) | 8 | 109 / 120 | 90.83% | 93.13% | 0.4964 | Descriptive Mean |
| **Example Shots** | 3-shot (C3,C6) | 8 | 108 / 120 | 90.00% | 92.50% | 0.4929 | Descriptive Mean |

### By-Condition Descriptive Summary

| Condition | Prompt Configuration | Suites | Pooled Defects | Pooled Detection Rate | Mean of Per-Suite Rates | Mean defects_per_test |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: |
| **$C1$** | Low Detail, Zero-Shot | 4 | 55 / 60 | 91.67% | 93.75% | 0.5000 |
| **$C2$** | Low Detail, One-Shot | 4 | 55 / 60 | 91.67% | 93.75% | 0.5000 |
| **$C3$** | Low Detail, Three-Shot | 4 | 55 / 60 | 91.67% | 93.75% | 0.5000 |
| **$C4$** | High Detail, Zero-Shot | 4 | 54 / 60 | 90.00% | 92.50% | 0.4929 |
| **$C5$** | High Detail, One-Shot | 4 | 54 / 60 | 90.00% | 92.50% | 0.4929 |
| **$C6$** | High Detail, Three-Shot | 4 | 53 / 60 | 88.33% | 91.25% | 0.4857 |

*Insights:*
* **Prompt Detail Level & Few-Shot Variations:** With only one repetition per prompt condition, differences of 1–2 defects across suites are purely descriptive and not statistically interpretable. Across all four applications ($10+10+20+20 = 60$ defect opportunities per condition):
  * **Detail Level:** Low Detail prompts ($C_1..C_3$, 12 suites, $3 \times 60 = 180$ opportunities) detected **165 / 180** pooled defects (pooled rate **91.67%**; mean of per-suite rates **93.75%**; **13.75** defects detected per suite on average). High Detail prompts ($C_4..C_6$, 12 suites, $3 \times 60 = 180$ opportunities) detected **161 / 180** pooled defects (pooled rate **89.44%**; mean of per-suite rates **92.08%**; **13.42** defects detected per suite on average). The absolute difference is only 4 defect detections across 180 opportunities.
  * **Example Shots:** Zero-shot ($C_1, C_4$, 8 suites, $2 \times 60 = 120$ opportunities) detected **109 / 120** pooled defects (pooled rate **90.83%**; mean of per-suite rates **93.13%**; **13.625** defects detected per suite). One-shot ($C_2, C_5$, 8 suites, $2 \times 60 = 120$ opportunities) detected **109 / 120** pooled defects (pooled rate **90.83%**; mean of per-suite rates **93.13%**; **13.625** defects detected per suite). Three-shot ($C_3, C_6$, 8 suites, $2 \times 60 = 120$ opportunities) detected **108 / 120** pooled defects (pooled rate **90.00%**; mean of per-suite rates **92.50%**; **13.50** defects detected per suite).
* A conjecture that high-detail prompts might constrain exploratory boundary sampling remains an untested hypothesis that cannot be evaluated without multi-repetition variance data.

*Raw data source:* [`Results/llm_factors.csv`](file:///D:/SWT301_TEST/Results/llm_factors.csv)

---

## 7. Input Diversity & Cross-Suite Overlap

To determine whether the test generation techniques explore distinct regions of the input space, input vectors (excluding expected outputs) were compared pairwise.

### Input Uniqueness within LLM Suites
* In all 24 Gemini LLM suites across the four applications, **100% of generated rows were unique within each suite** (20/20 distinct inputs in Basic apps; 35/35 distinct inputs in Enhanced apps). Zero intra-suite duplicate rows were produced.

### Identical Suites and Input Sets Audit
An audit grouping LLM conditions whose INPUT rows are identical as sets reveals that several prompt conditions produced identical suites. Consequently, the 24 generation runs are **not 24 independent samples**:
* **BasicPackage (2 distinct suites; 23 distinct input rows in LLM union):**
  * *Group 1 [C1] (1 condition, 20 rows):* `gen_log` = `2026-11-04 11:25`, `mtime` = `2026-11-05T05:20:54.477Z`. Expected column identical across group: YES.
  * *Group 2 [C2, C3, C4, C5, C6] (5 conditions, 20 rows):* 100% identical input sets and identical expected columns across all 5 conditions.
    * C2: `gen_log` = `2026-11-04 11:28`, `mtime` = `2026-11-04T04:47:20.538Z`
    * C3: `gen_log` = `2026-11-04 11:29`, `mtime` = `2026-11-04T04:47:30.173Z`
    * C4: `gen_log` = `2026-11-04 11:30`, `mtime` = `2026-11-04T04:47:39.990Z`
    * C5: `gen_log` = `2026-11-04 11:32`, `mtime` = `2026-11-04T04:47:49.460Z`
    * C6: `gen_log` = `2026-11-04 11:33`, `mtime` = `2026-11-04T04:47:59.170Z`
* **BasicPayroll (6 distinct suites; 73 distinct input rows in LLM union):**
  * All 6 conditions generated distinct input sets (all singletons).
    * C1: `gen_log` = `2026-11-04 13:11`, `mtime` = `2026-11-05T05:20:54.500Z`
    * C2: `gen_log` = `2026-11-04 13:13`, `mtime` = `2026-11-04T06:22:58.994Z`
    * C3: `gen_log` = `2026-11-04 13:15`, `mtime` = `2026-11-04T06:23:11.289Z`
    * C4: `gen_log` = `2026-11-04 13:15`, `mtime` = `2026-11-04T06:23:21.448Z`
    * C5: `gen_log` = `2026-11-04 13:16`, `mtime` = `2026-11-04T06:23:30.967Z`
    * C6: `gen_log` = `2026-11-04 13:17`, `mtime` = `2026-11-04T06:23:41.264Z`
* **EnhancedPackage (4 distinct suites; 77 distinct input rows in LLM union):**
  * *Group 1 [C1, C2, C4] (3 conditions, 35 rows):* 100% identical input sets and identical expected columns across all 3 conditions.
    * C1: `gen_log` = `2026-11-04 13:43`, `mtime` = `2026-11-04T06:54:26.447Z`
    * C2: `gen_log` = `2026-11-04 13:44`, `mtime` = `2026-11-04T06:54:35.793Z`
    * C4: `gen_log` = `2026-11-04 13:47`, `mtime` = `2026-11-04T06:54:51.325Z`
  * *Group 2 [C3] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:45`, `mtime` = `2026-11-04T06:54:43.562Z`
  * *Group 3 [C5] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:48`, `mtime` = `2026-11-04T06:54:59.555Z`
  * *Group 4 [C6] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:50`, `mtime` = `2026-11-04T06:55:08.545Z`
* **EnhancedPayroll (5 distinct suites; 121 distinct input rows in LLM union):**
  * *Group 1 [C1] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:26`, `mtime` = `2026-11-05T05:20:54.516Z`
  * *Group 2 [C2] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:27`, `mtime` = `2026-11-04T06:41:27.084Z`
  * *Group 3 [C3] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:29`, `mtime` = `2026-11-04T06:41:40.260Z`
  * *Group 4 [C4, C5] (2 conditions, 35 rows):* 100% identical input sets and identical expected columns.
    * C4: `gen_log` = `2026-11-04 13:31`, `mtime` = `2026-11-04T06:41:47.884Z`
    * C5: `gen_log` = `2026-11-04 13:32`, `mtime` = `2026-11-04T06:41:57.055Z`
  * *Group 5 [C6] (1 condition, 35 rows):* `gen_log` = `2026-11-04 13:34`, `mtime` = `2026-11-04T06:42:07.171Z`

### Pairwise Cross-Condition Overlap (Within App)
* **BasicPackage:** Condition $C_1$ shared 17/20 inputs with $C_2..C_6$ (Jaccard similarity = 0.7391), while conditions $C_2$ through $C_6$ produced 100% identical inputs to each other (20/20 shared rows, Jaccard similarity = 1.0000).
* **BasicPayroll:** Condition $C_1$ shared 0/20 rows with any other condition (Jaccard similarity = 0.0000). Among conditions $C_2..C_6$, pairwise overlap ranged from 5 to 13 shared rows out of 20 (Jaccard similarity 0.1429 to 0.4815).
* **EnhancedPackage:** Pairwise overlap between LLM conditions ranged from 7 to 35 shared rows out of 35 (Jaccard similarity 0.1111 to 1.0000). Identical inputs were produced between $C_1, C_2, C_4$ (35/35 shared rows, Jaccard = 1.0000), while differing pairs shared between 7 and 25 rows (Jaccard similarity 0.1111 to 0.5556).
* **EnhancedPayroll:** Condition $C_1$ shared 0/35 rows with any other condition (Jaccard similarity = 0.0000). Conditions $C_4$ and $C_5$ produced identical inputs (35/35 shared rows, Jaccard similarity = 1.0000), while other pairwise combinations shared between 5 and 23 rows (Jaccard similarity 0.0769 to 0.4894).

### Cross-Method Overlap (LLM vs TFD vs PICT)
* **LLM vs TFD Overlap:** In `BasicPackage`, all LLM conditions shared exactly 1 test with TFD (Jaccard = 0.0256). In `BasicPayroll`, LLM conditions shared between 0 and 5 tests with TFD (Jaccard range: 0.0000 to 0.1429). In `EnhancedPackage`, LLM conditions shared 1 to 2 tests with TFD (Jaccard range: 0.0145 to 0.0294). In `EnhancedPayroll`, LLM conditions shared 0 to 3 tests with TFD (Jaccard range: 0.0000 to 0.0448).
* **LLM vs PICT Overlap:** Exactly 0 shared input rows across all 4 applications (Jaccard = 0.0000).
* **TFD vs PICT Overlap:** Exactly 0 shared input rows across all 4 applications (Jaccard = 0.0000).

*Implication:* The high defect detection achieved by combining methods is directly attributable to their input orthogonalities: PICT systematically covers parameter tuples, LLM explores boundary clusters and error semantics, and TFD targets domain-specific business exceptions.

*Raw data source:* [`Results/diversity_and_overlap.csv`](file:///D:/SWT301_TEST/Results/diversity_and_overlap.csv)

---

## 8. Statistical Hypothesis Testing

To evaluate whether the observed defect detection differences between methods are statistically significant, paired non-parametric tests were performed on the defect-level binary detection data across all $N=60$ seeded defects.

### Cochran's Q Test (Across $k=3$ Matched Methods)
1. **TFD vs PICT_plain vs Best LLM:**
   * $Q = 21.8750$, $df = 2$, $p = 1.778 \times 10^{-5}$ ($p < 0.001$).
   * *Conclusion:* Significant difference in detection capability across the three paradigms.
2. **TFD vs PICT_plain vs LLM Union:**
   * $Q = 26.3750$, $df = 2$, $p = 1.874 \times 10^{-6}$ ($p < 0.001$).
   * *Conclusion:* Highly significant divergence across methods.

### Exact Two-Sided McNemar Tests (With Holm-Bonferroni Correction)
Five targeted pairwise comparisons were evaluated across $N=60$ pooled defects ($b$: detected by Method A only; $c$: detected by Method B only):

| Method A | Method B | $b$ ($A=1, B=0$) | $c$ ($A=0, B=1$) | Raw $p$-value | Holm-Adjusted $p$ | Significant ($alpha=0.05$) | Odds Ratio ($OR$) | Risk Difference ($RD$) | Cohen's $g$ |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **TFD** | **PICT_plain** | 15 | 0 | $6.100 \times 10^{-5}$ | **$3.050 \times 10^{-4}$** | **YES** | $\infty$ | $+0.2500$ | 0.5000 |
| **LLM_Union** | **PICT_plain** | 15 | 1 | $5.190 \times 10^{-4}$ | **$0.00208$** | **YES** | $15.000$ | $+0.2333$ | 0.4375 |
| **Best_LLM** | **PICT_plain** | 11 | 1 | $0.00635$ | **$0.01904$** | **YES** | $11.000$ | $+0.1667$ | 0.4167 |
| **TFD** | **Best_LLM** | 5 | 0 | $0.06250$ | **$0.12500$** | **NO** | $\infty$ | $+0.0833$ | 0.5000 |
| **TFD** | **LLM_Union** | 1 | 0 | $1.00000$ | **$1.00000$** | **NO** | $\infty$ | $+0.0167$ | 0.5000 |

> [!WARNING] Statistical Power Caveat
> While pooled sample size ($N=60$) was sufficient to demonstrate that Human TFD and Gemini LLM significantly outperform Pairwise PICT plain ($p_{adj} < 0.01$), comparisons between TFD and Best LLM ($b=5, c=0, p_{raw}=0.0625$) did not reach significance under the conservative two-sided exact test after Holm-Bonferroni correction ($p_{adj} = 0.125$). Non-significance here must NOT be interpreted as equivalence; the small discordant sample size ($m=5$) incurs high Type II error risk (low statistical power).

*Raw data source:* [`Results/statistical_tests.csv`](file:///D:/SWT301_TEST/Results/statistical_tests.csv)

---

## 9. Cost & Generation Efficiency Comparison

Effort expenditure was recorded per suite generation:
* **LLM Generation Time:** Measured API wall-clock response time (recorded in `Logs/generation_log.csv`).
* **Human TFD Time:** Self-reported by the external human tester (recorded in `Logs/TFD_tester_notes.txt`).
* **PICT Generation Time:** Automated execution time ($< 0.5$ seconds per suite).

| Method | BasicPackage | BasicPayroll | EnhancedPackage | EnhancedPayroll | Total Time Across All 4 Apps | Defect Detection Rate | Defects Detected per Minute |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Human TFD** | 45.0 min | 55.0 min | 85.0 min | 95.0 min | **280.0 min** (4.67 hrs) | **100.0%** (60/60) | **0.214** defects/min |
| **Gemini LLM (Single Best)**| 0.55 min | 0.90 min | 0.86 min | 1.05 min | **3.36 min** (0.06 hrs) | **91.7%** (55/60) | **16.369** defects/min |
| **Gemini LLM (All 6 Runs)** | 3.14 min | 3.78 min | 5.75 min | 7.02 min | **19.69 min** (0.33 hrs) | **98.3%** (59/60) | **2.996** defects/min |
| **Pairwise PICT (Plain)** | $<0.01$ min | $<0.01$ min | $<0.01$ min | $<0.01$ min | **$<0.04$ min** | **75.0%** (45/60) | **>1000** defects/min |

*Efficiency Trade-Off Analysis:*
* **83.3x Speedup for 92% Effectiveness:** A single Gemini 3.1 Pro prompt generation achieved 91.7% defect coverage in 3.36 minutes total across the 4 applications, representing an **83.3-fold time reduction** compared to the 280.0 minutes of self-reported manual human test design (280.0 / 3.36 = 83.33).
* **14.2x Speedup for 98% Effectiveness:** Generating all 24 suites across the 6 LLM conditions took 19.69 minutes total and captured 59 out of 60 defects, representing a **14.2-fold speedup** (280.0 / 19.69 = 14.22).

*Raw data source:* [`Results/cost_comparison.csv`](file:///D:/SWT301_TEST/Results/cost_comparison.csv)

---

## 10. Verification: Five Independently Recomputed Numbers

To adhere strictly to the Honesty directive in the preamble, five primary numbers were extracted directly from the raw disk artifacts and recomputed using isolated algorithmic logic without relying on the summary generation pipeline:

### Independent Recomputation 1: EnhancedPackage LLM_C1 Defect Detection Count ($18 / 20$)
* **Source File:** `D:\SWT301_TEST\Results\EnhancedPackage\LLM_C1\matrix_long.csv` (size: 20,035 bytes, 701 lines)
* **Recomputation Method:** Parsed all 700 execution data rows. Filtered records where column 4 (`HIT_REF`) equaled `1`. Collected distinct defect names into a hash set.
* **Result:** Set of detected defects = `['D1', 'D10', 'D11', 'D13', 'D15', 'D16', 'D17', 'D18', 'D19', 'D2', 'D20', 'D3', 'D4', 'D5', 'D6', 'D7', 'D8', 'D9']`.
* **Verification:** Size is exactly **18**, confirming undetected defects are strictly `D12` and `D14`. Matches `Results/primary_metrics.csv` row 20 ($18/20 = 0.9000$).

### Independent Recomputation 2: BasicPackage TFD Invalid Test Count ($5 / 20$)
* **Source File:** `D:\SWT301_TEST\Suites\BasicPackage\TFD\suite.csv` (size: 915 bytes, 21 lines)
* **Recomputation Method:** Evaluated each of the 20 input vectors against `com.shipping.ShippingCalculator(PackageDefect.NONE)`. Counted cases throwing `IllegalArgumentException`.
* **Result:** Rows 16..20 (inputs: `weight=0.0`, `weight=60.01`, `origin=0`, `destination=5`, `deliveryOption=8`) throw exceptions.
* **Verification:** Number of invalid tests is exactly **5**, yielding an invalid rate of **0.2500 (25.0%)**. Matches `Results/secondary_metrics.csv` row 8.

### Independent Recomputation 3: Exact Two-Sided McNemar $p$-value for TFD vs PICT_plain ($p = 0.000061$)
* **Source Files:** Pooled binary detection vectors of `TFD` and `PICT_plain` across all 60 defects in `Results\*\matrix_long.csv`.
* **Recomputation Method:** Evaluated discordances: $b = |\{d \mid TFD(d)=1 \land PICT(d)=0\}| = 15$; $c = |\{d \mid TFD(d)=0 \land PICT(d)=1\}| = 0$. For $m = 15$ with $k=0$, exact two-sided binomial formula is $p = 2 \times \binom{15}{0} \times 0.5^{15} = 2 \times (1 / 32768) = 2 / 32768 \approx 0.000061035156$.
* **Verification:** Calculated value **$0.000061$** exactly matches `p_value_raw` in `Results/statistical_tests.csv` row 3.

### Independent Recomputation 4: BasicPackage LLM $C_1$ vs $C_2$ Input Overlap ($17 / 20$)
* **Source Files:** `D:\SWT301_TEST\Suites\BasicPackage\LLM\gemini_C1_r1.csv` and `gemini_C2_r1.csv`.
* **Recomputation Method:** Extracted the 10 input column values (columns 0..9) for each of the 20 rows from both files as formatted comma-delimited strings. Calculated the set intersection.
* **Result:** Exactly 17 input rows are identical between $C_1$ and $C_2$; 3 rows differ (`TC02`, `TC08`, `TC11`).
* **Verification:** Intersection count is exactly **17**; Jaccard similarity is $17 / (20 + 20 - 17) = 17 / 23 \approx 0.7391$. Matches `Results/diversity_and_overlap.csv` line 15.

### Independent Recomputation 5: Total Gemini LLM Generation Time ($19.69$ minutes)
* **Source File:** `D:\SWT301_TEST\Logs\generation_log.csv` (size: 2,126 bytes, 28 lines)
* **Recomputation Method:** Parsed column 9 (`minutes`) across all 24 LLM generation records:
  * BasicPackage: $0.55 + 0.46 + 0.56 + 0.55 + 0.43 + 0.59 = 3.14$
  * BasicPayroll: $0.90 + 0.69 + 0.62 + 0.52 + 0.44 + 0.61 = 3.78$
  * EnhancedPayroll: $1.05 + 1.28 + 1.50 + 0.98 + 1.11 + 1.10 = 7.02$
  * EnhancedPackage: $0.86 + 0.78 + 1.27 + 0.81 + 1.06 + 0.97 = 5.75$
* **Result:** Sum $= 3.14 + 3.78 + 7.02 + 5.75 = 19.69$ minutes.
* **Verification:** Sum is exactly **19.69 minutes** (1,181.4 seconds), matching `Results/cost_comparison.csv`.

---

## 11. Artifact & Figure Index

All benchmark analysis artifacts and visual figures are stored in `D:\SWT301_TEST\Results\`:
* [`primary_metrics.csv`](file:///D:/SWT301_TEST/Results/primary_metrics.csv) - Primary detection rates and `defects_per_test` across all 34 suites.
* [`secondary_metrics.csv`](file:///D:/SWT301_TEST/Results/secondary_metrics.csv) - Test validity, expected-value accuracy, and sensitivity metrics.
* [`defect_type_and_domain_breakdown.csv`](file:///D:/SWT301_TEST/Results/defect_type_and_domain_breakdown.csv) - Performance grouped by boundary, arithmetic, logic, conditional, and application domain.
* [`hybrid_analysis.csv`](file:///D:/SWT301_TEST/Results/hybrid_analysis.csv) - Synergistic defect gains from combining test generation paradigms.
* [`llm_factors.csv`](file:///D:/SWT301_TEST/Results/llm_factors.csv) - Descriptive factorial evaluation of detail level (low vs high) and shots (0, 1, 3).
* [`diversity_and_overlap.csv`](file:///D:/SWT301_TEST/Results/diversity_and_overlap.csv) - Jaccard input vector overlaps between conditions and methods.
* [`statistical_tests.csv`](file:///D:/SWT301_TEST/Results/statistical_tests.csv) - Cochran's Q and exact McNemar tests with Holm-Bonferroni corrections.
* [`cost_comparison.csv`](file:///D:/SWT301_TEST/Results/cost_comparison.csv) - Wall-clock effort vs defect exposure efficiency.
* `fig1_detection_rates.png` - Method comparison chart across applications.
* `fig2_defect_type_breakdown.png` - Defect type breakdown grouped bar chart.
* `fig3_hybrid_synergy.png` - Hybrid combination synergy bar chart.
* `fig4_llm_prompt_factors.png` - Prompt factors (detail and shots) chart.
* `fig5_cost_vs_detection.png` - Cost-efficiency scatter plot (time vs detection).

---

## 12. Limitations

The empirical findings of this benchmark should be interpreted in light of the following methodological constraints:
1. The seeded defects are mutations of specification constants and thresholds, so a threshold-driven tester can reach an artificial ceiling.
2. Pooled defects across applications are not fully independent.
3. Several conditions produced identical suites, so the 24 runs are not 24 independent samples.
4. "Best LLM" and "LLM union" are selected post hoc and use substantially more tests than TFD.
5. LLM minutes exclude specification and prompt preparation as well as format normalization.
6. TFD creation minutes are self-reported and could not be independently audited or verified; the tester states that no AI tool was used.
7. Both the application testbeds and seeded defect catalogues were reconstructed by us in Java.
8. The benchmark evaluates only a single model family (Gemini 3.1 Pro) with one repetition per condition.
9. The human testing baseline relies on only a single external tester.
10. PICT representative values for continuous parameters are the range midpoints chosen by us, which makes exact-boundary defects hard for PICT to detect.
11. For apps above quota the PICT suites are a seeded random sample (seed 42) of a larger generated set.
12. Some PICT suites are below quota because generation saturated pairwise coverage.
13. The specification contained worked examples that some suites reused.
14. The log labels the model as "Gemini 3.1 Pro" and the label is taken from our log, not from an official model identifier.
