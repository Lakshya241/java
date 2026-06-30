package com.electricity.model;

/**
 * Bill class representing an electricity bill statement record.
 * 
 * OOP CONCEPT: Encapsulation
 * - Models billing statement metadata and metrics (previous reading, current reading, units, amount, status).
 * - Restricts direct access to member variables by defining them as private.
 * - Exposes controlled public getter and setter methods.
 */
public class Bill {
    // Private data members
    private int billId;
    private String customerId;
    private double previousReading;
    private double currentReading;
    private double unitsConsumed;
    private double totalAmount;
    private String billDate;
    private String paymentStatus;

    /**
     * Default constructor.
     */
    public Bill() {}

    /**
     * Parameterized constructor to initialize a complete billing statement record.
     */
    public Bill(int billId, String customerId, double previousReading, double currentReading, 
                double unitsConsumed, double totalAmount, String billDate, String paymentStatus) {
        this.billId = billId;
        this.customerId = customerId;
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.unitsConsumed = unitsConsumed;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
        this.paymentStatus = paymentStatus;
    }

    // --- GETTERS AND SETTERS ---

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getPreviousReading() {
        return previousReading;
    }

    public void setPreviousReading(double previousReading) {
        this.previousReading = previousReading;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
