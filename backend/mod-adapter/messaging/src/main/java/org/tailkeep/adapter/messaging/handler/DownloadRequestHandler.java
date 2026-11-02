package org.tailkeep.adapter.messaging.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.DownloadMessageMapper;
import org.tailkeep.adapter.messaging.contract.DownloadRequestMessage;
import org.tailkeep.domain.download.usecase.RunDownload;

/** Returns when the download is over, which can take hours. */
@Component
@Slf4j
public class DownloadRequestHandler {
    private final RunDownload runDownload;

    public DownloadRequestHandler(RunDownload runDownload) {
        this.runDownload = runDownload;
    }

    public void handle(DownloadRequestMessage message) {
        log.info("Received download job {}", message);

        runDownload.execute(DownloadMessageMapper.toDomain(message));
    }
}
