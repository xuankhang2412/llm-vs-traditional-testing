# Audit Report: PICT Suite Sizes, Efficiency, and Invalid Test Analysis

## 1. Executive Summary

This report documents the independent verification and audit of all six PICT-generated pairwise test suites across the four benchmark applications (`BasicPackage`, `BasicPayroll`, `EnhancedPackage`, and `EnhancedPayroll`). The audit recomputed all execution statistics using `SuiteRunner` with outputs directed to `Audit/runs/<app>/<suite>/`, preserving the integrity of existing files.

---

## 2. PICT Suite Size and Efficiency Metrics

| Application | Suite Name | Quota | Raw Size | Final Size | Invalid Tests | Valid Tests | Defects Detected | Total Defects | Detection Rate | Detection / Valid Test |
|---|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **BasicPackage** | `PICT_plain` | 20 | 32 | 20 | 0 | 20 | 8 | 10 | 80.00% | 0.4000 |
| **BasicPayroll** | `PICT_plain` | 20 | 16 | 16 | 0 | 16 | 8 | 10 | 80.00% | 0.5000 |
| **EnhancedPackage** | `PICT_plain` | 35 | 61 | 35 | 25 | 10 | 13 | 20 | 65.00% | 1.3000 |
| **EnhancedPackage** | `PICT_constrained` | 35 | 63 | 35 | 0 | 35 | 14 | 20 | 70.00% | 0.4000 |
| **EnhancedPayroll** | `PICT_plain` | 35 | 27 | 27 | 11 | 16 | 16 | 20 | 80.00% | 1.0000 |
| **EnhancedPayroll** | `PICT_constrained` | 35 | 32 | 32 | 0 | 32 | 13 | 20 | 65.00% | 0.4063 |

---

## 3. Comparison with Previously Reported Numbers

A strict line-by-line comparison between the newly audited runs in `Audit/runs/` and the previously reported metrics was conducted:

| Metric | Previously Reported | Audited Value | Discrepancy |
|---|:---:|:---:|:---:|
| **BasicPackage PICT_plain** (Detected / Invalid) | 8/10 / 0 | 8/10 / 0 | **None (Identical)** |
| **BasicPayroll PICT_plain** (Detected / Invalid) | 8/10 / 0 | 8/10 / 0 | **None (Identical)** |
| **EnhancedPackage PICT_plain** (Detected / Invalid) | 13/20 / 25 | 13/20 / 25 | **None (Identical)** |
| **EnhancedPackage PICT_constrained** (Detected / Invalid) | 14/20 / 0 | 14/20 / 0 | **None (Identical)** |
| **EnhancedPayroll PICT_plain** (Detected / Invalid) | 16/20 / 11 | 16/20 / 11 | **None (Identical)** |
| **EnhancedPayroll PICT_constrained** (Detected / Invalid) | 13/20 / 0 | 13/20 / 0 | **None (Identical)** |

**Result:** Zero discrepancies found across all suites, counts, and detection numbers.

---

## 4. Explanation of Suites Below Quota

Three suites produced fewer rows than their quota:
- **`BasicPayroll PICT_plain`**: 16 rows (quota: 20)
- **`EnhancedPayroll PICT_plain`**: 27 rows (quota: 35)
- **`EnhancedPayroll PICT_constrained`**: 32 rows (quota: 35)

### Combinatorial Explanation
In pairwise combinatorial testing, the minimum theoretical size of a 2-way test suite is lower-bounded by the Cartesian product of the two largest parameter domain cardinalities: $N \ge \max_{i \ne j} (|V_i| \times |V_j|)$. 

For `BasicPayroll`, the parameters are small discrete categories (e.g., 4 medical plans, 3 dental, 3 vision, 3 salary tiers, and binary flags). The maximum 2-way Cartesian product across any two variables is $4 \times 3 = 12$. Microsoft PICT's greedy clique-cover algorithm easily satisfies all $\binom{12}{2} = 66$ pairwise interactions in just 16 rows. 

Similarly, for `EnhancedPayroll plain`, the two largest cardinalities are hourly rate (6 levels) and overtime hours or medical plan (4 levels), yielding a maximum pairwise interaction product of $6 \times 4 = 24$. PICT achieves 100% pairwise coverage in 27 rows. When constraints were added (`EnhancedPayroll constrained`), mutually exclusive domain combinations forced PICT to generate additional non-conflicting tuples, reaching 32 rows. Because 16, 27, and 32 are strictly smaller than their quotas (20 and 35), all generated test vectors were retained without downsampling. Generating more rows would only add redundant pairwise combinations that violate pairwise generation principles.

---

## 5. Detailed Breakdown of Invalid Tests in `PICT_plain` (Task B)

### 5.1 EnhancedPackage `PICT_plain` (25 Invalid Tests)

#### Summary by Rule
| Violated Rule | Number of Tests | Percentage |
|---|:---:|:---:|
| **Packaging Weight Limit** | 14 | 56.0% |
| **Note N4 (Business requires signature 1 or 2)** | 6 | 24.0% |
| **Note N7 (Fragile and dangerous cannot be combined)** | 5 | 20.0% |
| **Total Invalid** | **25** | **100.0%** |

#### Test-by-Test Listing
1. **TC01**: `[w=1.0, orig=4, dest=1, deliv=2, pack=5, sig=0, loc=1, sat=1, hold=0, frag=1, dang=1]`
   - **Rule**: Note N7 (Fragile and dangerous cannot be combined)
   - **Reference Message**: `Fragile and dangerous cannot be combined (N7)`
2. **TC03**: `[w=17.5, orig=4, dest=2, deliv=4, pack=5, sig=2, loc=1, sat=0, hold=0, frag=1, dang=1]`
   - **Rule**: Packaging Weight Limit
   - **Reference Message**: `Tube weight limit exceeded (got 17.5)`
3. **TC04**: `[w=6.0, orig=3, dest=1, deliv=4, pack=3, sig=0, loc=0, sat=1, hold=1, frag=0, dang=0]`
   - **Rule**: Note N4 (Business requires signature 1 or 2)
   - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
4. **TC05**: `[w=32.5, orig=3, dest=3, deliv=5, pack=1, sig=0, loc=1, sat=1, hold=0, frag=1, dang=1]`
   - **Rule**: Packaging Weight Limit
   - **Reference Message**: `Envelope weight limit exceeded (got 32.5)`
5. **TC06**: `[w=6.0, orig=1, dest=2, deliv=5, pack=3, sig=2, loc=1, sat=0, hold=0, frag=1, dang=1]`
   - **Rule**: Note N7 (Fragile and dangerous cannot be combined)
   - **Reference Message**: `Fragile and dangerous cannot be combined (N7)`
6. **TC07**: `[w=45.0, orig=1, dest=1, deliv=3, pack=8, sig=2, loc=1, sat=1, hold=0, frag=1, dang=1]`
   - **Rule**: Note N7 (Fragile and dangerous cannot be combined)
   - **Reference Message**: `Fragile and dangerous cannot be combined (N7)`
7. **TC08**: `[w=55.0, orig=3, dest=4, deliv=5, pack=2, sig=2, loc=1, sat=0, hold=1, frag=1, dang=0]`
   - **Rule**: Packaging Weight Limit
   - **Reference Message**: `Small Box weight limit exceeded (got 55.0)`
8. **TC10**: `[w=45.0, orig=4, dest=4, deliv=5, pack=4, sig=1, loc=1, sat=0, hold=1, frag=1, dang=1]`
   - **Rule**: Packaging Weight Limit
   - **Reference Message**: `Large Box weight limit exceeded (got 45.0)`
9. **TC11**: `[w=55.0, orig=3, dest=2, deliv=2, pack=4, sig=2, loc=0, sat=1, hold=1, frag=1, dang=0]`
   - **Rule**: Packaging Weight Limit
   - **Reference Message**: `Large Box weight limit exceeded (got 55.0)`
10. **TC15**: `[w=32.5, orig=3, dest=2, deliv=3, pack=5, sig=2, loc=0, sat=1, hold=1, frag=0, dang=1]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Tube weight limit exceeded (got 32.5)`
11. **TC18**: `[w=6.0, orig=4, dest=2, deliv=4, pack=2, sig=0, loc=0, sat=0, hold=1, frag=0, dang=1]`
    - **Rule**: Note N4 (Business requires signature 1 or 2)
    - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
12. **TC20**: `[w=1.0, orig=3, dest=1, deliv=6, pack=1, sig=2, loc=0, sat=1, hold=1, frag=1, dang=1]`
    - **Rule**: Note N7 (Fragile and dangerous cannot be combined)
    - **Reference Message**: `Fragile and dangerous cannot be combined (N7)`
13. **TC21**: `[w=17.5, orig=1, dest=1, deliv=2, pack=6, sig=0, loc=0, sat=0, hold=1, frag=0, dang=0]`
    - **Rule**: Note N4 (Business requires signature 1 or 2)
    - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
14. **TC23**: `[w=55.0, orig=4, dest=4, deliv=6, pack=8, sig=0, loc=0, sat=1, hold=1, frag=1, dang=0]`
    - **Rule**: Note N4 (Business requires signature 1 or 2)
    - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
15. **TC24**: `[w=32.5, orig=4, dest=4, deliv=7, pack=2, sig=0, loc=1, sat=0, hold=0, frag=0, dang=0]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Small Box weight limit exceeded (got 32.5)`
16. **TC25**: `[w=17.5, orig=2, dest=2, deliv=3, pack=2, sig=1, loc=0, sat=0, hold=0, frag=1, dang=0]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Small Box weight limit exceeded (got 17.5)`
17. **TC27**: `[w=32.5, orig=4, dest=3, deliv=3, pack=4, sig=0, loc=0, sat=0, hold=0, frag=1, dang=1]`
    - **Rule**: Note N4 (Business requires signature 1 or 2)
    - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
18. **TC28**: `[w=45.0, orig=4, dest=1, deliv=3, pack=3, sig=0, loc=0, sat=0, hold=0, frag=1, dang=0]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Medium Box weight limit exceeded (got 45.0)`
19. **TC29**: `[w=45.0, orig=1, dest=1, deliv=7, pack=1, sig=2, loc=0, sat=0, hold=0, frag=0, dang=1]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Envelope weight limit exceeded (got 45.0)`
20. **TC30**: `[w=45.0, orig=2, dest=1, deliv=2, pack=1, sig=1, loc=0, sat=1, hold=0, frag=0, dang=1]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Envelope weight limit exceeded (got 45.0)`
21. **TC31**: `[w=55.0, orig=1, dest=1, deliv=3, pack=1, sig=0, loc=1, sat=0, hold=1, frag=0, dang=0]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Envelope weight limit exceeded (got 55.0)`
22. **TC32**: `[w=32.5, orig=4, dest=1, deliv=6, pack=6, sig=1, loc=1, sat=1, hold=1, frag=1, dang=1]`
    - **Rule**: Note N7 (Fragile and dangerous cannot be combined)
    - **Reference Message**: `Fragile and dangerous cannot be combined (N7)`
23. **TC33**: `[w=1.0, orig=4, dest=1, deliv=3, pack=2, sig=0, loc=0, sat=1, hold=1, frag=0, dang=1]`
    - **Rule**: Note N4 (Business requires signature 1 or 2)
    - **Reference Message**: `Business location requires signature 1 or 2 (got 0)`
24. **TC34**: `[w=55.0, orig=3, dest=3, deliv=4, pack=5, sig=1, loc=1, sat=1, hold=0, frag=0, dang=1]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Tube weight limit exceeded (got 55.0)`
25. **TC35**: `[w=32.5, orig=4, dest=3, deliv=4, pack=1, sig=0, loc=1, sat=0, hold=1, frag=0, dang=0]`
    - **Rule**: Packaging Weight Limit
    - **Reference Message**: `Envelope weight limit exceeded (got 32.5)`

---

### 5.2 EnhancedPayroll `PICT_plain` (11 Invalid Tests)

#### Summary by Rule
| Violated Rule | Number of Tests | Percentage |
|---|:---:|:---:|
| **Note N4 (Medical requires dental)** | 5 | 45.5% |
| **Note N9 (LTD=1 requires STD=1)** | 3 | 27.3% |
| **Note N5 (High-earner h>=40.01 requires medical plan 2 or 3)** | 2 | 18.2% |
| **Note N1 (Low-wage hourly $15-$20 limits overtime <= 10)** | 1 | 9.1% |
| **Total Invalid** | **11** | **100.0%** |

#### Test-by-Test Listing
1. **TC04**: `[h=27.50, reg=40.0, ot=12.0, hol=8.0, st=3, med=1, den=1, vis=0, uw=5.0, pen=0.0, ltd=1, std=0, park=1]`
   - **Rule**: Note N9 (LTD=1 requires STD=1)
   - **Reference Message**: `Long-term disability requires short-term disability (N9)`
2. **TC05**: `[h=45.00, reg=40.0, ot=12.0, hol=0.0, st=1, med=0, den=0, vis=1, uw=20.0, pen=50.0, ltd=1, std=0, park=1]`
   - **Rule**: Note N5 (High-earner h>=40.01 requires medical plan 2 or 3)
   - **Reference Message**: `Hourly rate >= $40.01 requires medical plan 2 or 3 (got 0)`
3. **TC09**: `[h=32.50, reg=40.0, ot=8.0, hol=8.0, st=2, med=1, den=0, vis=2, uw=20.0, pen=0.0, ltd=1, std=1, park=0]`
   - **Rule**: Note N4 (Medical requires dental)
   - **Reference Message**: `Medical enrollment requires dental enrollment (N4)`
4. **TC12**: `[h=17.50, reg=40.0, ot=0.0, hol=3.0, st=1, med=1, den=0, vis=0, uw=0.0, pen=0.0, ltd=0, std=0, park=1]`
   - **Rule**: Note N4 (Medical requires dental)
   - **Reference Message**: `Medical enrollment requires dental enrollment (N4)`
5. **TC17**: `[h=32.50, reg=40.0, ot=0.0, hol=3.0, st=3, med=3, den=0, vis=1, uw=0.0, pen=200.0, ltd=0, std=0, park=0]`
   - **Rule**: Note N4 (Medical requires dental)
   - **Reference Message**: `Medical enrollment requires dental enrollment (N4)`
6. **TC19**: `[h=22.50, reg=40.0, ot=3.0, hol=0.0, st=1, med=3, den=0, vis=2, uw=20.0, pen=50.0, ltd=1, std=1, park=0]`
   - **Rule**: Note N4 (Medical requires dental)
   - **Reference Message**: `Medical enrollment requires dental enrollment (N4)`
7. **TC21**: `[h=17.50, reg=40.0, ot=12.0, hol=8.0, st=2, med=3, den=2, vis=2, uw=5.0, pen=0.0, ltd=0, std=1, park=0]`
   - **Rule**: Note N1 (Low-wage hourly $15-$20 limits overtime <= 10)
   - **Reference Message**: `Hourly rate $15-20 limits overtime to 10.0 hours (got 12.0)`
8. **TC22**: `[h=22.50, reg=40.0, ot=12.0, hol=8.0, st=1, med=2, den=0, vis=1, uw=0.0, pen=200.0, ltd=1, std=1, park=1]`
   - **Rule**: Note N4 (Medical requires dental)
   - **Reference Message**: `Medical enrollment requires dental enrollment (N4)`
9. **TC24**: `[h=17.50, reg=40.0, ot=8.0, hol=8.0, st=3, med=3, den=1, vis=2, uw=20.0, pen=50.0, ltd=1, std=0, park=1]`
   - **Rule**: Note N9 (LTD=1 requires STD=1)
   - **Reference Message**: `Long-term disability requires short-term disability (N9)`
10. **TC25**: `[h=27.50, reg=40.0, ot=12.0, hol=3.0, st=2, med=3, den=2, vis=1, uw=5.0, pen=0.0, ltd=1, std=0, park=0]`
    - **Rule**: Note N9 (LTD=1 requires STD=1)
    - **Reference Message**: `Long-term disability requires short-term disability (N9)`
11. **TC27**: `[h=45.00, reg=40.0, ot=3.0, hol=8.0, st=2, med=1, den=2, vis=2, uw=0.0, pen=50.0, ltd=0, std=0, park=0]`
    - **Rule**: Note N5 (High-earner h>=40.01 requires medical plan 2 or 3)
    - **Reference Message**: `Hourly rate >= $40.01 requires medical plan 2 or 3 (got 1)`

