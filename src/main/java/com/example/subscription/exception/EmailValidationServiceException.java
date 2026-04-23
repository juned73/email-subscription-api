package com.example.subscription.exception;

public class EmailValidationServiceException extends RuntimeException {
    public EmailValidationServiceException(String message) {
        super(message);
    }

    public EmailValidationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
