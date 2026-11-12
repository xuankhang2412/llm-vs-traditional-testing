# Extra Analysis of PICT Suites: Detection Efficiency, Oracle Types, and Constraint Trade-offs

## 1. Overview
This report extends the PICT suite evaluation with metrics based on total suite size, categorizes defect detections by test validity status (whether a defect was exposed exclusively by invalid test vectors, exclusively by valid test vectors, or both), and investigates the differences between unconstrained (`PICT_plain`) and constrained (`PICT_constrained`) pairwise suites.

---

## 2. Summary Table

| Application | Suite Name | Total Tests | Defects Detected | Total Defects | Detection Rate (Detected / Total Tests) | Defects Detected ONLY by Invalid Tests | Defects Detected ONLY by Valid Tests | Defects Missed by Suite |
|---|---|:---:|:---:|:---:|:---:|---|---|---|
| **BasicPackage** | `PICT_plain` | 20 | 8 | 10 | **0.4000** (40.00%) | *None* | D3, D4, D5, D6, D7, D8, D9, D10 | D1, D2 |
| **BasicPayroll** | `PICT_plain` | 16 | 8 | 10 | **0.5000** (50.00%) | *None* | D2, D3, D5, D6, D7, D8, D9, D10 | D1, D4 |
| **EnhancedPackage** | `PICT_plain` | 35 | 13 | 20 | **0.3714** (37.14%) | D9 | D1, D3, D5, D6, D7, D8, D10, D13, D15, D16, D17, D20 | D2, D4, D11, D12, D14, D18, D19 |
| **EnhancedPackage** | `PICT_constrained` | 35 | 14 | 20 | **0.4000** (40.00%) | *None* | D1, D2, D3, D5, D6, D7, D8, D10, D13, D15, D16, D17, D19, D20 | D4, D9, D11, D12, D14, D18 |
| **EnhancedPayroll** | `PICT_plain` | 27 | 16 | 20 | **0.5926** (59.26%) | D9, D12, D15 | D1, D2, D3, D4, D6, D7, D10, D13, D14, D16, D18, D19, D20 | D5, D8, D11, D17 |
| **EnhancedPayroll** | `PICT_constrained` | 32 | 13 | 20 | **0.4063** (40.63%) | *None* | D1, D2, D3, D4, D6, D7, D10, D13, D14, D16, D18, D19, D20 | D5, D8, D9, D11, D12, D15, D17 |

---

## 3. Defects Missed by ALL PICT Suites per Application

### 3.1 Basic Applications
- **BasicPackage** (Missed by `PICT_plain`):
  - **D1**: Origin 3 to Destination 4 rate arithmetic change ($2.30 \to 2.50$).
  - **D2**: Priority Overnight multiplier arithmetic change ($1.20 \to 1.50$).
- **BasicPayroll** (Missed by `PICT_plain`):
  - **D1**: Federal tax rate bracket adjustment.
  - **D4**: Arkansas lower bracket tax logic.

### 3.2 Enhanced Applications (Missed by BOTH `plain` and `constrained` Suites)
- **EnhancedPackage** (Missed by both `PICT_plain` and `PICT_constrained`):
  - **D4** `[boundary]`: Small Box capacity boundary changed from $w \le 10$ to $w < 10$. (Missed because PICT sampled range midpoints 1.0, 6.0, 17.5 rather than exact boundary 10.0).
  - **D11** `[boundary]`: Heavy parcel signature threshold shifted from $w > 50$ to $w > 55$. (Requires an unsigned parcel with $50 < w \le 55$).
  - **D12** `[boundary]`: Envelope capacity boundary changed from $w \le 2$ to $w < 2$. (Missed because PICT sampled midpoint 1.0).
  - **D14** `[boundary]`: Medium Box capacity boundary changed from $w \le 25$ to $w < 25$. (Missed because PICT sampled midpoint 17.5).
  - **D18** `[conditional]`: Dangerous goods rule N8 permits Delivery Option 5.
- **EnhancedPayroll** (Missed by both `PICT_plain` and `PICT_constrained`):
  - **D5** `[boundary]`: High-earner medical mandate begins at hourly rate $\$40.00$ instead of $\$40.01$.
  - **D8** `[boundary]`: Community incentive credit minimum threshold shifted from $\$10.00$ to $\$20.00$.
  - **D11** `[boundary]`: Overtime production bonus triggers at $\text{overtime} \ge 5.0$ instead of $> 5.0$. (PICT sampled 0.0, 3.0, 8.0, 12.0; neither 5.0 was evaluated).
  - **D17** `[boundary]`: Mississippi middle bracket threshold begins at $\$25.00$ instead of $\$25.01$.

*Observation*: Pairwise generation with domain midpoints inherently misses off-by-one and boundary shift defects unless boundary values are explicitly included as parameter choices in the model.

---

## 4. Defect Exposure by Invalid Tests

### 4.1 EnhancedPackage `PICT_plain`
- **D9** `[conditional]`: Removes Rule N7 (`fragile = 1` and `dangerous = 1` rejected).
  - *Exposure mechanism*: In the reference implementation, combining fragile and dangerous produces `ERROR`. D9 removes this check, causing the calculation to succeed and output a numerical value. Because valid test cases are constrained to never combine fragile and dangerous goods, D9 **can only be detected by an invalid test case**.

### 4.2 EnhancedPayroll `PICT_plain`
- **D9** `[conditional]`: Removes Rule N9 (`ltd = 1` requires `std = 1`).
  - *Exposure mechanism*: Exposed by `ltd = 1, std = 0`. This input is rejected as `ERROR` by the reference implementation, but succeeds under D9.
- **D12** `[boundary]`: Shifts Rule N1 low-wage overtime limit from $10.0 \to 12.0$ hours.
  - *Exposure mechanism*: Exposed by an employee with hourly rate $\$15 \le h \le \$20$ working $12.0$ overtime hours. The reference implementation rejects this input with `ERROR`, whereas D12 accepts it.
- **D15** `[conditional]`: Removes Rule N4 (`medical != 0` requires `dental != 0`).
  - *Exposure mechanism*: Exposed by selecting medical plan $> 0$ with dental plan $0$. The reference implementation outputs `ERROR`, whereas D15 computes a salary.

---

## 5. Hypothesis: Why EnhancedPayroll `PICT_plain` Detects More Defects Than `PICT_constrained`

> **[HYPOTHESIS]**  
> `EnhancedPayroll PICT_plain` detects 16 defects compared to 13 defects detected by `EnhancedPayroll PICT_constrained` because the unconstrained suite accidentally acts as a negative test generator. By freely generating tuples without constraint pruning, `PICT_plain` produces test vectors that deliberately violate the business validation rules (such as low-wage overtime caps, medical-dental dependencies, and disability hierarchies). 
> 
> Three seeded defects (D9, D12, and D15) specifically mutate these validation checks (removing the check or expanding an invalid threshold). In the reference implementation, these test vectors result in an `ERROR` rejection, but under the mutated code, they execute successfully and calculate a numerical net pay, causing a distinct mismatch (`HIT_REF = 1`). 
> 
> In contrast, `PICT_constrained` enforces the domain rules during generation and therefore produces zero invalid test cases. As a direct consequence, `PICT_constrained` lacks the negative test cases necessary to kill mutant branches residing within the input validation routines, resulting in a lower raw defect count (13 vs 16) despite having a higher valid test density.

