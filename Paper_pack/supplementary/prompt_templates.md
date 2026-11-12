# LLM Prompt Templates (C1 through C6)

This document provides the full, verbatim prompt templates used for automated test case generation.

## Template: `C1_low_detail_zero_shot.txt`

```
Please read the following specification carefully:

{SPEC}

Generate {N} test cases based on the specification above.
Output the test cases strictly as CSV data.
```

---

## Template: `C2_low_detail_one_shot.txt`

```
Please read the following specification carefully:

{SPEC}

Here is an example test case:
{EXAMPLES}

Generate {N} test cases based on the specification above.
Output the test cases strictly as CSV data.
```

---

## Template: `C3_low_detail_few_shot.txt`

```
Please read the following specification carefully:

{SPEC}

Here are example test cases:
{EXAMPLES}

Generate {N} test cases based on the specification above.
Output the test cases strictly as CSV data.
```

---

## Template: `C4_high_detail_zero_shot.txt`

```
Please read the following specification carefully:

{SPEC}

INSTRUCTIONS:
1. Generate exactly {N} test cases based on the specification above.
2. Format your output strictly as a CSV document with the exact column header defined in the specification.
3. Every input variable must strictly adhere to the defined units, numeric encodings, and types:
   - Floating-point numbers must use standard decimal notation.
   - Categorical and binary flags must use their assigned integer codes (e.g., 0/1 for binary flags, discrete integers for tiered options).
4. For every test case, you must compute the final expected value step by step by hand following the specified order of operations before writing it:
   - If the input vector violates any domain constraint, input range, or validation rule, set the `expected` column value to the literal text ERROR.
   - If the input is valid, calculate the exact numeric result and format it rounded to exactly 2 decimal places using half-up rounding (e.g. 131.40).
5. Output only valid CSV rows starting with the header line, with no extra conversational commentary or explanation.
```

---

## Template: `C5_high_detail_one_shot.txt`

```
Please read the following specification carefully:

{SPEC}

Here is an example test case:
{EXAMPLES}

INSTRUCTIONS:
1. Generate exactly {N} test cases based on the specification above.
2. Format your output strictly as a CSV document with the exact column header defined in the specification.
3. Every input variable must strictly adhere to the defined units, numeric encodings, and types:
   - Floating-point numbers must use standard decimal notation.
   - Categorical and binary flags must use their assigned integer codes (e.g., 0/1 for binary flags, discrete integers for tiered options).
4. For every test case, you must compute the final expected value step by step by hand following the specified order of operations before writing it:
   - If the input vector violates any domain constraint, input range, or validation rule, set the `expected` column value to the literal text ERROR.
   - If the input is valid, calculate the exact numeric result and format it rounded to exactly 2 decimal places using half-up rounding (e.g. 131.40).
5. Output only valid CSV rows starting with the header line, with no extra conversational commentary or explanation.
```

---

## Template: `C6_high_detail_few_shot.txt`

```
Please read the following specification carefully:

{SPEC}

Here are example test cases:
{EXAMPLES}

INSTRUCTIONS:
1. Generate exactly {N} test cases based on the specification above.
2. Format your output strictly as a CSV document with the exact column header defined in the specification.
3. Every input variable must strictly adhere to the defined units, numeric encodings, and types:
   - Floating-point numbers must use standard decimal notation.
   - Categorical and binary flags must use their assigned integer codes (e.g., 0/1 for binary flags, discrete integers for tiered options).
4. For every test case, you must compute the final expected value step by step by hand following the specified order of operations before writing it:
   - If the input vector violates any domain constraint, input range, or validation rule, set the `expected` column value to the literal text ERROR.
   - If the input is valid, calculate the exact numeric result and format it rounded to exactly 2 decimal places using half-up rounding (e.g. 131.40).
5. Output only valid CSV rows starting with the header line, with no extra conversational commentary or explanation.
```

---

