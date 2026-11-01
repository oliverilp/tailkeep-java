package org.tailkeep.adapter.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.tailkeep.adapter.messaging.contract.Destination;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.adapter.messaging.contract.DownloadProgressMessage;
import org.tailkeep.adapter.messaging.contract.DownloadRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataRequestMessage;
import org.tailkeep.adapter.messaging.contract.MetadataResultMessage;
import org.tailkeep.domain.shared.ResourceNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka settings. The connection comes from {@code spring.kafka.*}. What the design needs is set here:
 * acknowledging after the handler returned, a bounded number of attempts, and a consumer group per instance for
 * the broadcast.
 */
@Configuration
@ConditionalOnKafkaProvider
@EnableConfigurationProperties({KafkaProperties.class, KafkaMessagingProperties.class})
public class KafkaMessagingConfig {

    /** Every instance has its own group, so every instance receives each broadcast. */
    private static final String BROADCAST_GROUP_PREFIX = "tailkeep-broadcast-";

    @Bean
    KafkaAdmin.NewTopics messagingTopics() {
        return new KafkaAdmin.NewTopics(Arrays.stream(Destination.values())
                .map(destination -> TopicBuilder.name(KafkaTopics.of(destination)).build())
                .toArray(org.apache.kafka.clients.admin.NewTopic[]::new));
    }

    @Bean
    ProducerFactory<String, Object> messagingProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        JsonSerializer<Object> valueSerializer = new JsonSerializer<>();
        // The consumers read the target type from their own config, not from a header
        valueSerializer.setAddTypeInfo(false);
        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), valueSerializer);
    }

    @Bean
    KafkaTemplate<String, Object> messagingKafkaTemplate(ProducerFactory<String, Object> messagingProducerFactory) {
        return new KafkaTemplate<>(messagingProducerFactory);
    }

    @Bean("metadataRequestFactory")
    ConcurrentKafkaListenerContainerFactory<String, MetadataRequestMessage> metadataRequestFactory(
            KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        return factory(MetadataRequestMessage.class, consumerConfig(kafkaProperties, properties), properties);
    }

    @Bean("metadataResultFactory")
    ConcurrentKafkaListenerContainerFactory<String, MetadataResultMessage> metadataResultFactory(
            KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        return factory(MetadataResultMessage.class, consumerConfig(kafkaProperties, properties), properties);
    }

    /**
     * The handler holds the listener thread until the download ends, so the consumer may stay away from poll for
     * that long, and it takes one message at a time so nothing waits behind a download in a batch.
     */
    @Bean("downloadRequestFactory")
    ConcurrentKafkaListenerContainerFactory<String, DownloadRequestMessage> downloadRequestFactory(
            KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        Map<String, Object> config = consumerConfig(kafkaProperties, properties);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                (int) Math.min(properties.getMaxDownloadDuration().toMillis(), Integer.MAX_VALUE));
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        return factory(DownloadRequestMessage.class, config, properties);
    }

    @Bean("downloadProgressFactory")
    ConcurrentKafkaListenerContainerFactory<String, DownloadProgressMessage> downloadProgressFactory(
            KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        return factory(DownloadProgressMessage.class, consumerConfig(kafkaProperties, properties), properties);
    }

    /** Own group per instance, started at the end: a broadcast that was sent before the instance started is stale. */
    @Bean("downloadBroadcastFactory")
    ConcurrentKafkaListenerContainerFactory<String, DownloadBroadcastMessage> downloadBroadcastFactory(
            KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        Map<String, Object> config = consumerConfig(kafkaProperties, properties);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, BROADCAST_GROUP_PREFIX + UUID.randomUUID());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return factory(DownloadBroadcastMessage.class, config, properties);
    }

    private static Map<String, Object> consumerConfig(KafkaProperties kafkaProperties, KafkaMessagingProperties properties) {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        return config;
    }

    private static <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(
            Class<T> type, Map<String, Object> config, KafkaMessagingProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // A message that cannot be read is skipped by the error handler instead of failing again and again
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                config,
                StringDeserializer::new,
                () -> new ErrorHandlingDeserializer<>(jsonDeserializer(type))));
        // The offset is committed after the listener method returned
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandler(properties));
        return factory;
    }

    private static <T> JsonDeserializer<T> jsonDeserializer(Class<T> type) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(type, false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    /** A failed message is tried again a few times, and then skipped with a log. A missing job will not appear by waiting. */
    private static DefaultErrorHandler errorHandler(KafkaMessagingProperties properties) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new FixedBackOff(properties.getRetryInterval().toMillis(), properties.getMaxAttempts() - 1L));
        handler.addNotRetryableExceptions(ResourceNotFoundException.class);
        return handler;
    }
}
