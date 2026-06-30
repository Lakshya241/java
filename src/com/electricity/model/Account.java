package com.electricity.model;

/**
 * Account class representing a user credential record for authentication.
 * Demonstrates encapsulation.
 */
public class Account {
    private String username;
    private String password;
    private String role;
    private String customerId; // Null for admin accounts

    public Account() {}

    public Account(String username, String password, String role, String customerId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
    }

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
