package com.electricity.calculator;

/**
 * Interface defining the contract for electricity billing calculators.
 */
public interface BillCalculator {
    /**
     * Calculates the total amount for the given units consumed.
     * @param units the number of units consumed
     * @return the calculated bill amount
     */
    double calculateBill(double units);

    /**
     * Returns a string breakdown of how the bill was calculated based on slabs.
     * @param units the number of units consumed
     * @return HTML or formatted text breakdown
     */
    String getSlabBreakdown(double units);
}
