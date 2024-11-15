package org.tailkeep.worker.download;

public record DownloadProgressMessage(
        String videoId,
        String jobId,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta) {
}
