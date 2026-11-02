package org.tailkeep.domain.download.usecase;

import java.util.UUID;

public interface PushDownloadProgress {
    /**
     * Sends the current state of a download to the clients connected to this instance. Every instance runs it
     * when progress was saved, because a client is connected to one of them. It does nothing when no client is
     * connected or the download has no progress any more.
     */
    void execute(UUID jobId);
}
