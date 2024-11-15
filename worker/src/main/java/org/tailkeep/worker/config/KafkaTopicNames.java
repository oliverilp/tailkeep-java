package org.tailkeep.worker.config;

public class KafkaTopicNames {
    public static final String DOWNLOAD_QUEUE = "download-queue";
    public static final String METADATA_QUEUE = "metadata-queue";
    public static final String DOWNLOAD_PROGRESS = "download-progress";
    public static final String METADATA_RESULTS = "metadata-results";

    private KafkaTopicNames() {
    } // Prevent instantiation
}
