package org.tailkeep.api.exception;

public record ApiError(String path, String message, int statusCode, String timestamp) {
    
}
