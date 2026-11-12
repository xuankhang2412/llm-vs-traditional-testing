package com.shipping.enhanced;

/**
 * Value object holding all inputs for an enhanced shipping-cost calculation.
 *
 * @param weight         Package weight in lbs (0 < weight <= 60)
 * @param origin         Origin zone (1-4)
 * @param destination    Destination zone (1-4)
 * @param deliveryOption 1=Same Day, 2=First Overnight, 3=Priority Overnight,
 *                       4=Standard Overnight, 5=2-day AM, 6=2-day, 7=Express Saver
 * @param packaging      1=Envelope, 2=Small Box, 3=Medium Box, 4=Large Box,
 *                       5=Tube, 6=Customer <=30in, 7=Customer >30-60in, 8=Customer >60-100in
 * @param signature      0=None, 1=Indirect, 2=Direct
 * @param locationType   0=Business, 1=Residence
 * @param saturday       0=No, 1=Yes
 * @param hold           0=No, 1=Yes (Hold at location)
 * @param fragile        0=No, 1=Yes
 * @param dangerous      0=No, 1=Yes
 */
public record ShippingInput(
        double weight,
        int origin,
        int destination,
        int deliveryOption,
        int packaging,
        int signature,
        int locationType,
        int saturday,
        int hold,
        int fragile,
        int dangerous) {
}

