package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.model.DownloadRequestMessage;
import org.tailkeep.api.model.MetadataResultMessage;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetadataListener {
    private final KafkaTemplate<String, DownloadRequestMessage> downloadKafkaTemplate;

    public MetadataListener(KafkaTemplate<String, DownloadRequestMessage> downloadRequestKafkaTemplate) {
        this.downloadKafkaTemplate = downloadRequestKafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopicNames.METADATA_RESULTS, groupId = "metadata-results-consumer", containerFactory = "metadataFactory")
    public void listen(MetadataResultMessage message) {
        log.info("Received metadata result: {}", message);

        DownloadRequestMessage downloadRequest = new DownloadRequestMessage(
                message.jobId(),
                message.metadata().youtubeId(),
                message.metadata().url(),
                message.metadata().filename());
        downloadKafkaTemplate.send(KafkaTopicNames.DOWNLOAD_QUEUE, downloadRequest);

        log.info("Sent download request for job: {}", message.jobId());
    }
}
