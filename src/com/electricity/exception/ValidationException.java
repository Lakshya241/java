package com.electricity.exception;

/**
 * Custom Exception representing a validation error in user input fields.
 * 
 * DESIGN PRINCIPLE: Exception Handling
 * - Extending 'Exception' creates a checked exception, forcing the compiler to verify
 *   that calling methods catch or propagate it.
 * - Used to separate input validation checks from standard application execution flows.
 * - Implements descriptive messages to give developers and users clear context on what inputs are wrong.
 */
public class ValidationException extends Exception {
    /**
     * Parameterized constructor providing custom warning messages.
     * 
     * @param message warning context
     */
    public ValidationException(String message) {
        super(message);
    }
}
