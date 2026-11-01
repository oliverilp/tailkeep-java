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
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MetadataRequestMessage;
import org.tailkeep.api.message.MetadataResultMessage;

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
    public ConsumerFactory<String, MetadataRequestMessage> metadataRequestConsumerFactory() {
        return createConsumerFactory(MetadataRequestMessage.class);
    }

    @Bean
    public ConsumerFactory<String, MetadataResultMessage> metadataResultConsumerFactory() {
        return createConsumerFactory(MetadataResultMessage.class);
    }

    @Bean
    public ConsumerFactory<String, DownloadRequestMessage> downloadRequestConsumerFactory() {
        return createConsumerFactory(DownloadRequestMessage.class);
    }

    @Bean
    public ConsumerFactory<String, DownloadProgressMessage> downloadProgressConsumerFactory() {
        return createConsumerFactory(DownloadProgressMessage.class);
    }

    @Bean("metadataRequestFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MetadataRequestMessage>> metadataRequestListenerContainerFactory(
            ConsumerFactory<String, MetadataRequestMessage> metadataRequestConsumerFactory) {
        return createListenerContainerFactory(metadataRequestConsumerFactory);
    }

    @Bean("metadataResultFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MetadataResultMessage>> metadataResultListenerContainerFactory(
            ConsumerFactory<String, MetadataResultMessage> metadataResultConsumerFactory) {
        return createListenerContainerFactory(metadataResultConsumerFactory);
    }

    @Bean("downloadRequestFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, DownloadRequestMessage>> downloadRequestListenerContainerFactory(
            ConsumerFactory<String, DownloadRequestMessage> downloadRequestConsumerFactory) {
        return createListenerContainerFactory(downloadRequestConsumerFactory);
    }

    @Bean("downloadProgressFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, DownloadProgressMessage>> downloadProgressListenerContainerFactory(
            ConsumerFactory<String, DownloadProgressMessage> downloadProgressConsumerFactory) {
        return createListenerContainerFactory(downloadProgressConsumerFactory);
    }
}
