# KNOWN ISSUES & BENCHMARK NOTES FOR PAPER AUTHORS

This document records all methodological nuances, known format anomalies, test generation constraints, and suite characteristics observed during the black-box benchmarking of **BasicPackage**, **BasicPayroll**, **EnhancedPackage**, and **EnhancedPayroll**.

---

## 1. Format Normalization of LLM-Generated Suites
* **Context:** Low-detail prompts (especially Condition $C_1$) in Gemini 3.1 Pro generated CSV outputs with minor formatting variations:
  * `BasicPackage/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column and titled the expected column `expected_output`.
  * `BasicPayroll/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column, intermediate `grossPay` calculation columns, and titled the net pay column `expectedNet`.
  * `EnhancedPayroll/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column, intermediate `grossPay` calculation columns, and titled the net pay column `Expected_NetPay`.
* **Remediation:** In accordance with Task B, exact byte-for-byte unnormalized originals were backed up in `Suites/_raw_llm/<App>/<file>`. The working suites in `Suites/<App>/LLM/` were normalized by stripping extraneous non-input columns and standardizing the header to match `Spec/csv_format.md`.
* **Integrity Assertion:** An automated SHA-256 and field-by-field value equality validator verified that **no input or expected value was altered, reordered, added, or deleted**. Detailed before/after hashes are recorded in `Audit/llm_normalization_log.csv`.

---

## 2. Test Suite Row Counts and Quota Discrepancies
* **Benchmark Quotas:** Specified target quotas were **20 rows** for Basic applications and **35 rows** for Enhanced applications.
* **TFD & LLM Suites:** All 4 Human TFD suites and all 24 Gemini LLM suites met their exact quotas (20 rows Basic, 35 rows Enhanced).
* **PICT Combinatorial Suites:**
  * `BasicPackage/PICT_plain`: 20 rows (meets quota).
  * `EnhancedPackage/PICT_plain`: 35 rows (meets quota).
  * `EnhancedPackage/PICT_constrained`: 35 rows (meets quota).
  * `BasicPayroll/PICT_plain`: **16 rows** (below 20-row quota).
  * `EnhancedPayroll/PICT_plain`: **27 rows** (below 35-row quota).
  * `EnhancedPayroll/PICT_constrained`: **32 rows** (below 35-row quota).
* **Reasoning:** Microsoft PICT optimizes for complete 2-way parameter combinatorial coverage. For `BasicPayroll` and `EnhancedPayroll`, all pairwise parameter combinations were mathematically saturated at 16, 27, and 32 test cases respectively. Adding arbitrary filler rows would artificially depress `defects_per_test` efficiency.

---

## 3. Specification Clarifications & Rounding Policies (v1.1)
* **Rounding Tie Policy:** Specifications clarify that half-up rounding applies to the final computed dollar amount at the second decimal place, rounding ties away from zero (e.g., $12.345 \to 12.35$ and $-161.795 \to -161.80$). Intermediate precision is preserved across all preceding calculation steps.
* **Negative Net Pay:** In Payroll systems, excessive voluntary deductions (e.g., parking, United Way, benefit plans with low or zero hours worked) yield valid negative net pay. The system does not clamp net pay to zero.

---

## 4. TFD Suite Provenance & Self-Reported Status
* **Authoring Context:** The TFD (Test-First Development) test suites were authored by an external human software tester working strictly from the functional specifications (`Spec/`) without access to seeded defects, mutant source code, or internal implementation classes.
* **Caveat for Publication:** The tester states that no AI tool was used; this statement and the effort times are self-reported and could not be independently verified.

---

## 5. Specification Example Ingestion in LLM Suites
* **Example Reuse:** Some few-shot Gemini LLM suites (notably $C_2$, $C_3$, $C_5$, $C_6$) ingested the worked specification examples provided in the prompt templates as part of their generated test cases (e.g. `BasicPackage` worked example $w=1.05, orig=1, dest=2, deliv=3$).
* **Verification:** Detailed inventory in `Audit/inventory_and_validation.md` catalogs each instance of example reuse.

---

## 6. Identical LLM Suites Across Conditions
* **Non-Independence:** Several prompt conditions produced identical suites (as sets and byte-identical outputs), meaning the 24 runs are not 24 independent samples:
  * `BasicPackage`: $C_2, C_3, C_4, C_5, C_6$ are 100% identical (2 distinct suites total).
  * `BasicPayroll`: All 6 conditions distinct (6 distinct suites total).
  * `EnhancedPackage`: $C_1, C_2, C_4$ are 100% identical (4 distinct suites total).
  * `EnhancedPayroll`: $C_4, C_5$ are 100% identical (5 distinct suites total).
