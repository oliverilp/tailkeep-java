package org.tailkeep.worker.queue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.tailkeep.worker.command.CommandOutput;

@Service
public class KafkaCallback implements CommandOutput {

    private static final String TOPIC = "tailkeep";

    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaCallback(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onData(String data) {
        System.out.println("Sending to Kafka: " + data);
        kafkaTemplate.send(TOPIC, data);
    }
}
