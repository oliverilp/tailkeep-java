package org.tailkeep.domain.user.model;

public class InvalidCurrentPasswordException extends RuntimeException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
