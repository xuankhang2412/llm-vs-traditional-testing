package com.shipping;

import java.math.BigDecimal;

public final class ShippingResult {
    private final BigDecimal baseRate;
    private final BigDecimal baseCost;
    private final BigDecimal deliveryMultiplier;
    private final BigDecimal multipliedCost;
    private final BigDecimal signatureSurcharge;
    private final BigDecimal residentialSurcharge;
    private final BigDecimal saturdaySurcharge;
    private final BigDecimal holdSurcharge;
    private final BigDecimal fragileSurcharge;
    private final BigDecimal dangerousSurcharge;
    private final BigDecimal totalCost;

    public ShippingResult(
            BigDecimal baseRate,
            BigDecimal baseCost,
            BigDecimal deliveryMultiplier,
            BigDecimal multipliedCost,
            BigDecimal signatureSurcharge,
            BigDecimal residentialSurcharge,
            BigDecimal saturdaySurcharge,
            BigDecimal holdSurcharge,
            BigDecimal fragileSurcharge,
            BigDecimal dangerousSurcharge,
            BigDecimal totalCost) {
        this.baseRate = baseRate;
        this.baseCost = baseCost;
        this.deliveryMultiplier = deliveryMultiplier;
        this.multipliedCost = multipliedCost;
        this.signatureSurcharge = signatureSurcharge;
        this.residentialSurcharge = residentialSurcharge;
        this.saturdaySurcharge = saturdaySurcharge;
        this.holdSurcharge = holdSurcharge;
        this.fragileSurcharge = fragileSurcharge;
        this.dangerousSurcharge = dangerousSurcharge;
        this.totalCost = totalCost;
    }

    public BigDecimal getBaseRate() { return baseRate; }
    public BigDecimal getBaseCost() { return baseCost; }
    public BigDecimal getDeliveryMultiplier() { return deliveryMultiplier; }
    public BigDecimal getMultipliedCost() { return multipliedCost; }
    public BigDecimal getSignatureSurcharge() { return signatureSurcharge; }
    public BigDecimal getResidentialSurcharge() { return residentialSurcharge; }
    public BigDecimal getSaturdaySurcharge() { return saturdaySurcharge; }
    public BigDecimal getHoldSurcharge() { return holdSurcharge; }
    public BigDecimal getFragileSurcharge() { return fragileSurcharge; }
    public BigDecimal getDangerousSurcharge() { return dangerousSurcharge; }
    public BigDecimal getTotalCost() { return totalCost; }

    @Override
    public String toString() {
        return String.format(
                "ShippingResult[baseRate=%s, baseCost=%s, multiplier=%s, multipliedCost=%s, signature=%s, residential=%s, saturday=%s, hold=%s, fragile=%s, dangerous=%s, total=%s]",
                baseRate, baseCost, deliveryMultiplier, multipliedCost, signatureSurcharge, residentialSurcharge, saturdaySurcharge, holdSurcharge, fragileSurcharge, dangerousSurcharge, totalCost);
    }
}
