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

@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  public Map<String, Object> producerConfig() {
    return Map.of(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
        JsonSerializer.TYPE_MAPPINGS,
        "org.tailkeep.api.model.MetadataRequestMessage:org.tailkeep.worker.metadata.MetadataRequestMessage");
  }

  @Bean
  public ProducerFactory<String, MetadataRequestMessage> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfig());
  }

  // @Bean
  // public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String,
  // String> producerFactory) {
  // return new KafkaTemplate<>(producerFactory);
  // }

  @Bean
  public KafkaTemplate<String, MetadataRequestMessage> messageRequestKafkaTemplate(
      ProducerFactory<String, MetadataRequestMessage> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  // @Bean
  // public KafkaTemplate<String, DownloadRequest> downloadRequestKafkaTemplate(
  // ProducerFactory<String, DownloadRequest> producerFactory) {
  // return new KafkaTemplate<>(producerFactory);
  // }

  // @Bean
  // public KafkaTemplate<String, MetadataRequest> metadataRequestKafkaTemplate(
  // ProducerFactory<String, MetadataRequest> producerFactory) {
  // return new KafkaTemplate<>(producerFactory);
  // }
}
