package com.dk.jpatesting.exception;

public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(String emailId) {
        super("User not found with email: " + emailId);
        this.userId = null;
    }

    public Long getUserId() {
        return userId;
    }
}
