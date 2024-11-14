package org.tailkeep.api.config;

public class KafkaTopicNames {
    public static final String DOWNLOAD_QUEUE = "download-queue";
    public static final String METADATA_QUEUE = "metadata-queue";
    public static final String PROGRESS_UPDATES = "progress-updates";
    public static final String METADATA_RESULTS = "metadata-results";

    private KafkaTopicNames() {
    } // Prevent instantiation
}
