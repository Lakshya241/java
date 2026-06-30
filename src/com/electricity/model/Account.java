package com.electricity.model;

/**
 * Account class representing user credentials for authentication.
 * 
 * OOP CONCEPT: Encapsulation
 * - Protects secure credentials (username, password) using private modifiers.
 * - Provides public getters and setters to encapsulate the state.
 */
public class Account {
    // Private data members
    private String username;
    private String password;
    private String role;
    private String customerId; // References customer_id, null for admins

    /**
     * Default constructor.
     */
    public Account() {}

    /**
     * Parameterized constructor.
     */
    public Account(String username, String password, String role, String customerId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
    }

    // --- GETTERS AND SETTERS ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
