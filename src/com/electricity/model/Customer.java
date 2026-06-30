package com.electricity.model;

/**
 * Customer class representing a client in the system.
 * Inherits from Person (demonstrating inheritance) and uses encapsulation.
 */
public class Customer extends Person {
    private String customerId;
    private String meterNumber;

    // Default constructor
    public Customer() {
        super();
    }

    // Parameterized constructor
    public Customer(String customerId, String name, String email, String phone, String address, String meterNumber) {
        super(name, email, phone, address);
        this.customerId = customerId;
        this.meterNumber = meterNumber;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(String meterNumber) {
        this.meterNumber = meterNumber;
    }
}
