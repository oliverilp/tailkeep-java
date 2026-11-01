package org.tailkeep.worker.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.domain.download.FetchMetadata;

@Component
@Slf4j
public class MetadataQueueListener {
    private final FetchMetadata fetchMetadata;

    public MetadataQueueListener(FetchMetadata fetchMetadata) {
        this.fetchMetadata = fetchMetadata;
    }

    @KafkaListener(
            topics = KafkaTopicNames.METADATA_QUEUE,
            groupId = "metadata-queue-consumer",
            containerFactory = "metadataRequestFactory"
    )
    public void onMetadataJob(MetadataRequestMessage message) {
        log.info("Received message request with url: {}", message.url());

        try {
            fetchMetadata.execute(MessageMapper.toDomain(message));
        } catch (Exception e) {
            // Legacy: a failed request is dropped, as it was before. Task 05 decides on retries.
            log.error("Failed to fetch metadata", e);
        }
    }
}
