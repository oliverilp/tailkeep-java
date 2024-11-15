package org.tailkeep.api.listener;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.tailkeep.api.config.KafkaTopicNames;
import org.tailkeep.api.model.DownloadProgress;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DownloadProgressListener {
    @KafkaListener(topics = KafkaTopicNames.DOWNLOAD_PROGRESS, groupId = "download-progress-consumer", containerFactory = "downloadFactory")
    public void listen(DownloadProgress progress) {
        log.info("[{} | {}] {}%", progress.videoId(), progress.jobId(), String.format("%.2f", progress.progress()));
    }
}
