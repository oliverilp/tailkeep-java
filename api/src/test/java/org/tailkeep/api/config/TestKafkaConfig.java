package org.tailkeep.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.tailkeep.api.message.MetadataResultMessage;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TestKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(JsonSerializer.TYPE_MAPPINGS, 
            "metadataResult:org.tailkeep.api.message.MetadataResultMessage");
        return props;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.TYPE_MAPPINGS, 
            "metadataResult:org.tailkeep.api.message.MetadataResultMessage");
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setAutoStartup(true);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public NewTopic metadataResultsTopic() {
        return TopicBuilder.name(KafkaTopicNames.METADATA_RESULTS)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public ProducerFactory<String, MetadataResultMessage> metadataProducerFactory() {
        Map<String, Object> configProps = new HashMap<>(producerConfigs());
        configProps.put(JsonSerializer.TYPE_MAPPINGS, 
            "metadataResult:org.tailkeep.api.message.MetadataResultMessage");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, MetadataResultMessage> metadataKafkaTemplate(
            ProducerFactory<String, MetadataResultMessage> metadataProducerFactory) {
        return new KafkaTemplate<>(metadataProducerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MetadataResultMessage> metadataFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MetadataResultMessage> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        ConsumerFactory<String, MetadataResultMessage> consumerFactory = new DefaultKafkaConsumerFactory<>(
            consumerConfigs(),
            new StringDeserializer(),
            new JsonDeserializer<>(MetadataResultMessage.class)
        );
        
        factory.setConsumerFactory(consumerFactory);
        factory.setAutoStartup(true);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
