package org.tailkeep.adapter.messaging.publisher;

public class MessagePublishException extends RuntimeException {
    public MessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
