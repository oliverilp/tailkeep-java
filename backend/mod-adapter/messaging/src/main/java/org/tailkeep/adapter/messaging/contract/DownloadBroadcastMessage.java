package org.tailkeep.adapter.messaging.contract;

import java.util.UUID;

/** Tells every instance that the download of a job changed, so each can push it to its own clients. */
public record DownloadBroadcastMessage(UUID jobId) {
}
