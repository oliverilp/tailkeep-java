package org.tailkeep.adapter.messaging.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** A process killed in the middle of a download does not acknowledge the request, so another gets it. */
@Testcontainers
class KafkaRedeliveryTest {
    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Test
    void aDownloadRequestOfAKilledProcessIsDeliveredToTheNextInstance() throws Exception {
        UUID jobId = UUID.randomUUID();
        Process killed = new ProcessBuilder(
                new File(System.getProperty("java.home"), "bin/java").getPath(),
                "-cp", System.getProperty("messaging.test.classpath"),
                BlockingDownloadProcess.class.getName(), kafka.getBootstrapServers())
                .redirectErrorStream(true)
                .start();
        try (BufferedReader output = new BufferedReader(new InputStreamReader(killed.getInputStream()))) {
            awaitLine(output, "READY");

            sendDownloadRequest(jobId);
            awaitLine(output, "STARTED " + jobId);
            killed.destroyForcibly().waitFor(30, TimeUnit.SECONDS);
        } finally {
            killed.destroyForcibly();
        }

        // Only now, so the killed process had the partitions. Its session ends after a few seconds.
        try (TestInstance next = TestInstance.start(kafka.getBootstrapServers())) {
            await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> assertThat(next.downloaded).containsExactly(jobId));
        }
    }

    private static void awaitLine(BufferedReader output, String expected) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(90).toNanos();
        StringBuilder seen = new StringBuilder();
        String line;
        while (System.nanoTime() < deadline && (line = output.readLine()) != null) {
            if (line.equals(expected)) {
                return;
            }
            seen.append(line).append('\n');
        }
        throw new AssertionError("The other process never printed " + expected + ". It printed:\n" + seen);
    }

    /** As raw JSON, which is what the wire format is. */
    private static void sendDownloadRequest(UUID jobId) throws Exception {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(
                Map.of("bootstrap.servers", kafka.getBootstrapServers()), new StringSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>(KafkaTopics.DOWNLOAD_REQUESTS, jobId.toString(),
                    "{\"jobId\":\"" + jobId + "\",\"videoId\":\"abc\",\"url\":\"https://youtube.com/watch?v=abc\",\"filename\":\"abc.mp4\"}"))
                    .get();
        }
    }
}
