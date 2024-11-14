package org.tailkeep.worker.queue;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.worker.config.KafkaTopicNames;
import org.tailkeep.worker.download.DownloadProgress;
import org.tailkeep.worker.download.DownloadService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DownloadQueueListener {
    private final DownloadService downloadService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public DownloadQueueListener(
            DownloadService downloadService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.downloadService = downloadService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopicNames.DOWNLOAD_QUEUE, groupId = "download-queue-consumer")
    public void onDownloadJob(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String jobId = json.get("jobId").asText();
            String videoId = json.get("videoId").asText();
            String url = json.get("url").asText();
            String filename = json.get("filename").asText();

            downloadService.processDownload(
                    jobId,
                    videoId,
                    url,
                    filename,
                    progress -> sendProgressUpdate(progress)).thenAccept(progress -> sendProgressUpdate(progress));

        } catch (Exception e) {
            log.error("Failed to process download job", e);
        }
    }

    private void sendProgressUpdate(DownloadProgress progress) {
        try {
            String progressJson = objectMapper.writeValueAsString(progress);
            kafkaTemplate.send(KafkaTopicNames.PROGRESS_UPDATES, progressJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize progress", e);
        }
    }
}
