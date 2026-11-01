package org.tailkeep.worker.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.worker.download.YtDlpDownloadService;

@Component
@Slf4j
public class DownloadQueueListener {
    private final YtDlpDownloadService downloadService;
    private final KafkaTemplate<String, DownloadProgressMessage> downloadKafkaTemplate;

    public DownloadQueueListener(
            YtDlpDownloadService downloadService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, DownloadProgressMessage> downloadKafkaTemplate) {
        this.downloadService = downloadService;
        this.downloadKafkaTemplate = downloadKafkaTemplate;
    }

    @KafkaListener(
            topics = KafkaTopicNames.DOWNLOAD_QUEUE,
            groupId = "download-queue-consumer",
            containerFactory = "downloadRequestFactory"
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
