package org.tailkeep.worker.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.tailkeep.worker.download.DownloadProgressMessage;
import org.tailkeep.worker.metadata.MetadataResultMessage;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> producerConfig() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.ADD_TYPE_INFO_HEADERS, false
            ); // Needed to avoid type mappings in consumer
    }

    @Bean
    public KafkaTemplate<String, MetadataResultMessage> metadataKafkaTemplate() {
        var producerFactory = new DefaultKafkaProducerFactory<String, MetadataResultMessage>(producerConfig());

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, DownloadProgressMessage> downloadKafkaTemplate() {
        var producerFactory = new DefaultKafkaProducerFactory<String, DownloadProgressMessage>(producerConfig());

        return new KafkaTemplate<>(producerFactory);
    }
}
