package org.tailkeep.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.worker.config.KafkaTopicNames;
import org.tailkeep.worker.download.DownloadProgressMessage;
import org.tailkeep.worker.download.DownloadRequestMessage;
import org.tailkeep.worker.download.DownloadService;

@Component
@Slf4j
public class DownloadQueueListener {
    private final DownloadService downloadService;
    private final KafkaTemplate<String, DownloadProgressMessage> downloadKafkaTemplate;

    public DownloadQueueListener(
            DownloadService downloadService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, DownloadProgressMessage> downloadKafkaTemplate) {
        this.downloadService = downloadService;
        this.downloadKafkaTemplate = downloadKafkaTemplate;
    }

    @KafkaListener(
            topics = KafkaTopicNames.DOWNLOAD_QUEUE,
            groupId = "download-queue-consumer",
            containerFactory = "downloadFactory"
    )
    public void onDownloadJob(DownloadRequestMessage message) {
        log.info("Received download job {}", message);

        try {
            downloadService.processDownload(
                    message.jobId(),
                    message.videoId(),
                    message.url(),
                    message.filename(),
                    this::sendProgressUpdate
            ).thenAccept(this::sendProgressUpdate);

        } catch (Exception e) {
            log.error("Failed to process download job", e);
        }
    }

    private void sendProgressUpdate(DownloadProgressMessage progress) {
        downloadKafkaTemplate.send(KafkaTopicNames.DOWNLOAD_PROGRESS, progress);
    }
}
