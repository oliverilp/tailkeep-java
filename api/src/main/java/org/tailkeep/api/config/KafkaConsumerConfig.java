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

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public Map<String, Object> consumerConfig() {
        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    }

    @Bean
    public ConsumerFactory<String, MetadataResultMessage> metadataConsumerFactory() {
        JsonDeserializer<MetadataResultMessage> jsonDeserializer = new JsonDeserializer<>(MetadataResultMessage.class,
                false);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(),
                jsonDeserializer);
    }

    // @Bean
    // public ConsumerFactory<String, DownloadRequestMessage>
    // downloadConsumerFactory() {
    // JsonDeserializer<DownloadRequestMessage> jsonDeserializer = new
    // JsonDeserializer<>(DownloadRequestMessage.class,
    // false);
    // jsonDeserializer.addTrustedPackages("*");
    // jsonDeserializer.setUseTypeHeaders(false);

    // return new DefaultKafkaConsumerFactory<>(
    // consumerConfig(),
    // new StringDeserializer(),
    // jsonDeserializer);
    // }

    @Bean("metadataFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, MetadataResultMessage>> metadataListenerContainerFactory(
            ConsumerFactory<String, MetadataResultMessage> metadataConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, MetadataResultMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(metadataConsumerFactory);
        return factory;
    }

    // @Bean("downloadFactory")
    // public
    // KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,
    // DownloadRequestMessage>> downloadListenerContainerFactory(
    // ConsumerFactory<String, DownloadRequestMessage> downloadConsumerFactory) {
    // ConcurrentKafkaListenerContainerFactory<String, DownloadRequestMessage>
    // factory = new ConcurrentKafkaListenerContainerFactory<>();
    // factory.setConsumerFactory(downloadConsumerFactory);
    // return factory;
    // }
}
