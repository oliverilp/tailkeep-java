package org.tailkeep.domain.download.model;

/** The URL of a download request is not acceptable. */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
