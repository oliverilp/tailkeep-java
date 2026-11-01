package org.tailkeep.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
} 
