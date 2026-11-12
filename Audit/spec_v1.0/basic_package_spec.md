# Basic Package Shipping Specification

## 1. Overview
The Basic Package Shipping system calculates shipping costs for domestic parcel deliveries based on parcel weight, origin and destination zones, service speed options, and optional value-added surcharges.

## 2. Input Parameters

| Field Name | Type | Valid Domain | Description |
|---|---|---|---|
| `weight` | Floating-point | `0.0 < weight <= 60.0` | Parcel weight in pounds (lbs) |
| `origin` | Integer | `1` to `4` | Origin domestic zone |
| `destination` | Integer | `1` to `4` | Destination domestic zone |
| `deliveryOption` | Integer | `1` to `7` | Service delivery speed code |
| `signatureOption` | Integer | `0` to `2` | Signature requirement code |
| `deliveryLocationType` | Integer | `0` or `1` | Delivery destination location type |
| `saturdayDelivery` | Integer | `0` or `1` | Saturday delivery service flag |
| `holdAtLocation` | Integer | `0` or `1` | Hold at courier facility flag |
| `fragile` | Integer | `0` or `1` | Fragile package handling flag |
| `dangerous` | Integer | `0` or `1` | Hazardous / dangerous goods flag |

Any input values outside the specified ranges must result in an error rejection (`ERROR`).

## 3. Code Tables & Rate Structures

### 3.1 Base Rate Table (per pound, symmetric)
Shipping rates depend on origin and destination zone pairs. Rates are symmetric between zones (rate from zone A to B equals the rate from B to A):

| Zone Pair | Rate per Pound ($/lb) |
|---|---|
| Same Zone (1-1, 2-2, 3-3, 4-4) | $2.00 |
| Zones 1 and 2 | $2.50 |
| Zones 1 and 3 | $3.00 |
| Zones 1 and 4 | $3.75 |
| Zones 2 and 3 | $2.75 |
| Zones 2 and 4 | $3.25 |
| Zones 3 and 4 | $2.30 |

### 3.2 Delivery Speed Multipliers

| Code | Delivery Option Name | Multiplier |
|---|---|---|
| `1` | Same Day | 2.1 |
| `2` | First Overnight | 1.5 |
| `3` | Priority Overnight | 1.2 |
| `4` | Standard Overnight | 1.0 |
| `5` | 2-day AM | 0.9 |
| `6` | 2-day | 0.8 |
| `7` | Express Saver | 0.6 |

### 3.3 Service Surcharges

| Service | Condition / Code | Fee ($) |
|---|---|---|
| Signature | `signatureOption = 0` (None) | $0.00 |
| Signature | `signatureOption = 1` (Indirect) | $3.00 |
| Signature | `signatureOption = 2` (Direct) | $10.00 |
| Location Type | `deliveryLocationType = 0` (Business) | $0.00 |
| Location Type | `deliveryLocationType = 1` (Residence) | $10.00 |
| Saturday Delivery | `saturdayDelivery = 1` | $20.00 |
| Hold at Location | `holdAtLocation = 1` | $15.00 |
| Fragile Handling | `fragile = 1` | $25.00 |
| Dangerous Goods | `dangerous = 1` | $50.00 |

## 4. Calculation Procedure & Order of Operations

1. **Base Cost Calculation**:
   Look up the base rate per pound for the origin and destination zone pair.
   $$\text{baseCost} = \text{baseRate} \times \text{weight}$$

2. **Delivery Speed Adjustment**:
   Multiply base cost by the service delivery multiplier:
   $$\text{multipliedCost} = \text{baseCost} \times \text{deliveryMultiplier}$$

3. **Additive Surcharges**:
   Add all applicable service surcharges:
   $$\text{totalCost} = \text{multipliedCost} + \text{signatureSurcharge} + \text{residentialSurcharge} + \text{saturdaySurcharge} + \text{holdSurcharge} + \text{fragileSurcharge} + \text{dangerousSurcharge}$$

4. **Rounding Policy**:
   The final total cost is rounded to exactly two decimal places using standard half-up rounding.
