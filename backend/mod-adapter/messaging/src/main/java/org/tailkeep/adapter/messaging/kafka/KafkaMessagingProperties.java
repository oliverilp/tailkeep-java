package org.tailkeep.adapter.messaging.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Settings of the Kafka adapter. The connection itself is {@code spring.kafka.*}. */
@Getter
@Setter
@ConfigurationProperties(prefix = "tailkeep.messaging.kafka")
public class KafkaMessagingProperties {
    /** Where a consumer group without a committed offset starts. Queues must not lose what was sent before it joined. */
    private String autoOffsetReset = "earliest";

    /**
     * The longest a download may take. A download keeps its listener thread until it ends, and the consumer must
     * not be dropped from its group meanwhile ({@code max.poll.interval.ms} of the download queue).
     */
    private Duration maxDownloadDuration = Duration.ofHours(24);

    /** How long to wait for the broker to confirm a message. */
    private Duration sendTimeout = Duration.ofSeconds(30);

    /** How often a message is handled before it is skipped. */
    private int maxAttempts = 3;

    /** The wait between two attempts. */
    private Duration retryInterval = Duration.ofSeconds(2);
}
