package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.dto.DownloadProgressMessage;
import org.tailkeep.api.service.DownloadService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DownloadProgressListener {
    private final DownloadService downloadService;

    public DownloadProgressListener(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @KafkaListener(topics = KafkaTopicNames.DOWNLOAD_PROGRESS, groupId = "download-progress-consumer", containerFactory = "downloadFactory")
    public void listen(DownloadProgressMessage message) {
        log.info("[{} | {}] {}%", message.videoId(), message.jobId(), String.format("%.2f", message.progress()));

        if (message.hasEnded()) {
            downloadService.markDownloadComplete(message);
            log.info("Download completed for job: {}", message.jobId());
        } else {
            downloadService.upsertDownloadProgress(message);
        }
    }
}
