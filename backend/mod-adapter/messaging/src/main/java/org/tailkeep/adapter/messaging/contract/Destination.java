package org.tailkeep.adapter.messaging.contract;

/**
 * The logical places a message can go, whatever the broker calls them. Each broker adapter maps a destination to
 * its own name (the Kafka topic) and its {@link DeliveryType} to its own features.
 */
public enum Destination {
    METADATA_REQUESTS(DeliveryType.QUEUE),
    METADATA_RESULTS(DeliveryType.QUEUE),
    DOWNLOAD_REQUESTS(DeliveryType.QUEUE),
    DOWNLOAD_PROGRESS(DeliveryType.QUEUE),
    DOWNLOAD_BROADCAST(DeliveryType.BROADCAST);

    private final DeliveryType deliveryType;

    Destination(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public DeliveryType deliveryType() {
        return deliveryType;
    }
}
