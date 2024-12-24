package org.tailkeep.api.integration.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.tailkeep.api.message.MetadataResultMessage;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class KafkaTestHelper {

    private static final String METADATA_RESULT_TOPIC = "metadata-results";
    
    @Autowired
    private KafkaTemplate<String, MetadataResultMessage> kafkaTemplate;

    public void sendMetadataResult(MetadataResultMessage message) {
        try {
            SendResult<String, MetadataResultMessage> result = kafkaTemplate.send(METADATA_RESULT_TOPIC, message)
                .get(5, TimeUnit.SECONDS);
            log.info("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            
            // Add a small delay after sending to ensure the message is processed
            Thread.sleep(100);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka", e);
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }

    public <T> void waitUntil(Supplier<T> condition, T expectedValue) {
        try {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .pollInterval(Duration.ofMillis(100))
                    .pollDelay(Duration.ofMillis(100))
                    .until(() -> {
                        T actualValue = condition.get();
                        log.info("Waiting for condition. Expected: {}, Actual: {}", expectedValue, actualValue);
                        return expectedValue != null && expectedValue.equals(actualValue);
                    });
        } catch (Exception e) {
            log.error("Condition wait failed", e);
            throw e;
        }
    }
}
