package org.tailkeep.api.message;

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
