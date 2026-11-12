package com.shipping.enhanced;

/**
 * CLI entry point for Enhanced Package Shipping Calculator.
 * Reads eleven positional arguments:
 *   weight origin destination deliveryOption packaging signature locationType saturday hold fragile dangerous
 *
 * Inject a defect with  -Ddefect=D1  (etc.)
 */
public class Main {

    public static void main(String[] args) {
        String defectProp = System.getProperty("defect", "NONE").trim();
        Defect defect = Defect.fromSystemProperty();

        if (args.length < 11) {
            System.out.println("Enhanced Package Shipping Calculator (Active Defect: " + defect + ")");
            System.out.println("Usage: java [-Ddefect=D1..D20] -cp <classpath> com.shipping.enhanced.Main "
                    + "<weight> <origin> <dest> <deliveryOpt> <packaging> <signature> <locType> <saturday> <hold> <fragile> <dangerous>");
            System.out.println("Arguments:");
            System.out.println("  weight         : (double) 0 < weight <= 60 lbs");
            System.out.println("  origin, dest   : (int) domestic zones 1-4");
            System.out.println("  deliveryOption : (int) 1=SameDay, 2=FirstOvernight, 3=PriorityOvernight, 4=StdOvernight, 5=2dayAM, 6=2day, 7=ExpressSaver");
            System.out.println("  packaging      : (int) 1=Envelope, 2=SmallBox, 3=MediumBox, 4=LargeBox, 5=Tube, 6=Customer<=30in, 7=Customer>30-60in, 8=Customer>60-100in");
            System.out.println("  signature      : (int) 0=None, 1=Indirect, 2=Direct");
            System.out.println("  locationType   : (int) 0=Business, 1=Residence");
            System.out.println("  saturday       : (int) 0 or 1");
            System.out.println("  hold           : (int) 0 or 1");
            System.out.println("  fragile        : (int) 0 or 1");
            System.out.println("  dangerous      : (int) 0 or 1");
            return;
        }

        try {
            double weight = Double.parseDouble(args[0]);
            int origin = Integer.parseInt(args[1]);
            int destination = Integer.parseInt(args[2]);
            int deliveryOption = Integer.parseInt(args[3]);
            int packaging = Integer.parseInt(args[4]);
            int signature = Integer.parseInt(args[5]);
            int locationType = Integer.parseInt(args[6]);
            int saturday = Integer.parseInt(args[7]);
            int hold = Integer.parseInt(args[8]);
            int fragile = Integer.parseInt(args[9]);
            int dangerous = Integer.parseInt(args[10]);

            ShippingInput input = new ShippingInput(
                    weight, origin, destination, deliveryOption, packaging,
                    signature, locationType, saturday, hold, fragile, dangerous);

            ShippingCalculator calculator = new ShippingCalculator(defect);
            ShippingResult result = calculator.calculate(input);

            System.out.println("==========================================");
            System.out.println("Enhanced Package Shipping Calculation Summary");
            System.out.println("Active Defect: " + defect);
            System.out.println("==========================================");
            System.out.println("Total Price: $" + result.totalCost());
            System.out.println("==========================================");
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

