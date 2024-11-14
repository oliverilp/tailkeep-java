package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MetadataListener {

    @KafkaListener(topics = KafkaTopicNames.METADATA_RESULTS, groupId = "metadata-results-consumer")
    public void listen(String message) {
        log.info("Received metadata result: {}", message);
        // Add your processing logic here
    }
}
