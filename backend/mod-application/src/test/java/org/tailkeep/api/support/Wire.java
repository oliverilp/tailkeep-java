package org.tailkeep.api.support;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/** Reads and writes raw JSON on the Kafka topics, the way the worker and the broker see them. */
@Component
public class Wire {
    public static final String METADATA_QUEUE = "metadata-queue";
    public static final String METADATA_RESULTS = "metadata-results";
    public static final String DOWNLOAD_QUEUE = "download-queue";
    public static final String DOWNLOAD_PROGRESS = "download-progress";

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    /**
     * The listeners start at {@code latest}, so a message sent before they have partitions is lost.
     */
    public void awaitListenersAssigned() {
        Awaitility.await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(100)).until(() -> {
            List<MessageListenerContainer> containers = List.copyOf(registry.getListenerContainers());
            return !containers.isEmpty() && containers.stream()
                    .allMatch(c -> c.getAssignedPartitions() != null && !c.getAssignedPartitions().isEmpty());
        });
    }

    public void send(String topic, String json) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
            producer.send(new ProducerRecord<>(topic, json)).get();
        } catch (Exception e) {
            throw new AssertionError("Failed to send to " + topic, e);
        }
    }

    /**
     * Reads the topic from the start, with a new consumer group, until a message matches.
     * Messages left by earlier tests are skipped.
     */
    public JsonNode awaitMessage(String topic, Predicate<JsonNode> matches) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of(topic));
            long deadline = System.nanoTime() + TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(200))) {
                    JsonNode message = Json.parse(record.value());
                    if (matches.test(message)) {
                        return message;
                    }
                }
            }
        }
        throw new AssertionError("No matching message on " + topic + " within " + TIMEOUT);
    }

    public JsonNode awaitMessageForJob(String topic, UUID jobId) {
        return awaitMessage(topic, message -> jobId.toString().equals(message.path("jobId").asText()));
    }
}
