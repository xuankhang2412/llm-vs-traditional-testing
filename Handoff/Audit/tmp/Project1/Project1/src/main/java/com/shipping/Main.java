package com.shipping;

public class Main {
    public static void main(String[] args) {
        String defectProp = System.getProperty("defect", "NONE").trim();
        PackageDefect defect;
        try {
            defect = PackageDefect.valueOf(defectProp.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown defect: " + defectProp + ". Defaulting to NONE.");
            defect = PackageDefect.NONE;
        }

        if (args.length < 10) {
            System.out.println("Package Shipping Calculator (Active Defect: " + defect + ")");
            System.out.println("Usage: java [-Ddefect=D1..D10] -cp <classpath> com.shipping.Main <weight> <origin> <dest> <deliveryOpt> <sigOpt> <locType> <sat> <hold> <fragile> <dangerous>");
            System.out.println("Arguments:");
            System.out.println("  weight              : (double) 0 < weight <= 60 lbs");
            System.out.println("  origin, dest        : (int) domestic zones 1..4");
            System.out.println("  deliveryOption      : (int) 1=SameDay, 2=FirstOvernight, 3=PriorityOvernight, 4=StandardOvernight, 5=2dayAM, 6=2day, 7=ExpressSaver");
            System.out.println("  signatureOption     : (int) 0=None, 1=Indirect, 2=Direct");
            System.out.println("  deliveryLocationType: (int) 0=Business, 1=Residence");
            System.out.println("  saturdayDelivery    : (int) 0 or 1");
            System.out.println("  holdAtLocation      : (int) 0 or 1");
            System.out.println("  fragile             : (int) 0 or 1");
            System.out.println("  dangerous           : (int) 0 or 1");
            return;
        }

        try {
            double weight = Double.parseDouble(args[0]);
            int origin = Integer.parseInt(args[1]);
            int destination = Integer.parseInt(args[2]);
            int deliveryOption = Integer.parseInt(args[3]);
            int signatureOption = Integer.parseInt(args[4]);
            int deliveryLocationType = Integer.parseInt(args[5]);
            int saturdayDelivery = Integer.parseInt(args[6]);
            int holdAtLocation = Integer.parseInt(args[7]);
            int fragile = Integer.parseInt(args[8]);
            int dangerous = Integer.parseInt(args[9]);

            PackageShippingInput input = new PackageShippingInput(
                    weight, origin, destination, deliveryOption, signatureOption,
                    deliveryLocationType, saturdayDelivery, holdAtLocation, fragile, dangerous);

            ShippingCalculator calculator = new ShippingCalculator(defect);
            ShippingResult result = calculator.calculate(input);

            System.out.println("==========================================");
            System.out.println("Package Shipping Calculation Summary");
            System.out.println("Active Defect: " + defect);
            System.out.println("==========================================");
            System.out.println("Weight: " + weight + " lbs");
            System.out.println("Origin Zone: " + origin + ", Destination Zone: " + destination);
            System.out.println("Base Rate per lb: $" + result.getBaseRate());
            System.out.println("Base Cost: $" + result.getBaseCost());
            System.out.println("Delivery Option Multiplier: x" + result.getDeliveryMultiplier());
            System.out.println("Multiplied Cost: $" + result.getMultipliedCost());
            System.out.println("Signature Surcharge: $" + result.getSignatureSurcharge());
            System.out.println("Residential Surcharge: $" + result.getResidentialSurcharge());
            System.out.println("Saturday Surcharge: $" + result.getSaturdaySurcharge());
            System.out.println("Hold At Location Surcharge: $" + result.getHoldSurcharge());
            System.out.println("Fragile Surcharge: $" + result.getFragileSurcharge());
            System.out.println("Dangerous Surcharge: $" + result.getDangerousSurcharge());
            System.out.println("------------------------------------------");
            System.out.println("Total Price: $" + result.getTotalCost());
            System.out.println("==========================================");
        } catch (IllegalArgumentException e) {
            System.err.println("Validation / Calculation Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
