package org.tailkeep.adapter.messaging.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.Destination;
import org.tailkeep.adapter.messaging.publisher.MessagePublishException;
import org.tailkeep.adapter.messaging.publisher.MessagePublisher;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnKafkaProvider
public class KafkaMessagePublisher implements MessagePublisher {
    private final KafkaTemplate<String, Object> template;
    private final KafkaMessagingProperties properties;

    public KafkaMessagePublisher(KafkaTemplate<String, Object> messagingKafkaTemplate, KafkaMessagingProperties properties) {
        this.template = messagingKafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(Destination destination, String key, Object message) {
        String topic = KafkaTopics.of(destination);
        try {
            template.send(topic, key, message).get(properties.getSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagePublishException("Interrupted while publishing to " + topic, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new MessagePublishException("Could not publish to " + topic, e);
        }
    }
}
