package org.tailkeep.adapter.security.auth;

/** The same JSON as {@code ApiError} in {@code web}, which this adapter may not depend on. */
record SecurityError(String path, String message, int statusCode, String timestamp) {
}
