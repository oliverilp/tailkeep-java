package org.tailkeep.api.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.message.DownloadProgressMessage;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.domain.download.RecordDownloadProgress;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DownloadProgressListener {
    private final RecordDownloadProgress recordDownloadProgress;

    public DownloadProgressListener(RecordDownloadProgress recordDownloadProgress) {
        this.recordDownloadProgress = recordDownloadProgress;
    }

    @KafkaListener(topics = KafkaTopicNames.DOWNLOAD_PROGRESS, groupId = "download-progress-consumer", containerFactory = "downloadProgressFactory")
    public void listen(DownloadProgressMessage message) {
        log.info("[{} | {}] {}%", message.videoId(), message.jobId(), String.format("%.2f", message.progress()));

        recordDownloadProgress.execute(MessageMapper.toDomain(message));

        if (message.hasEnded()) {
            log.info("Download completed for job: {}", message.jobId());
        }
    }
}
