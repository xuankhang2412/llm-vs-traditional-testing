package com.shipping;

public final class PackageShippingInput {
    private final double weight;
    private final int origin;
    private final int destination;
    private final int deliveryOption;
    private final int signatureOption;
    private final int deliveryLocationType;
    private final int saturdayDelivery;
    private final int holdAtLocation;
    private final int fragile;
    private final int dangerous;

    public PackageShippingInput(
            double weight,
            int origin,
            int destination,
            int deliveryOption,
            int signatureOption,
            int deliveryLocationType,
            int saturdayDelivery,
            int holdAtLocation,
            int fragile,
            int dangerous) {
        this.weight = weight;
        this.origin = origin;
        this.destination = destination;
        this.deliveryOption = deliveryOption;
        this.signatureOption = signatureOption;
        this.deliveryLocationType = deliveryLocationType;
        this.saturdayDelivery = saturdayDelivery;
        this.holdAtLocation = holdAtLocation;
        this.fragile = fragile;
        this.dangerous = dangerous;
    }

    public void validate() {
        validate(PackageDefect.NONE);
    }

    public void validate(PackageDefect defect) {
        if (defect == null) {
            defect = PackageDefect.NONE;
        }

        // Weight boundary validation
        // DEFECT D2 [boundary]: Validation of weight <= 0 removed
        if (defect == PackageDefect.D2) {
            // Validation of weight <= 0 removed
        } else {
            if (weight <= 0.0) {
                throw new IllegalArgumentException("Weight must be greater than 0 lbs (got " + weight + ")");
            }
        }

        // DEFECT D1 [boundary]: Max weight limit reduced from 60 to 50 (weights in (50,60] wrongly rejected)
        double maxWeight = (defect == PackageDefect.D1) ? 50.0 : 60.0;
        if (weight > maxWeight) {
            throw new IllegalArgumentException("Weight exceeds maximum limit of " + (int) maxWeight + " lbs (got " + weight + ")");
        }

        // Zone validation: domestic zones 1..4
        if (origin < 1 || origin > 4) {
            throw new IllegalArgumentException("Origin zone must be between 1 and 4 (got " + origin + ")");
        }
        if (destination < 1 || destination > 4) {
            throw new IllegalArgumentException("Destination zone must be between 1 and 4 (got " + destination + ")");
        }

        // Delivery option validation: 1..7
        if (deliveryOption < 1 || deliveryOption > 7) {
            throw new IllegalArgumentException("Delivery option must be between 1 and 7 (got " + deliveryOption + ")");
        }

        // Signature option validation: 0..2
        if (signatureOption < 0 || signatureOption > 2) {
            throw new IllegalArgumentException("Signature option must be between 0 and 2 (got " + signatureOption + ")");
        }

        // Delivery location type validation: 0 (Business) or 1 (Residence)
        if (deliveryLocationType < 0 || deliveryLocationType > 1) {
            throw new IllegalArgumentException("Delivery location type must be 0 (Business) or 1 (Residence) (got " + deliveryLocationType + ")");
        }

        // Binary options validation: 0 or 1
        if (saturdayDelivery != 0 && saturdayDelivery != 1) {
            throw new IllegalArgumentException("Saturday delivery option must be 0 or 1 (got " + saturdayDelivery + ")");
        }
        if (holdAtLocation != 0 && holdAtLocation != 1) {
            throw new IllegalArgumentException("Hold at location option must be 0 or 1 (got " + holdAtLocation + ")");
        }
        if (fragile != 0 && fragile != 1) {
            throw new IllegalArgumentException("Fragile option must be 0 or 1 (got " + fragile + ")");
        }
        if (dangerous != 0 && dangerous != 1) {
            throw new IllegalArgumentException("Dangerous option must be 0 or 1 (got " + dangerous + ")");
        }
    }

    public double getWeight() { return weight; }
    public int getOrigin() { return origin; }
    public int getDestination() { return destination; }
    public int getDeliveryOption() { return deliveryOption; }
    public int getSignatureOption() { return signatureOption; }
    public int getDeliveryLocationType() { return deliveryLocationType; }
    public int getSaturdayDelivery() { return saturdayDelivery; }
    public int getHoldAtLocation() { return holdAtLocation; }
    public int getFragile() { return fragile; }
    public int getDangerous() { return dangerous; }
}
