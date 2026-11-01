package org.tailkeep.adapter.messaging.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.adapter.messaging.contract.DownloadProgressMessage;
import org.tailkeep.adapter.messaging.contract.DownloadRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataResultMessage;
import org.tailkeep.adapter.messaging.handler.DownloadBroadcastHandler;
import org.tailkeep.adapter.messaging.handler.DownloadProgressHandler;
import org.tailkeep.adapter.messaging.handler.DownloadRequestHandler;
import org.tailkeep.adapter.messaging.handler.MetadataRequestHandler;
import org.tailkeep.adapter.messaging.handler.MetadataResultHandler;

/**
 * Receives the messages from Kafka and passes them on. The container acknowledges a message when its method
 * returns, so a method that throws leaves the message to be retried. The queues have one group each, shared by all
 * instances. The broadcast has a group per instance, set in {@link KafkaMessagingConfig}.
 */
@Component
@ConditionalOnKafkaProvider
public class KafkaMessageListeners {
    private final MetadataRequestHandler metadataRequestHandler;
    private final MetadataResultHandler metadataResultHandler;
    private final DownloadRequestHandler downloadRequestHandler;
    private final DownloadProgressHandler downloadProgressHandler;
    private final DownloadBroadcastHandler downloadBroadcastHandler;

    public KafkaMessageListeners(
            MetadataRequestHandler metadataRequestHandler,
            MetadataResultHandler metadataResultHandler,
            DownloadRequestHandler downloadRequestHandler,
            DownloadProgressHandler downloadProgressHandler,
            DownloadBroadcastHandler downloadBroadcastHandler) {
        this.metadataRequestHandler = metadataRequestHandler;
        this.metadataResultHandler = metadataResultHandler;
        this.downloadRequestHandler = downloadRequestHandler;
        this.downloadProgressHandler = downloadProgressHandler;
        this.downloadBroadcastHandler = downloadBroadcastHandler;
    }

    @KafkaListener(topics = KafkaTopics.METADATA_REQUESTS, groupId = "metadata-queue-consumer",
            containerFactory = "metadataRequestFactory")
    public void onMetadataRequest(MetadataRequestMessage message) {
        metadataRequestHandler.handle(message);
    }

    @KafkaListener(topics = KafkaTopics.METADATA_RESULTS, groupId = "metadata-results-consumer",
            containerFactory = "metadataResultFactory")
    public void onMetadataResult(MetadataResultMessage message) {
        metadataResultHandler.handle(message);
    }

    @KafkaListener(topics = KafkaTopics.DOWNLOAD_REQUESTS, groupId = "download-queue-consumer",
            containerFactory = "downloadRequestFactory")
    public void onDownloadRequest(DownloadRequestMessage message) {
        downloadRequestHandler.handle(message);
    }

    @KafkaListener(topics = KafkaTopics.DOWNLOAD_PROGRESS, groupId = "download-progress-consumer",
            containerFactory = "downloadProgressFactory")
    public void onDownloadProgress(DownloadProgressMessage message) {
        downloadProgressHandler.handle(message);
    }

    @KafkaListener(topics = KafkaTopics.DOWNLOAD_BROADCAST, containerFactory = "downloadBroadcastFactory")
    public void onDownloadBroadcast(DownloadBroadcastMessage message) {
        downloadBroadcastHandler.handle(message);
    }
}
