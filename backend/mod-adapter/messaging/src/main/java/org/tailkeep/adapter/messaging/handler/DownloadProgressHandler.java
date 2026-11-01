package org.tailkeep.adapter.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.DownloadMessageMapper;
import org.tailkeep.adapter.messaging.contract.DownloadProgressMessage;
import org.tailkeep.domain.download.RecordDownloadProgress;

@Component
@Slf4j
public class DownloadProgressHandler {
    private final RecordDownloadProgress recordDownloadProgress;

    public DownloadProgressHandler(RecordDownloadProgress recordDownloadProgress) {
        this.recordDownloadProgress = recordDownloadProgress;
    }

    public void handle(DownloadProgressMessage message) {
        log.info("[{} | {}] {}%", message.videoId(), message.jobId(), String.format("%.2f", message.progress()));

        recordDownloadProgress.execute(DownloadMessageMapper.toDomain(message));

        if (message.hasEnded()) {
            log.info("Download completed for job: {}", message.jobId());
        }
    }
}
