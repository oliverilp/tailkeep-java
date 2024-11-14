package org.tailkeep.worker.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.worker.config.KafkaTopicNames;
import org.tailkeep.worker.metadata.MetadataRequestMessage;
import org.tailkeep.worker.metadata.MetadataResultMessage;
import org.tailkeep.worker.metadata.MetadataFetcher;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetadataQueueListener {
    private final MetadataFetcher metadataFetcher;
    private final KafkaTemplate<String, MetadataResultMessage> kafkaTemplate;

    public MetadataQueueListener(MetadataFetcher metadataFetcher,
            KafkaTemplate<String, MetadataResultMessage> metadataKafkaTemplate) {
        this.metadataFetcher = metadataFetcher;
        this.kafkaTemplate = metadataKafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopicNames.METADATA_QUEUE, groupId = "metadata-queue-consumer", containerFactory = "factory")
    public void onMetadataJob(MetadataRequestMessage messageRequest) {
        String url = messageRequest.url();
        log.info("Received message request with url: {}", url);

        metadataFetcher.fetch(url)
                .thenAccept(metadata -> {
                    log.info("\nMetadata fetch completed successfully:");
                    log.info("----------------------------------------");
                    log.info("Title: " + metadata.title());
                    log.info("Channel: " + metadata.uploader());
                    log.info("Duration: " + metadata.durationString());
                    log.info("Views: " + metadata.viewCount());
                    log.info("Video ID: " + metadata.youtubeId());
                    log.info("Filename: " + metadata.filename());
                    log.info("----------------------------------------\n");

                    var result = new MetadataResultMessage(messageRequest.jobId(), metadata);

                    kafkaTemplate.send(KafkaTopicNames.METADATA_RESULTS, result);
                });
    }
}
