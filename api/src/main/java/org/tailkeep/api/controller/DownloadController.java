package org.tailkeep.api.controller;

import java.util.UUID;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.model.MetadataRequestMessage;

@RestController
@RequestMapping("api/v1/messages")
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DownloadController {
    private final KafkaTemplate<String, MetadataRequestMessage> kafkaTemplate;

    public DownloadController(
            KafkaTemplate<String, MetadataRequestMessage> messageRequestKafkaTemplate) {
        this.kafkaTemplate = messageRequestKafkaTemplate;
    }

    @PostMapping
    public void publish(@RequestBody MetadataRequestMessage request) throws Exception {
        String jobId = UUID.randomUUID().toString();

        MetadataRequestMessage enrichedRequest = new MetadataRequestMessage(jobId, request.url());
        kafkaTemplate.send(KafkaTopicNames.METADATA_QUEUE, enrichedRequest);
    }
}
