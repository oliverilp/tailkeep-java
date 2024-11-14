package org.tailkeep.worker.config;

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
import org.tailkeep.worker.metadata.MetadataResultMessage;
import org.tailkeep.worker.metadata.Metadata;

@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  public Map<String, Object> producerConfig() {
    return Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    // ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
  }

  // @Bean
  // public ProducerFactory<String, Object> producerFactory() {
  // return new DefaultKafkaProducerFactory<>(producerConfig());
  // }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    var producerFactory = new DefaultKafkaProducerFactory<String, String>(producerConfig());

    return new KafkaTemplate<String, String>(producerFactory);
  }

  @Bean
  public KafkaTemplate<String, MetadataResultMessage> metadataKafkaTemplate() {
    var producerFactory = new DefaultKafkaProducerFactory<String, MetadataResultMessage>(producerConfig());

    return new KafkaTemplate<String, MetadataResultMessage>(producerFactory);
  }

  // @Bean
  // public KafkaTemplate<String, MetadataResultMessage> downloadKafkaTemplate() {
  // var producerFactory = new DefaultKafkaProducerFactory<String,
  // MetadataResultMessage>(producerConfig());

  // return new KafkaTemplate<String, MetadataResultMessage>(producerFactory);
  // }
}
