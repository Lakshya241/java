package com.electricity.calculator;

/**
 * Concrete implementation of BillCalculator based on progressive slab rates.
 */
public class SlabBillCalculator implements BillCalculator {
    public static final double SLAB_1_LIMIT = 100;
    public static final double SLAB_2_LIMIT = 300;
    public static final double SLAB_3_LIMIT = 500;

    public static final double RATE_SLAB_1 = 3.00;
    public static final double RATE_SLAB_2 = 4.50;
    public static final double RATE_SLAB_3 = 6.00;
    public static final double RATE_SLAB_4 = 8.00;

    public static final double FIXED_CHARGE = 15.00;
    public static final double TAX_RATE = 0.10; // 10% surcharge/tax

    @Override
    public double calculateBill(double units) {
        if (units < 0) return 0;

        double energyCharge = 0;
        double remainingUnits = units;

        // Slab 1: 0 - 100
        if (remainingUnits > SLAB_1_LIMIT) {
            energyCharge += SLAB_1_LIMIT * RATE_SLAB_1;
            remainingUnits -= SLAB_1_LIMIT;
        } else {
            energyCharge += remainingUnits * RATE_SLAB_1;
            remainingUnits = 0;
        }

        // Slab 2: 101 - 300
        double slab2Range = SLAB_2_LIMIT - SLAB_1_LIMIT;
        if (remainingUnits > slab2Range) {
            energyCharge += slab2Range * RATE_SLAB_2;
            remainingUnits -= slab2Range;
        } else {
            energyCharge += remainingUnits * RATE_SLAB_2;
            remainingUnits = 0;
        }

        // Slab 3: 301 - 500
        double slab3Range = SLAB_3_LIMIT - SLAB_2_LIMIT;
        if (remainingUnits > slab3Range) {
            energyCharge += slab3Range * RATE_SLAB_3;
            remainingUnits -= slab3Range;
        } else {
            energyCharge += remainingUnits * RATE_SLAB_3;
            remainingUnits = 0;
        }

        // Slab 4: Above 500
        if (remainingUnits > 0) {
            energyCharge += remainingUnits * RATE_SLAB_4;
        }

        double subtotal = energyCharge + FIXED_CHARGE;
        double tax = subtotal * TAX_RATE;
        return subtotal + tax;
    }

    @Override
    public String getSlabBreakdown(double units) {
        if (units < 0) return "Invalid units";

        StringBuilder sb = new StringBuilder();
        double energyCharge = 0;

        sb.append("<html><body style='font-family: sans-serif; font-size: 11px; line-height: 1.4; margin: 5px;'>");
        sb.append("<b style='font-size: 12px; color: #2196F3;'>Slab-wise Calculation Breakdown:</b><br/>");
        sb.append("<table border='0' cellspacing='4' cellpadding='0' width='100%'>");

        // Slab 1
        double unitsSlab1 = Math.min(units, SLAB_1_LIMIT);
        double chargeSlab1 = unitsSlab1 * RATE_SLAB_1;
        energyCharge += chargeSlab1;
        sb.append(String.format("<tr><td>Slab 1 (0-100 u):</td><td align='right'>%.2f u @ $%.2f =</td><td align='right'>$%.2f</td></tr>", unitsSlab1, RATE_SLAB_1, chargeSlab1));

        // Slab 2
        if (units > SLAB_1_LIMIT) {
            double unitsSlab2 = Math.min(units - SLAB_1_LIMIT, SLAB_2_LIMIT - SLAB_1_LIMIT);
            double chargeSlab2 = unitsSlab2 * RATE_SLAB_2;
            energyCharge += chargeSlab2;
            sb.append(String.format("<tr><td>Slab 2 (101-300 u):</td><td align='right'>%.2f u @ $%.2f =</td><td align='right'>$%.2f</td></tr>", unitsSlab2, RATE_SLAB_2, chargeSlab2));
        }

        // Slab 3
        if (units > SLAB_2_LIMIT) {
            double unitsSlab3 = Math.min(units - SLAB_2_LIMIT, SLAB_3_LIMIT - SLAB_2_LIMIT);
            double chargeSlab3 = unitsSlab3 * RATE_SLAB_3;
            energyCharge += chargeSlab3;
            sb.append(String.format("<tr><td>Slab 3 (301-500 u):</td><td align='right'>%.2f u @ $%.2f =</td><td align='right'>$%.2f</td></tr>", unitsSlab3, RATE_SLAB_3, chargeSlab3));
        }

        // Slab 4
        if (units > SLAB_3_LIMIT) {
            double unitsSlab4 = units - SLAB_3_LIMIT;
            double chargeSlab4 = unitsSlab4 * RATE_SLAB_4;
            energyCharge += chargeSlab4;
            sb.append(String.format("<tr><td>Slab 4 (&gt;500 u):</td><td align='right'>%.2f u @ $%.2f =</td><td align='right'>$%.2f</td></tr>", unitsSlab4, RATE_SLAB_4, chargeSlab4));
        }

        double subtotal = energyCharge + FIXED_CHARGE;
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        sb.append("<tr><td colspan='3'><hr style='border: 0; border-top: 1px solid #555; margin: 4px 0;'/></td></tr>");
        sb.append(String.format("<tr><td><b>Energy Charge Subtotal:</b></td><td></td><td align='right'><b>$%.2f</b></td></tr>", energyCharge));
        sb.append(String.format("<tr><td>Fixed System Charge:</td><td></td><td align='right'>$%.2f</td></tr>", FIXED_CHARGE));
        sb.append(String.format("<tr><td>Surcharge Tax (10%%):</td><td></td><td align='right'>$%.2f</td></tr>", tax));
        sb.append("<tr><td colspan='3'><hr style='border: 0; border-top: 1px solid #555; margin: 4px 0;'/></td></tr>");
        sb.append(String.format("<tr><td><b style='color: #4CAF50;'>Total Net Payable:</b></td><td></td><td align='right'><b style='color: #4CAF50;'>$%.2f</b></td></tr>", total));

        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }
}
