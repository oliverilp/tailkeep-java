package org.tailkeep.adapter.messaging.handler;

import org.springframework.stereotype.Component;
import org.tailkeep.adapter.messaging.contract.DownloadBroadcastMessage;
import org.tailkeep.domain.download.usecase.PushDownloadProgress;

/** Runs on every instance, because each has its own clients. */
@Component
public class DownloadBroadcastHandler {
    private final PushDownloadProgress pushDownloadProgress;

    public DownloadBroadcastHandler(PushDownloadProgress pushDownloadProgress) {
        this.pushDownloadProgress = pushDownloadProgress;
    }

    public void handle(DownloadBroadcastMessage message) {
        pushDownloadProgress.execute(message.jobId());
    }
}
