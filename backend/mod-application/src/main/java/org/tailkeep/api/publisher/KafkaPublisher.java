package org.tailkeep.api.publisher;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.api.message.MetadataResultMessage;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.MetadataRequest;
import org.tailkeep.domain.download.MetadataResult;
import org.tailkeep.domain.download.ProgressUpdate;
import org.tailkeep.domain.download.PublishDownloadProgress;
import org.tailkeep.domain.download.PublishDownloadRequest;
import org.tailkeep.domain.download.PublishMetadataRequest;
import org.tailkeep.domain.download.PublishMetadataResult;

// Legacy: the Kafka side of the publish ports. Task 05 replaces it with the messaging adapter.
@Component
public class KafkaPublisher
        implements PublishMetadataRequest, PublishMetadataResult, PublishDownloadRequest, PublishDownloadProgress {
    private final KafkaTemplate<String, MetadataRequestMessage> metadataRequests;
    private final KafkaTemplate<String, MetadataResultMessage> metadataResults;
    private final KafkaTemplate<String, DownloadRequestMessage> downloadRequests;
    private final KafkaTemplate<String, DownloadProgressMessage> downloadProgress;

    public KafkaPublisher(
            KafkaTemplate<String, MetadataRequestMessage> metadataRequests,
            KafkaTemplate<String, MetadataResultMessage> metadataResults,
            KafkaTemplate<String, DownloadRequestMessage> downloadRequests,
            KafkaTemplate<String, DownloadProgressMessage> downloadProgress) {
        this.metadataRequests = metadataRequests;
        this.metadataResults = metadataResults;
        this.downloadRequests = downloadRequests;
        this.downloadProgress = downloadProgress;
    }

    @Override
    public void publish(MetadataRequest request) {
        metadataRequests.send(KafkaTopicNames.METADATA_QUEUE, MessageMapper.toMessage(request));
    }

    @Override
    public void publish(MetadataResult result) {
        metadataResults.send(KafkaTopicNames.METADATA_RESULTS, MessageMapper.toMessage(result));
    }

    @Override
    public void publish(DownloadRequest request) {
        downloadRequests.send(KafkaTopicNames.DOWNLOAD_QUEUE, MessageMapper.toMessage(request));
    }

    @Override
    public void publish(ProgressUpdate update) {
        downloadProgress.send(KafkaTopicNames.DOWNLOAD_PROGRESS, MessageMapper.toMessage(update));
    }
}
