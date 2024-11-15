package org.tailkeep.worker.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic tailkeepTopic() {
        return TopicBuilder.name("tailkeep").build();
    }

    @Bean
    public NewTopic downloadQueueTopic() {
        return TopicBuilder.name(KafkaTopicNames.DOWNLOAD_QUEUE).build();
    }

    @Bean
    public NewTopic metadataQueueTopic() {
        return TopicBuilder.name(KafkaTopicNames.METADATA_QUEUE).build();
    }

    @Bean
    public NewTopic downloadProgressTopic() {
        return TopicBuilder.name(KafkaTopicNames.DOWNLOAD_PROGRESS).build();
    }

    @Bean
    public NewTopic metadataResultsTopic() {
        return TopicBuilder.name(KafkaTopicNames.METADATA_RESULTS).build();
    }
}
