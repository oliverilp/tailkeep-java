package org.tailkeep.domain.download;

/** The URL of a download request is not acceptable. */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
