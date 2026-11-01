package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.api.message.MetadataResultMessage;
import org.tailkeep.domain.download.RecordMetadata;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetadataResultListener {
    private final RecordMetadata recordMetadata;

    public MetadataResultListener(RecordMetadata recordMetadata) {
        this.recordMetadata = recordMetadata;
    }

    @KafkaListener(topics = KafkaTopicNames.METADATA_RESULTS, groupId = "metadata-results-consumer", containerFactory = "metadataResultFactory")
    public void listen(MetadataResultMessage message) {
        log.info("Received metadata result: {}", message);

        recordMetadata.execute(MessageMapper.toDomain(message));

        log.info("Sent download request for job: {}", message.jobId());
    }
}
