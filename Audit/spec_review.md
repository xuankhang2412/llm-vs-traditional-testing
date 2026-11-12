# Specification Review: Ambiguities, Undefined Behaviors, and Edge Cases

## 1. Scope and Methodology
This review examines the four clean specification documents (`basic_package_spec.md`, `basic_payroll_spec.md`, `enhanced_package_spec.md`, and `enhanced_payroll_spec.md`) from the perspective of an independent QA engineer and test creator. It identifies potential ambiguities, missing order-of-operations details, undefined behaviors, and areas where a human tester could reasonably calculate a differing result.

---

## 2. Review by Application

### 2.1 Basic Package (`basic_package_spec.md`)
1. **Intermediate vs. Final Rounding**:
   - *Observation*: Section 4.4 states "The final total cost is rounded to exactly two decimal places using standard half-up rounding." 
   - *Risk*: A human tester might round intermediate values (such as $\text{baseCost} = \text{baseRate} \times \text{weight}$ or $\text{multipliedCost} = \text{baseCost} \times \text{deliveryMultiplier}$) to 2 decimal places before adding surcharges. Because delivery multipliers include fractional rates like 2.1, 1.2, 0.9, 0.6, intermediate rounding can introduce $\pm \$0.01$ discrepancies compared to retaining full `BigDecimal` precision until final output.
2. **Numeric Precision and Floating-Point Edge Cases**:
   - *Observation*: Weight domain is specified as `0.0 < weight <= 60.0`.
   - *Risk*: The spec does not specify maximum decimal precision for weight (e.g., $0.0001$ lbs vs $0.1$ lbs). A tester providing floating-point binary approximations (e.g. `0.7000000000000001`) might expect exact decimal parsing.
3. **Data Types for Integer Flags**:
   - *Observation*: Coded fields (e.g., `origin`, `signatureOption`) are defined as integers.
   - *Risk*: Undefined behavior if non-integer numerical inputs (e.g. `1.0` or `2.5`) appear in CSV files. The specification implies integer parsing, where non-integer tokens cause an input parsing error (`ERROR`).

---

### 2.2 Basic Payroll (`basic_payroll_spec.md`)
1. **Negative Net Salary Allowance**:
   - *Observation*: The specification computes $\text{netSalary} = \text{grossSalary} - \text{totalTaxes} - \text{totalDeductions}$.
   - *Risk*: For low hours worked (e.g., 2 regular hours at $15.00/hr = $30.00 gross) with substantial voluntary elections (e.g., Medical Plan 3 at $125.00 + Parking at $39.50), deductions exceed gross salary, causing net salary to be negative (e.g., $-\$134.50$). The spec does not explicitly state whether negative net pay is valid or whether it must be clamped to $\$0.00$ or rejected.
2. **Pension Cap Calculation Precision**:
   - *Observation*: $\text{pensionDeduction} = \min(\text{pension}, 0.10 \times \text{grossSalary})$.
   - *Risk*: Does the $10\%$ cap evaluation round to cents prior to comparison or during subtraction? In reference implementations, full precision is kept, but a tester rounding the cap first might see a $\$0.01$ discrepancy.
3. **Tax Bracket Boundary Definitions**:
   - *Observation*: Federal brackets specify "$15.00 to $20.00", "$20.01 to $40.00", "$40.01 to $50.00".
   - *Risk*: If an hourly rate falls between $20.00$ and $20.01$ (e.g., $\$20.005$), the bracket is technically undefined in prose, although standard financial inputs use 2 decimals.

---

### 2.3 Enhanced Package (`enhanced_package_spec.md`)
1. **Rule Precedence and Short-Circuit Validation**:
   - *Observation*: Section 4 specifies validation rules (N4, N5, N7, N8). 
   - *Risk*: Rule N4 requires signature 1 or 2 for business locations (`signature != 0`), while Rule 10 introduces a surcharge for unsigned dangerous goods (`signature = 0`). If a test vector specifies business location (`locationType = 0`), dangerous goods (`dangerous = 1`), and `signature = 0`, Rule N4 mandates rejection (`ERROR`), whereas a tester focusing only on Rule 10 might attempt to calculate an output with the $+\$40$ fee. The order of operations in Section 5 clarifies that validation precedes price computation.
2. **Dimension Multipliers Order of Operations**:
   - *Observation*: Packaging codes 7 and 8 apply multiplicative adjustments ($1.15\times$ and $1.30\times$).
   - *Risk*: Section 5.3 specifies these multipliers apply after delivery option scaling and before additive adjustments. If a tester incorrectly applies packaging multipliers to the total price *after* additive adjustments, the resulting price will be significantly larger. Section 5 explicitly calibrates this sequence.
3. **Local Delivery Discount Order**:
   - *Observation*: Rule 1 grants a $10\%$ discount on the running total when `origin == destination` and `deliveryOption` is 1, 2, or 3.
   - *Risk*: The spec explicitly states in Section 5.5 that this discount applies to the total running price (including additive surcharges). A tester might intuitively apply the discount only to the base freight cost rather than the entire invoice.

---

### 2.4 Enhanced Payroll (`enhanced_payroll_spec.md`)
1. **Bonus Taxability vs. Separate Stipend**:
   - *Observation*: Rule 2 ($+\$50$ for overtime $> 5.0$ hrs) and Rule 3 ($+\$75$ for holiday $> 5.0$ hrs) add bonuses to gross pay.
   - *Risk*: In payroll systems, bonuses are sometimes taxed at separate supplemental flat rates or excluded from state withholdings. Section 5.1 explicitly confirms bonuses are included in `grossPay`, which forms the basis for all federal, FICA, and state tax calculations.
2. **Interaction of Entry-Wage Subsidy (Rule 6) and Wellness Credit (Rule 7)**:
   - *Observation*: Rule 6 zeros medical, dental, and vision deductions when $\text{hourly} \le \$20.00$. Rule 7 grants a $+\$15.00$ net credit if enrolled in medical 3, dental 2, vision 2.
   - *Risk*: A tester might assume that since the employee pays $\$0.00$ in deductions under Rule 6, they are ineligible for the wellness credit. The specification treats them as independent clauses: Rule 6 affects deductions; Rule 7 awards credit based strictly on enrollment codes.
3. **Strict Inequality on Overtime/Holiday Thresholds**:
   - *Observation*: Rules 2 and 3 state "strictly exceed 5.0 hours (`overtimeHours > 5.0`)".
   - *Risk*: Testers frequently confuse $>$ with $\ge$. Exactly $5.0$ hours receives no bonus; $5.1$ hours receives the bonus.
4. **State Tax Threshold Discontinuities**:
   - *Observation*: Tennessee tax has $0\%$ up to $\$20.00$, $4\%$ for $\$20.01$ to $\$35.00$, and $8\%$ above $\$35.00$.
   - *Risk*: State tax is assessed on the entire gross pay using a marginal bracket determined by the hourly wage rate, rather than progressive brackets on gross salary chunks. The spec's formulas clarify this directly.

---

## 3. Recommendations for Benchmark Protocol
1. **Pre-computation Clarification**: In high-detail LLM prompt templates, emphasizing that intermediate steps preserve full decimal precision prevents spurious cent-rounding drift.
2. **Strict Domain Checks**: Rejection behavior (`ERROR`) on out-of-domain values should be treated as a strict primary oracle check.

