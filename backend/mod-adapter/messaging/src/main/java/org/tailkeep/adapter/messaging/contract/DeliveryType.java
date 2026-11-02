package org.tailkeep.adapter.messaging.contract;

/** How a destination delivers its messages. Each broker maps the two types to its own features. */
public enum DeliveryType {
    /** One instance handles each message, at least once. */
    QUEUE,
    /** Every instance receives each message. */
    BROADCAST
}
