package org.tailkeep.adapter.messaging.contract;

import java.util.UUID;

public record DownloadProgressMessage(
        String videoId,
        UUID jobId,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta) {
}
