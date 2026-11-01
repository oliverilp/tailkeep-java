package org.tailkeep.adapter.web.shared;

public record ApiError(String path, String message, int statusCode, String timestamp) {
    
}
