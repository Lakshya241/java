package com.electricity.exception;

/**
 * Custom exception representing a validation error in user input.
 * Demonstrates exception handling practices.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
