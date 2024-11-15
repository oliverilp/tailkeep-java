package org.tailkeep.api.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.tailkeep.api.model.MetadataRequestMessage;
import org.tailkeep.api.model.DownloadRequestMessage;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> producerConfig() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                // Needed to avoid type mappings in consumer
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    }

    @Bean
    public ProducerFactory<String, MetadataRequestMessage> metadataProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public ProducerFactory<String, DownloadRequestMessage> downloadProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, MetadataRequestMessage> metadataRequestKafkaTemplate(
            ProducerFactory<String, MetadataRequestMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, DownloadRequestMessage> downloadRequestKafkaTemplate(
            ProducerFactory<String, DownloadRequestMessage> downloadProducerFactory) {
        return new KafkaTemplate<>(downloadProducerFactory);
    }
}
