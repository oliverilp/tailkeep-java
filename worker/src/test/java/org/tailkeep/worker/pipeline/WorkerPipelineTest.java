package org.tailkeep.worker.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.tailkeep.worker.command.ProcessFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the worker's pipeline against a real broker, with yt-dlp replaced by {@link FakeYtDlp}. Messages go in and
 * out as raw JSON, so the topic names and the wire format are pinned.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "metadata-queue", "metadata-results", "download-queue", "download-progress" })
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class WorkerPipelineTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @TestConfiguration
    static class FakeYtDlpConfig {
        @Bean
        @Primary
        ProcessFactory fakeYtDlp() {
            // The worker reads the downloaded file's size from MEDIA_PATH, which the build sets for tests
            return new FakeYtDlp(System.getenv("MEDIA_PATH"));
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @BeforeEach
    void waitForListeners() {
        // The listeners start at the latest offset, so they must have partitions before a message is sent
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    void metadataRequest_ShouldProduceTheVideoMetadata() throws Exception {
        String jobId = UUID.randomUUID().toString();

        send("metadata-queue", "{\"jobId\":\"%s\",\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"}"
                .formatted(jobId));

        JsonNode result = awaitMessages("metadata-results", jobId, message -> true).getFirst();
        assertThat(fieldNames(result)).containsExactlyInAnyOrder("jobId", "metadata");
        assertThat(result.get("jobId").asText()).isEqualTo(jobId);
        JsonNode metadata = result.get("metadata");
        assertThat(fieldNames(metadata)).containsExactlyInAnyOrder("youtubeId", "url", "title", "uploader",
                "channelId", "channelUrl", "durationString", "duration", "thumbnailUrl", "description", "viewCount",
                "commentCount", "filename");
        assertThat(metadata.get("youtubeId").asText()).isEqualTo("dQw4w9WgXcQ");
        assertThat(metadata.get("url").asText()).isEqualTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertThat(metadata.get("title").asText()).isEqualTo("Rick Astley - Never Gonna Give You Up (Official Music Video)");
        assertThat(metadata.get("uploader").asText()).isEqualTo("Rick Astley");
        assertThat(metadata.get("channelId").asText()).isEqualTo("UCuAXFkgsw1L7xaCfnd5JJOw");
        assertThat(metadata.get("channelUrl").asText())
                .isEqualTo("https://www.youtube.com/channel/UCuAXFkgsw1L7xaCfnd5JJOw");
        assertThat(metadata.get("durationString").asText()).isEqualTo("3:32");
        assertThat(metadata.get("duration").asDouble()).isEqualTo(212.0);
        assertThat(metadata.get("thumbnailUrl").asText())
                .isEqualTo("https://i.ytimg.com/vi_webp/dQw4w9WgXcQ/maxresdefault.webp");
        assertThat(metadata.get("description").asText()).startsWith("The official video");
        assertThat(metadata.get("viewCount").asLong()).isEqualTo(1605836884L);
        assertThat(metadata.get("commentCount").asLong()).isEqualTo(2300000L);
        assertThat(metadata.get("filename").asText()).isEqualTo(FakeYtDlp.filename());
    }

    @Test
    void downloadRequest_ShouldReportProgressUntilTheDownloadHasEnded() throws Exception {
        String jobId = UUID.randomUUID().toString();

        send("download-queue", "{\"jobId\":\"%s\",\"videoId\":\"dQw4w9WgXcQ\",\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\",\"filename\":\"%s\"}"
                .formatted(jobId, FakeYtDlp.filename()));

        List<JsonNode> updates = awaitMessages("download-progress", jobId, message -> message.get("hasEnded").asBoolean());
        updates.forEach(update -> {
            assertThat(fieldNames(update)).containsExactlyInAnyOrder("videoId", "jobId", "status", "hasEnded",
                    "progress", "size", "speed", "eta");
            assertThat(update.get("videoId").asText()).isEqualTo("dQw4w9WgXcQ");
            assertThat(update.get("jobId").asText()).isEqualTo(jobId);
        });

        JsonNode midway = updates.stream().filter(update -> update.get("progress").asDouble() == 12.3).findFirst()
                .orElseThrow();
        assertThat(midway.get("status").asText()).isEqualTo("download");
        assertThat(midway.get("hasEnded").asBoolean()).isFalse();
        assertThat(midway.get("size").asText()).isEqualTo("50.00MiB");
        assertThat(midway.get("speed").asText()).isEqualTo("2.00MiB/s");
        assertThat(midway.get("eta").asText()).isEqualTo("00:25");

        List<Double> progress = updates.stream().map(update -> update.get("progress").asDouble()).toList();
        assertThat(progress).isSorted();

        // Only the last update has ended, and its size is the size of the file on disk
        assertThat(updates.stream().filter(update -> update.get("hasEnded").asBoolean())).hasSize(1);
        JsonNode last = updates.getLast();
        assertThat(last.get("hasEnded").asBoolean()).isTrue();
        assertThat(last.get("progress").asDouble()).isEqualTo(100.0);
        assertThat(last.get("size").asText()).isEqualTo("3.00KiB");
        assertThat(last.get("speed").asText()).isEqualTo("3.30MiB/s");
    }

    private static List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private void send(String topic, String json) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
            producer.send(new ProducerRecord<>(topic, json)).get();
        } catch (Exception e) {
            throw new AssertionError("Failed to send to " + topic, e);
        }
    }

    /** Reads the messages for the job, in order, up to and including the first one that satisfies {@code last}. */
    private List<JsonNode> awaitMessages(String topic, String jobId, java.util.function.Predicate<JsonNode> last)
            throws Exception {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            consumer.subscribe(List.of(topic));
            List<JsonNode> messages = new ArrayList<>();
            long deadline = System.nanoTime() + TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(200))) {
                    JsonNode message = objectMapper.readTree(record.value());
                    if (jobId.equals(message.path("jobId").asText())) {
                        messages.add(message);
                        if (last.test(message)) {
                            return messages;
                        }
                    }
                }
            }
        }
        throw new AssertionError("The job did not complete on " + topic + " within " + TIMEOUT);
    }
}
