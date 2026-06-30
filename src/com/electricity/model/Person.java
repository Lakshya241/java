package com.electricity.model;

/**
 * Base class representing a general Person.
 * 
 * OOP CONCEPT: Abstraction and Encapsulation
 * - This class encapsulates general contact information (name, email, phone, address).
 * - All instance fields are declared 'private' to prevent direct outside manipulation.
 * - Public getter and setter methods are provided to access and modify these fields,
 *   enabling data validation and safeguarding data integrity.
 */
public class Person {
    // Private data members (Encapsulation)
    private String name;
    private String email;
    private String phone;
    private String address;

    /**
     * Default constructor.
     */
    public Person() {}

    /**
     * Parameterized constructor to initialize a person's contact details.
     */
    public Person(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // --- GETTERS AND SETTERS (Encapsulation Interfaces) ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
