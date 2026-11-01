package org.tailkeep.worker.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadRequestMessage;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.domain.download.RunDownload;

@Component
@Slf4j
public class DownloadQueueListener {
    private final RunDownload runDownload;

    public DownloadQueueListener(RunDownload runDownload) {
        this.runDownload = runDownload;
    }

    @KafkaListener(
            topics = KafkaTopicNames.DOWNLOAD_QUEUE,
            groupId = "download-queue-consumer",
            containerFactory = "downloadRequestFactory"
    )
    public void onDownloadJob(DownloadRequestMessage message) {
        log.info("Received download job {}", message);

        try {
            runDownload.execute(MessageMapper.toDomain(message));
        } catch (Exception e) {
            // Legacy: a failed download is dropped, as it was before. Task 05 decides on retries.
            log.error("Failed to process download job", e);
        }
    }
}
