package com.example.subscription.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("A subscription already exists for email: " + email);
    }
}
