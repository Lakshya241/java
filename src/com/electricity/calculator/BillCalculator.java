package com.electricity.calculator;

/**
 * Interface defining the contract for electricity billing calculations.
 * 
 * OOP CONCEPT: Abstraction and Interface
 * - Defines a behavioral contract for billing without specifying implementation details.
 * - Promotes loose coupling: components that need calculations can rely on this interface
 *   rather than concrete subclasses.
 * - Supports polymorphism: different billing strategies (e.g. Slab billing, Commercial billing,
 *   Flat-rate billing) can implement this interface and be swapped dynamically at runtime.
 */
public interface BillCalculator {
    /**
     * Calculates the total amount for the given units consumed.
     * 
     * @param units the number of units consumed
     * @return the calculated bill amount
     */
    double calculateBill(double units);

    /**
     * Returns a string breakdown of how the bill was calculated based on slabs.
     * 
     * @param units the number of units consumed
     * @return HTML or formatted text breakdown
     */
    String getSlabBreakdown(double units);
}
