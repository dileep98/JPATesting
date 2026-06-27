package com.dk.jpatesting.exception;

public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("User already exists with email: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
