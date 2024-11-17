package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.dto.DownloadProgressDto;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.service.DownloadService;
import org.tailkeep.api.service.SseService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DownloadProgressListener {
    private final DownloadService downloadService;
    private final SseService sseService;

    public DownloadProgressListener(DownloadService downloadService, SseService sseService) {
        this.downloadService = downloadService;
        this.sseService = sseService;
    }

    @KafkaListener(topics = KafkaTopicNames.DOWNLOAD_PROGRESS, groupId = "download-progress-consumer", containerFactory = "downloadFactory")
    public void listen(DownloadProgressMessage message) {
        log.info("[{} | {}] {}%", message.videoId(), message.jobId(), String.format("%.2f", message.progress()));

        DownloadProgressDto progress;
        if (message.hasEnded()) {
            progress = downloadService.markDownloadComplete(message);
            log.info("Download completed for job: {}", message.jobId());
        } else {
            progress = downloadService.upsertDownloadProgress(message);
        }
        
        sseService.broadcast(progress);
    }
}
