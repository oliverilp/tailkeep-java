package org.tailkeep.adapter.messaging.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.tailkeep.domain.download.model.DownloadDetails;
import org.tailkeep.domain.download.model.DownloadProgress;
import org.tailkeep.domain.download.model.MetadataRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** Two app instances against a real Kafka: queues go to one of them, broadcasts to both. */
@Testcontainers
class KafkaDeliveryTest {
    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    private final List<TestInstance> instances = new ArrayList<>();

    @BeforeAll
    static void createTopicsWithTwoPartitions() throws Exception {
        // so that the two instances of a group can each get one
        try (Admin admin = Admin.create(Map.of(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
            admin.createTopics(Stream.of(KafkaTopics.METADATA_REQUESTS, KafkaTopics.DOWNLOAD_PROGRESS)
                    .map(topic -> new NewTopic(topic, 2, (short) 1)).toList()).all().get();
        }
    }

    @AfterEach
    void closeInstances() {
        instances.forEach(TestInstance::close);
    }

    private TestInstance startInstance() {
        TestInstance instance = TestInstance.start(kafka.getBootstrapServers());
        instances.add(instance);
        return instance;
    }

    private void awaitAssigned(TestInstance first, TestInstance second) {
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
            assertThat(first.isAssigned(true)).as("first broadcast").isTrue();
            assertThat(second.isAssigned(true)).as("second broadcast").isTrue();
            assertThat(first.hasQueuePartition() && second.hasQueuePartition()).as("queue split").isTrue();
        });
    }

    @Test
    void aQueueMessageIsHandledByExactlyOneInstance() throws Exception {
        TestInstance first = startInstance();
        TestInstance second = startInstance();
        awaitAssigned(first, second);

        List<UUID> jobIds = Stream.generate(UUID::randomUUID).limit(20).toList();
        jobIds.forEach(jobId -> first.publishers().publish(new MetadataRequest(jobId, "https://youtube.com/watch?v=" + jobId)));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(first.fetchedMetadata.size() + second.fetchedMetadata.size()).isEqualTo(20));
        Thread.sleep(2000);

        List<UUID> handled = new ArrayList<>(first.fetchedMetadata);
        handled.addAll(second.fetchedMetadata);
        assertThat(handled).containsExactlyInAnyOrderElementsOf(jobIds);
    }

    @Test
    void aBroadcastReachesEveryInstance() {
        TestInstance first = startInstance();
        TestInstance second = startInstance();
        awaitAssigned(first, second);
        UUID jobId = UUID.randomUUID();

        first.publishers().broadcast(new DownloadDetails(DownloadProgress.builder().jobId(jobId).build(), null, null));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(first.pushed).containsExactly(jobId);
            assertThat(second.pushed).containsExactly(jobId);
        });
    }

    @Test
    void aMessageWhoseHandlerFailedIsHandledAgain() {
        TestInstance instance = TestInstance.start(kafka.getBootstrapServers(), false, 1);
        instances.add(instance);
        await().atMost(Duration.ofSeconds(60)).until(() -> instance.hasQueuePartition());
        UUID jobId = UUID.randomUUID();

        instance.publishers().publish(new MetadataRequest(jobId, "https://youtube.com/watch?v=" + jobId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(instance.fetchedMetadata).containsExactly(jobId));
        assertThat(instance.metadataAttempts).hasValue(2);
    }
}
