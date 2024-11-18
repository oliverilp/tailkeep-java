package org.tailkeep.api.exception;

public class InvalidCurrentPasswordException extends RuntimeException {
    public InvalidCurrentPasswordException(String message) {
        super(message);
    }

    public InvalidCurrentPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
} 
