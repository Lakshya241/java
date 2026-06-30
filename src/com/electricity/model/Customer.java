package com.electricity.model;

/**
 * Customer class representing a client in the system.
 * 
 * OOP CONCEPT: Inheritance and Encapsulation
 * - 'Customer' extends 'Person', inheriting all general attributes (name, email, phone, address).
 * - Code Reusability: Reuses the constructor and getter/setter logic of the parent 'Person' class via 'super'.
 * - Extends functionality by adding customer-specific attributes: customerId and meterNumber.
 * - Restricts access to customerId and meterNumber through private fields and public accessors.
 */
public class Customer extends Person {
    // Customer-specific private members (Encapsulation)
    private String customerId;
    private String meterNumber;

    /**
     * Default constructor calling parent default constructor.
     */
    public Customer() {
        super();
    }

    /**
     * Parameterized constructor.
     * Passes general attributes to the parent constructor using 'super'.
     */
    public Customer(String customerId, String name, String email, String phone, String address, String meterNumber) {
        // OOP CONCEPT: Invoking parent class constructor to reuse initialization logic
        super(name, email, phone, address);
        this.customerId = customerId;
        this.meterNumber = meterNumber;
    }

    // --- GETTERS AND SETTERS (Encapsulation Interfaces) ---

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
