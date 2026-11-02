package org.tailkeep.adapter.messaging.kafka;

import org.tailkeep.adapter.messaging.contract.Destination;

/** The Kafka topic of each destination. The first four names are part of the wire format. */
final class KafkaTopics {
    static final String METADATA_REQUESTS = "metadata-queue";
    static final String METADATA_RESULTS = "metadata-results";
    static final String DOWNLOAD_REQUESTS = "download-queue";
    static final String DOWNLOAD_PROGRESS = "download-progress";
    static final String DOWNLOAD_BROADCAST = "download-broadcast";

    private KafkaTopics() {
    }

    static String of(Destination destination) {
        return switch (destination) {
            case METADATA_REQUESTS -> METADATA_REQUESTS;
            case METADATA_RESULTS -> METADATA_RESULTS;
            case DOWNLOAD_REQUESTS -> DOWNLOAD_REQUESTS;
            case DOWNLOAD_PROGRESS -> DOWNLOAD_PROGRESS;
            case DOWNLOAD_BROADCAST -> DOWNLOAD_BROADCAST;
        };
    }
}
