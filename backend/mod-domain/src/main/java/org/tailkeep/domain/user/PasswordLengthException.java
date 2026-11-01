package org.tailkeep.domain.user;

public class PasswordLengthException extends RuntimeException {
    public PasswordLengthException(String message) {
        super(message);
    }
}
