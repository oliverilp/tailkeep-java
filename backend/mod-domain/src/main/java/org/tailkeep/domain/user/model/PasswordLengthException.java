package org.tailkeep.domain.user.model;

public class PasswordLengthException extends RuntimeException {
    public PasswordLengthException(String message) {
        super(message);
    }
}
