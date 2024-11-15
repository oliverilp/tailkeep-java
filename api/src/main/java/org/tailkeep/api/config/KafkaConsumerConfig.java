package org.tailkeep.api.config;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.tailkeep.api.model.MetadataResultMessage;
import org.tailkeep.api.model.DownloadProgress;

@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> consumerConfig() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
        );
    }

    private <T> JsonDeserializer<T> createJsonDeserializer(Class<T> targetClass) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetClass, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetClass) {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig(),
                new StringDeserializer(),
                createJsonDeserializer(targetClass));
    }

    private <T> KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, T>> createListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, MetadataResultMessage> metadataConsumerFactory() {
        return createConsumerFactory(MetadataResultMessage.class);
    }

    @Bean
    public ConsumerFactory<String, DownloadProgress> downloadConsumerFactory() {
        return createConsumerFactory(DownloadProgress.class);
    }

    @Bean("metadataFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MetadataResultMessage>> metadataListenerContainerFactory(
            ConsumerFactory<String, MetadataResultMessage> metadataConsumerFactory) {
        return createListenerContainerFactory(metadataConsumerFactory);
    }

    @Bean("downloadFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, DownloadProgress>> downloadListenerContainerFactory(
            ConsumerFactory<String, DownloadProgress> downloadConsumerFactory) {
        return createListenerContainerFactory(downloadConsumerFactory);
    }
}
