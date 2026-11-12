const fs = require('fs');
const path = require('path');
const tableGen = require('./generate_and_audit_tables.js');

const root = 'D:/SWT301_TEST';
const summaryPath = path.join(root, 'Results/summary.md');
let summary = fs.readFileSync(summaryPath, 'utf8');

// Replace Table 1
const t1Regex = /\| Application \| Test Suite \| Method \| Condition \| Total Defects \|[\s\S]*?(?=\n\n)/;
summary = summary.replace(t1Regex, tableGen.generateTable1());

// Replace Table 2
const t2Regex = /\| Application \| Suite \| Invalid Tests \| Invalid Rate \|[\s\S]*?(?=\n\n\*Observations on Oracles)/;
summary = summary.replace(t2Regex, tableGen.generateTable2());

// Replace Table 3
const t3Regex = /\| Defect Category \| Total Seeded \| Human TFD Detected \|[\s\S]*?(?=\n\n### Breakdown by Application Domain)/;
summary = summary.replace(t3Regex, tableGen.generateTable3());

// Replace Table 4
const t4Regex = /\| Application \| Total Defects \| Combination \| Methods Included \|[\s\S]*?(?=\n\n\*Note: "Best LLM" is selected)/;
summary = summary.replace(t4Regex, tableGen.generateTable4());

// Replace Section 6 tables & text
// Let's inspect section 6 structure
const oldSec6Start = summary.indexOf('## 6. LLM Prompt Factors Analysis');
const oldSec7Start = summary.indexOf('## 7. Input Diversity & Cross-Suite Overlap');

const newSec6 = `## 6. LLM Prompt Factors Analysis

The benchmark examined two orthogonal prompt dimensions across 24 generation runs (4 applications $\\times$ 6 conditions):
* **Specification Detail Level:**
  * *Low Detail ($C_1..C_3$):* Concise functional rules, condensed domain tables.
  * *High Detail ($C_4..C_6$):* Full edge-case enumerations, explicit notes, detailed notes explanations.
* **Example Shots:**
  * *Zero-shot ($C_1, C_4$):* Specification only; no worked input/output examples.
  * *One-shot ($C_2, C_5$):* Specification plus 1 valid example.
  * *Three-shot ($C_3, C_6$):* Specification plus 3 diverse valid/invalid examples.

Because only ONE repetition was generated per condition, results are presented as descriptive statistics (both pooled defect-opportunity counts and the unweighted mean of per-suite detection rates across the 4 applications). Note that in the **mean of per-suite rates**, Basic applications (10 defects each) and Enhanced applications (20 defects each) receive equal weight ($0.25$ each), whereas pooled metrics weight Enhanced defects twice as heavily as Basic defects.

${tableGen.generateTable5()}

### By-Condition Descriptive Summary

${tableGen.generateTable6()}

*Insights:*
* **Prompt Detail Level & Few-Shot Variations:** With only one repetition per prompt condition, differences of 1–2 defects across suites are purely descriptive and not statistically interpretable. Across all four applications ($10+10+20+20 = 60$ defect opportunities per condition):
  * **Detail Level:** Low Detail prompts ($C_1..C_3$, 12 suites, $3 \\times 60 = 180$ opportunities) detected **165 / 180** pooled defects (pooled rate **91.67%**; mean of per-suite rates **93.75%**; **13.75** defects detected per suite on average). High Detail prompts ($C_4..C_6$, 12 suites, $3 \\times 60 = 180$ opportunities) detected **161 / 180** pooled defects (pooled rate **89.44%**; mean of per-suite rates **92.08%**; **13.42** defects detected per suite on average). The absolute difference is only 4 defect detections across 180 opportunities.
  * **Example Shots:** Zero-shot ($C_1, C_4$, 8 suites, $2 \\times 60 = 120$ opportunities) detected **109 / 120** pooled defects (pooled rate **90.83%**; mean of per-suite rates **93.13%**; **13.625** defects detected per suite). One-shot ($C_2, C_5$, 8 suites, $2 \\times 60 = 120$ opportunities) detected **109 / 120** pooled defects (pooled rate **90.83%**; mean of per-suite rates **93.13%**; **13.625** defects detected per suite). Three-shot ($C_3, C_6$, 8 suites, $2 \\times 60 = 120$ opportunities) detected **108 / 120** pooled defects (pooled rate **90.00%**; mean of per-suite rates **92.50%**; **13.50** defects detected per suite).
* A conjecture that high-detail prompts might constrain exploratory boundary sampling remains an untested hypothesis that cannot be evaluated without multi-repetition variance data.

*Raw data source:* [\`Results/llm_factors.csv\`](file:///D:/SWT301_TEST/Results/llm_factors.csv)

---

`;

// Replace Section 7
const oldSec8Start = summary.indexOf('## 8. Statistical Hypothesis Testing');

const newSec7 = `## 7. Input Diversity & Cross-Suite Overlap

To determine whether the test generation techniques explore distinct regions of the input space, input vectors (excluding expected outputs) were compared pairwise.

### Input Uniqueness within LLM Suites
* In all 24 Gemini LLM suites across the four applications, **100% of generated rows were unique within each suite** (20/20 distinct inputs in Basic apps; 35/35 distinct inputs in Enhanced apps). Zero intra-suite duplicate rows were produced.

### Identical Suites and Input Sets Audit
An audit grouping LLM conditions whose INPUT rows are identical as sets reveals that several prompt conditions produced identical suites. Consequently, the 24 generation runs are **not 24 independent samples**:
* **BasicPackage (2 distinct suites; 23 distinct input rows in LLM union):**
  * *Group 1 [C1] (1 condition, 20 rows):* \`gen_log\` = \`2026-11-04 11:25\`, \`mtime\` = \`2026-11-05T05:20:54.477Z\`. Expected column identical across group: YES.
  * *Group 2 [C2, C3, C4, C5, C6] (5 conditions, 20 rows):* 100% identical input sets and identical expected columns across all 5 conditions.
    * C2: \`gen_log\` = \`2026-11-04 11:28\`, \`mtime\` = \`2026-11-04T04:47:20.538Z\`
    * C3: \`gen_log\` = \`2026-11-04 11:29\`, \`mtime\` = \`2026-11-04T04:47:30.173Z\`
    * C4: \`gen_log\` = \`2026-11-04 11:30\`, \`mtime\` = \`2026-11-04T04:47:39.990Z\`
    * C5: \`gen_log\` = \`2026-11-04 11:32\`, \`mtime\` = \`2026-11-04T04:47:49.460Z\`
    * C6: \`gen_log\` = \`2026-11-04 11:33\`, \`mtime\` = \`2026-11-04T04:47:59.170Z\`
* **BasicPayroll (6 distinct suites; 73 distinct input rows in LLM union):**
  * All 6 conditions generated distinct input sets (all singletons).
    * C1: \`gen_log\` = \`2026-11-04 13:11\`, \`mtime\` = \`2026-11-05T05:20:54.500Z\`
    * C2: \`gen_log\` = \`2026-11-04 13:13\`, \`mtime\` = \`2026-11-04T06:22:58.994Z\`
    * C3: \`gen_log\` = \`2026-11-04 13:15\`, \`mtime\` = \`2026-11-04T06:23:11.289Z\`
    * C4: \`gen_log\` = \`2026-11-04 13:15\`, \`mtime\` = \`2026-11-04T06:23:21.448Z\`
    * C5: \`gen_log\` = \`2026-11-04 13:16\`, \`mtime\` = \`2026-11-04T06:23:30.967Z\`
    * C6: \`gen_log\` = \`2026-11-04 13:17\`, \`mtime\` = \`2026-11-04T06:23:41.264Z\`
* **EnhancedPackage (4 distinct suites; 77 distinct input rows in LLM union):**
  * *Group 1 [C1, C2, C4] (3 conditions, 35 rows):* 100% identical input sets and identical expected columns across all 3 conditions.
    * C1: \`gen_log\` = \`2026-11-04 13:43\`, \`mtime\` = \`2026-11-04T06:54:26.447Z\`
    * C2: \`gen_log\` = \`2026-11-04 13:44\`, \`mtime\` = \`2026-11-04T06:54:35.793Z\`
    * C4: \`gen_log\` = \`2026-11-04 13:47\`, \`mtime\` = \`2026-11-04T06:54:51.325Z\`
  * *Group 2 [C3] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:45\`, \`mtime\` = \`2026-11-04T06:54:43.562Z\`
  * *Group 3 [C5] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:48\`, \`mtime\` = \`2026-11-04T06:54:59.555Z\`
  * *Group 4 [C6] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:50\`, \`mtime\` = \`2026-11-04T06:55:08.545Z\`
* **EnhancedPayroll (5 distinct suites; 121 distinct input rows in LLM union):**
  * *Group 1 [C1] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:26\`, \`mtime\` = \`2026-11-05T05:20:54.516Z\`
  * *Group 2 [C2] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:27\`, \`mtime\` = \`2026-11-04T06:41:27.084Z\`
  * *Group 3 [C3] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:29\`, \`mtime\` = \`2026-11-04T06:41:40.260Z\`
  * *Group 4 [C4, C5] (2 conditions, 35 rows):* 100% identical input sets and identical expected columns.
    * C4: \`gen_log\` = \`2026-11-04 13:31\`, \`mtime\` = \`2026-11-04T06:41:47.884Z\`
    * C5: \`gen_log\` = \`2026-11-04 13:32\`, \`mtime\` = \`2026-11-04T06:41:57.055Z\`
  * *Group 5 [C6] (1 condition, 35 rows):* \`gen_log\` = \`2026-11-04 13:34\`, \`mtime\` = \`2026-11-04T06:42:07.171Z\`

### Pairwise Cross-Condition Overlap (Within App)
* **BasicPackage:** Condition $C_1$ shared 17/20 inputs with $C_2..C_6$ (Jaccard similarity = 0.7391), while conditions $C_2$ through $C_6$ produced 100% identical inputs to each other (20/20 shared rows, Jaccard similarity = 1.0000).
* **BasicPayroll:** Condition $C_1$ shared 0/20 rows with any other condition (Jaccard similarity = 0.0000). Among conditions $C_2..C_6$, pairwise overlap ranged from 5 to 13 shared rows out of 20 (Jaccard similarity 0.1429 to 0.4815).
* **EnhancedPackage:** Pairwise overlap between LLM conditions ranged from 7 to 35 shared rows out of 35 (Jaccard similarity 0.1111 to 1.0000). Identical inputs were produced between $C_1, C_2, C_4$ (35/35 shared rows, Jaccard = 1.0000), while differing pairs shared between 7 and 25 rows (Jaccard similarity 0.1111 to 0.5556).
* **EnhancedPayroll:** Condition $C_1$ shared 0/35 rows with any other condition (Jaccard similarity = 0.0000). Conditions $C_4$ and $C_5$ produced identical inputs (35/35 shared rows, Jaccard similarity = 1.0000), while other pairwise combinations shared between 5 and 23 rows (Jaccard similarity 0.0769 to 0.4894).

### Cross-Method Overlap (LLM vs TFD vs PICT)
* **LLM vs TFD Overlap:** In \`BasicPackage\`, all LLM conditions shared exactly 1 test with TFD (Jaccard = 0.0256). In \`BasicPayroll\`, LLM conditions shared between 0 and 5 tests with TFD (Jaccard range: 0.0000 to 0.1429). In \`EnhancedPackage\`, LLM conditions shared 1 to 2 tests with TFD (Jaccard range: 0.0145 to 0.0294). In \`EnhancedPayroll\`, LLM conditions shared 0 to 3 tests with TFD (Jaccard range: 0.0000 to 0.0448).
* **LLM vs PICT Overlap:** Exactly 0 shared input rows across all 4 applications (Jaccard = 0.0000).
* **TFD vs PICT Overlap:** Exactly 0 shared input rows across all 4 applications (Jaccard = 0.0000).

*Implication:* The high defect detection achieved by combining methods is directly attributable to their input orthogonalities: PICT systematically covers parameter tuples, LLM explores boundary clusters and error semantics, and TFD targets domain-specific business exceptions.

*Raw data source:* [\`Results/diversity_and_overlap.csv\`](file:///D:/SWT301_TEST/Results/diversity_and_overlap.csv)

---

`;

// Replace Table 7
const t7Regex = /\| Method A \| Method B \| \$b\$ \(\$A=1, B=0\$\) \|[\s\S]*?(?=\n\n> \[!WARNING\] Statistical Power Caveat)/;
summary = summary.replace(t7Regex, tableGen.generateTable7());

// Replace Table 8
const t8Regex = /\| Method \| BasicPackage \| BasicPayroll \| EnhancedPackage \| EnhancedPayroll \| Total Time Across All 4 Apps \|[\s\S]*?(?=\n\n\*Efficiency Trade-Off Analysis:\*)/;
summary = summary.replace(t8Regex, tableGen.generateTable8());

// Stitch Sections 6 and 7
summary = summary.substring(0, oldSec6Start) + newSec6 + newSec7 + summary.substring(oldSec8Start);

// Replace Section 12 (Limitations)
const sec12Start = summary.indexOf('## 12. Limitations');
const newSec12 = `## 12. Limitations

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
`;

summary = summary.substring(0, sec12Start) + newSec12;

fs.writeFileSync(summaryPath, summary, 'utf8');
console.log('Successfully updated Results/summary.md with regenerated tables and corrected text!');

