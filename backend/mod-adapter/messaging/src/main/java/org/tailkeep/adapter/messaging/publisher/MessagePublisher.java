package org.tailkeep.adapter.messaging.publisher;

import org.tailkeep.adapter.messaging.contract.Destination;

/**
 * Sends a wire message to a destination. Each broker adapter implements it. It returns when the broker has
 * confirmed the message and throws {@link MessagePublishException} when it could not, so a handler that
 * publishes fails, and its own message is not acknowledged.
 */
public interface MessagePublisher {
    /** @param key messages with the same key stay in order, the job ID here */
    void publish(Destination destination, String key, Object message);
}
