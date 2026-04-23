package com.example.subscription.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String email) {
        super("The email address '" + email + "' failed external validation. " +
              "Please provide a valid, deliverable email address.");
    }
}
