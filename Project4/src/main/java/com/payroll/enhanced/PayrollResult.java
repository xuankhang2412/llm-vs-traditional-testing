package com.payroll.enhanced;

import java.math.BigDecimal;

/**
 * Immutable result of an enhanced payroll calculation.
 *
 * @param grossPay weekly gross pay, rounded to 2 decimal places (HALF_UP)
 * @param netPay   weekly net pay after all taxes, deductions, and credits, rounded to 2 decimal places (HALF_UP)
 */
public record PayrollResult(BigDecimal grossPay, BigDecimal netPay) {
}

