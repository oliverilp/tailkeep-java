package org.tailkeep.adapter.messaging.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.tailkeep.adapter.messaging.handler.DownloadBroadcastHandler;
import org.tailkeep.adapter.messaging.handler.DownloadProgressHandler;
import org.tailkeep.adapter.messaging.handler.DownloadRequestHandler;
import org.tailkeep.adapter.messaging.handler.MetadataRequestHandler;
import org.tailkeep.adapter.messaging.handler.MetadataResultHandler;
import org.tailkeep.adapter.messaging.publisher.MessagingPublishers;
import org.tailkeep.domain.download.FetchMetadata;
import org.tailkeep.domain.download.PushDownloadProgress;
import org.tailkeep.domain.download.RecordDownloadProgress;
import org.tailkeep.domain.download.RecordMetadata;
import org.tailkeep.domain.download.RunDownload;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One app instance for the delivery tests: the Kafka adapter with its handlers, over use cases that only record
 * what they were called with. Two of them make two replicas.
 */
final class TestInstance implements AutoCloseable {
    final List<UUID> fetchedMetadata = new CopyOnWriteArrayList<>();
    final List<UUID> downloaded = new CopyOnWriteArrayList<>();
    final List<UUID> pushed = new CopyOnWriteArrayList<>();
    final AtomicInteger metadataAttempts = new AtomicInteger();

    private final boolean blockDownloads;
    private final int failedMetadataAttempts;
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    private TestInstance(boolean blockDownloads, int failedMetadataAttempts) {
        this.blockDownloads = blockDownloads;
        this.failedMetadataAttempts = failedMetadataAttempts;
    }

    static TestInstance start(String bootstrapServers) {
        return start(bootstrapServers, false, 0);
    }

    /**
     * @param blockDownloads a download prints {@code STARTED <jobId>} and never ends, like one that is killed
     * @param failedMetadataAttempts how many times fetching metadata fails before it succeeds
     */
    static TestInstance start(String bootstrapServers, boolean blockDownloads, int failedMetadataAttempts) {
        TestInstance instance = new TestInstance(blockDownloads, failedMetadataAttempts);
        instance.context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "spring.kafka.bootstrap-servers", bootstrapServers,
                // A killed instance is dropped from its group after this, so the test does not wait for 45 s
                "spring.kafka.consumer.properties.session.timeout.ms", "6000",
                "tailkeep.messaging.kafka.retry-interval", "100ms")));
        instance.context.getBeanFactory().registerSingleton("testInstance", instance);
        instance.context.register(Beans.class);
        instance.context.refresh();
        return instance;
    }

    MessagingPublishers publishers() {
        return context.getBean(MessagingPublishers.class);
    }

    /** Whether this instance has been given partitions of every topic its listeners read. */
    boolean isAssigned(boolean broadcastOnly) {
        return containers().stream()
                .filter(container -> isBroadcast(container) == broadcastOnly)
                .allMatch(container -> !container.getAssignedPartitions().isEmpty());
    }

    /** Whether any of the queue listeners of this instance has a partition. */
    boolean hasQueuePartition() {
        return containers().stream()
                .filter(container -> !isBroadcast(container))
                .anyMatch(container -> !container.getAssignedPartitions().isEmpty());
    }

    private List<MessageListenerContainer> containers() {
        return List.copyOf(context.getBean(KafkaListenerEndpointRegistry.class).getListenerContainers());
    }

    private static boolean isBroadcast(MessageListenerContainer container) {
        return List.of(container.getContainerProperties().getTopics()).contains(KafkaTopics.DOWNLOAD_BROADCAST);
    }

    @Override
    public void close() {
        context.close();
    }

    @Configuration
    @EnableKafka
    @Import({
            KafkaMessagingConfig.class, KafkaMessageListeners.class, KafkaMessagePublisher.class,
            MessagingPublishers.class, MetadataRequestHandler.class, MetadataResultHandler.class,
            DownloadRequestHandler.class, DownloadProgressHandler.class, DownloadBroadcastHandler.class})
    static class Beans {
        @Bean
        KafkaAdmin kafkaAdmin(org.springframework.core.env.Environment environment) {
            return new KafkaAdmin(Map.of(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers")));
        }

        @Bean
        FetchMetadata fetchMetadata(TestInstance instance) {
            return request -> {
                if (instance.metadataAttempts.incrementAndGet() <= instance.failedMetadataAttempts) {
                    throw new IllegalStateException("metadata fetch failed");
                }
                instance.fetchedMetadata.add(request.jobId());
            };
        }

        @Bean
        RecordMetadata recordMetadata() {
            return result -> {
            };
        }

        @Bean
        RunDownload runDownload(TestInstance instance) {
            return request -> {
                if (instance.blockDownloads) {
                    System.out.println("STARTED " + request.jobId());
                    System.out.flush();
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
                instance.downloaded.add(request.jobId());
            };
        }

        @Bean
        RecordDownloadProgress recordDownloadProgress() {
            return update -> null;
        }

        @Bean
        PushDownloadProgress pushDownloadProgress(TestInstance instance) {
            return instance.pushed::add;
        }
    }
}
