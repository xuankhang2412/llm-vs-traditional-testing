# Project 3: Enhanced Package Shipping

Deterministic, rule-based Java application calculating package shipping costs based on weight, origin/destination zones, delivery speed options, packaging tiers, and special service surcharges. Testbed application for SWT301 (Software Testing) benchmarking test-case generation with 20 seeded defects (D1–D20).

- **Language:** Java 17
- **Build Tool:** Maven
- **Testing:** JUnit 5 (JUnit Jupiter 5.10.2)
- **Package:** `com.shipping.enhanced`

---

## 1. Overview & How to Run

### Build and Run Tests
```powershell
mvn test -f "D:\SWT301_TEST\Project3\pom.xml"
```

### CLI Execution
```powershell
# Compile
mvn compile -f "D:\SWT301_TEST\Project3\pom.xml"

# Run with clean baseline (Defect.NONE)
java -cp "D:\SWT301_TEST\Project3\target\classes" com.shipping.enhanced.Main 35.0 3 3 2 4 1 0 1 0 0 0

# Run with defect D1 injected
java -Ddefect=D1 -cp "D:\SWT301_TEST\Project3\target\classes" com.shipping.enhanced.Main 35.0 3 3 2 4 1 0 1 0 0 0
```

### Defect Runner Execution
```powershell
java -cp "D:\SWT301_TEST\Project3\target\classes" com.shipping.enhanced.DefectRunner "D:\SWT301_TEST\Project3\testcases\sample_shipping_tests.csv" "D:\SWT301_TEST\Project3\results\defect_matrix.csv"
```

---

## 2. Inputs Specification

| # | Field | Range | Description |
|---|---|---|---|
| 1 | `weight` | `0 < w <= 60` | Package weight in pounds |
| 2 | `origin` | `1–4` | Origin domestic zone |
| 3 | `destination` | `1–4` | Destination domestic zone |
| 4 | `deliveryOption` | `1–7` | 1=Same Day, 2=First Overnight, 3=Priority Overnight, 4=Standard Overnight, 5=2-day AM, 6=2-day, 7=Express Saver |
| 5 | `packaging` | `1–8` | 1=Envelope, 2=Small Box, 3=Medium Box, 4=Large Box, 5=Tube, 6=Customer <=30in, 7=Customer >30-60in, 8=Customer >60-100in |
| 6 | `signature` | `0–2` | 0=None, 1=Indirect ($3), 2=Direct ($10) |
| 7 | `locationType` | `0–1` | 0=Business, 1=Residence ($10) |
| 8 | `saturday` | `0–1` | 0=No, 1=Yes (+$20, or +$10 under N9) |
| 9 | `hold` | `0–1` | 0=No, 1=Yes (+$15, waived under N3) |
| 10 | `fragile` | `0–1` | 0=No, 1=Yes (+$25) |
| 11 | `dangerous` | `0–1` | 0=No, 1=Yes (+$50) |

---

## 3. Rate Table & Multipliers

### Base Rate per lb (Symmetric)
| Zone Pair | Rate ($/lb) |
|---|---|
| Same Zone (1-1, 2-2, 3-3, 4-4) | $2.00 |
| 1 ↔ 2 | $2.50 |
| 1 ↔ 3 | $3.00 |
| 1 ↔ 4 | $3.75 |
| 2 ↔ 3 | $2.75 |
| 2 ↔ 4 | $3.25 |
| 3 ↔ 4 | $2.30 |

### Delivery Multipliers
| Option | Name | Multiplier |
|---|---|---|
| 1 | Same Day | x2.1 (or x2.75 with fragile under N2) |
| 2 | First Overnight | x1.5 |
| 3 | Priority Overnight | x1.2 |
| 4 | Standard Overnight | x1.0 |
| 5 | 2-day AM | x0.9 |
| 6 | 2-day | x0.8 |
| 7 | Express Saver | x0.6 |

### Packaging Types & Weight Limits
| Code | Packaging Type | Weight Limit | Fee / Multiplier |
|---|---|---|---|
| 1 | Envelope | w <= 2 lbs | +$0 |
| 2 | Small Box | w <= 10 lbs | +$0 |
| 3 | Medium Box | w <= 25 lbs | +$7 |
| 4 | Large Box | w <= 40 lbs | +$18 |
| 5 | Tube | w <= 10 lbs | +$5 |
| 6 | Customer Packaged (<=30 in) | w <= 60 lbs | +$0 |
| 7 | Customer Packaged (>30 & <=60 in) | w <= 60 lbs | x1.15 |
| 8 | Customer Packaged (>60 & <=100 in) | w <= 60 lbs | x1.30 |

---

## 4. Business Rules & Notes

- **N1**: `origin == destination` and `deliveryOption` in `{1, 2, 3}` -> 10% discount on total running price (multiply by 0.90).
- **N2**: `fragile == 1` and `deliveryOption == 1` -> delivery multiplier is x2.75 instead of x2.1.
- **N3**: `deliveryOption == 7` (Express Saver) with `hold == 1` -> hold fee is waived ($0 instead of $15).
- **N4**: Business location (`locationType == 0`) requires signature 1 or 2 (reject signature 0).
- **N5**: Package `weight > 50` requires signature 1 or 2 (reject signature 0).
- **N6**: `fragile == 1` and direct signature (`signature == 2`) -> extra +$30 fee.
- **N7**: `fragile == 1` and `dangerous == 1` together -> reject shipment.
- **N8**: `dangerous == 1` requires `deliveryOption` in `{1, 2, 3, 4}` (reject 5, 6, 7).
- **N9**: `locationType == 1` (Residence) and destination zone 4 and `saturday == 1` -> Saturday fee is +$10 instead of +$20.
- **N10**: `dangerous == 1` with `signature == 0` -> extra +$40 fee.

---

## 5. Order of Operations & Calibration Test

### Order of Operations:
1. `base = baseRate(origin, destination) * weight`
2. `price = base * deliveryMultiplier(N2)`
3. Packaging: Additive (+$0, +$7, +$18, +$5) or Multiplier on current price for codes 7/8 (x1.15, x1.30)
4. Additive adjustments:
   - Signature: Indirect +$3, Direct +$10
   - Residence: +$10
   - Saturday: +$20 (or +$10 under N9)
   - Hold: +$15 (or +$0 under N3)
   - Fragile: +$25
   - Dangerous: +$50
   - N6 adjustment: +$30 (fragile + direct signature)
   - N10 adjustment: +$40 (dangerous + signature 0)
5. N1 discount on total running price: 10% discount if `origin == destination` and `deliveryOption` in `{1, 2, 3}`.
6. Final rounding: `BigDecimal` rounded to 2 decimal places using `RoundingMode.HALF_UP`.

### Mandatory Calibration Test (from source paper):
`weight=35, origin=3, destination=3, deliveryOption=2, packaging=4, signature=1, locationType=0, saturday=1, hold=0, fragile=0, dangerous=0 -> $131.40`
- Base: 2.00 * 35 = 70.00
- Delivery: 70.00 * 1.5 = 105.00
- Packaging 4 (Large Box): 105.00 + 18.00 = 123.00
- Additive: Indirect signature (+3.00), Saturday (+20.00) = 146.00
- N1 discount: 146.00 * 0.90 = $131.40 (Passes exactly).

---

## 6. Assumptions

1. **Operation Order**: Base rate * weight, multiplied by delivery multiplier (incorporating N2), adjusted by packaging (additive or multiplier), additive surcharges (signature, residence, Saturday N9, hold N3, fragile, dangerous, N6, N10), and finally N1 discount applied to the total running price.
2. **Rate Symmetry**: Rates between origin and destination are symmetric (`rate(A, B) == rate(B, A)`), except as specifically modified by Defect D19.
3. **Packaging Dimensions**: Codes 6–8 encode dimension tiers directly without explicit dimension measurement inputs.
4. **Signature Requirements**: N4 requires signature 1 or 2 for business; N5 requires signature 1 or 2 for packages weighing more than 50 lbs.
5. **Rounding**: All monetary calculations use `BigDecimal` with final result rounded HALF_UP to 2 decimal places.

---

## 7. Defect Catalogue (20 Seeded Defects)

| ID | Description | Type | Note | class.method | Exposing Input (NONE vs Dk) |
|---|---|---|---|---|---|
| **D1** | Zone 3-4 rate 2.30 -> 2.50 | arithmetic | - | `ShippingCalculator.getBaseRate` | `w=10 o=3 d=4 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$26.00, D1=$28.00) |
| **D2** | Priority Overnight multiplier 1.2 -> 1.5 | arithmetic | - | `ShippingCalculator.getDeliveryMultiplier` | `w=10 o=1 d=2 del=3 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$33.00, D2=$40.50) |
| **D3** | N1 no longer applies to Same Day (option 1) | conditional | N1 | `ShippingCalculator.calculate` | `w=10 o=2 d=2 del=1 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$40.50, D3=$45.00) |
| **D4** | Small Box limit w<=10 becomes w<10 | boundary | - | `ShippingCalculator.calculate` | `w=10.0 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D4=Reject) |
| **D5** | Indirect signature fee 3 -> 5 | arithmetic | - | `ShippingCalculator.calculate` | `w=5 o=1 d=2 del=4 pkg=6 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$15.50, D5=$17.50) |
| **D6** | N4: direct signature no longer accepted for business | conditional | N4 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=2 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D6=Reject) |
| **D7** | N6 applies to indirect signature instead of direct | conditional | N6 | `ShippingCalculator.calculate` | `w=5 o=1 d=2 del=4 pkg=2 sig=2 loc=0 sat=0 hold=0 frag=1 dang=0` (NONE=$77.50, D7=$47.50) |
| **D8** | Hold-at-location fee never charged | logic | - | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=1 frag=0 dang=0` (NONE=$43.00, D8=$28.00) |
| **D9** | N7 removed (fragile+dangerous allowed) | conditional | N7 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=1 dang=1` (NONE=Reject, D9=$103.00) |
| **D10** | N10 removed | conditional | N10 | `ShippingCalculator.calculate` | `w=20 o=1 d=2 del=4 pkg=3 sig=0 loc=1 sat=0 hold=0 frag=0 dang=1` (NONE=$157.00, D10=$117.00) |
| **D11** | N5 threshold weight>50 becomes weight>55 | boundary | N5 | `ShippingCalculator.calculate` | `w=55.0 o=1 d=2 del=4 pkg=8 sig=0 loc=1 sat=0 hold=0 frag=0 dang=0` (NONE=Reject, D11=$188.75) |
| **D12** | Envelope limit w<=2 becomes w<2 | boundary | - | `ShippingCalculator.calculate` | `w=2.0 o=1 d=1 del=4 pkg=1 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D12=Reject) |
| **D13** | Packaging code 7 multiplier 1.15 -> 1.25 | arithmetic | - | `ShippingCalculator.applyPackaging` | `w=20 o=1 d=3 del=4 pkg=7 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$72.00, D13=$78.00) |
| **D14** | Medium Box limit w<=25 becomes w<25 | boundary | - | `ShippingCalculator.calculate` | `w=25.0 o=1 d=2 del=4 pkg=3 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=Pass, D14=Reject) |
| **D15** | N9 applies to destination zone 3 instead of 4 | conditional | N9 | `ShippingCalculator.calculate` | `w=10 o=1 d=3 del=4 pkg=2 sig=1 loc=1 sat=1 hold=0 frag=0 dang=0` (NONE=$63.00, D15=$53.00) |
| **D16** | N1 discount 10% -> 5% | arithmetic | N1 | `ShippingCalculator.calculate` | `w=10 o=2 d=2 del=2 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$29.70, D16=$31.35) |
| **D17** | N3 exception removed (hold fee charged for option 7) | conditional | N3 | `ShippingCalculator.calculate` | `w=8 o=2 d=3 del=7 pkg=2 sig=1 loc=0 sat=0 hold=1 frag=0 dang=0` (NONE=$16.20, D17=$31.20) |
| **D18** | N8 allows deliveryOption 5 | conditional | N8 | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=5 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=1` (NONE=Reject, D18=$75.50) |
| **D19** | Rate for pair (4,2) falls back to 2.00 while (2,4) stays correct | logic | - | `ShippingCalculator.getBaseRate` | `w=10 o=4 d=2 del=4 pkg=2 sig=1 loc=0 sat=0 hold=0 frag=0 dang=0` (NONE=$35.50, D19=$23.00) |
| **D20** | Residence fee not charged when Saturday=1 | logic | - | `ShippingCalculator.calculate` | `w=10 o=1 d=2 del=4 pkg=2 sig=1 loc=1 sat=1 hold=0 frag=0 dang=0` (NONE=$58.00, D20=$48.00) |

---

## 8. Defect Type Distribution

| Defect Type | Count | Percentage | Defect IDs |
|---|---|---|---|
| **conditional** | 8 | 40.0% | D3, D6, D7, D9, D10, D15, D17, D18 |
| **arithmetic** | 5 | 25.0% | D1, D2, D5, D13, D16 |
| **boundary** | 4 | 20.0% | D4, D11, D12, D14 |
| **logic** | 3 | 15.0% | D8, D19, D20 |
| **Total** | **20** | **100.0%** | D1–D20 |

