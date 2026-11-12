package com.shipping.enhanced;

import java.math.BigDecimal;

/**
 * Immutable result of an enhanced shipping-cost calculation.
 *
 * @param totalCost the calculated total shipping cost rounded to 2 decimal places (HALF_UP)
 */
public record ShippingResult(BigDecimal totalCost) {
}

