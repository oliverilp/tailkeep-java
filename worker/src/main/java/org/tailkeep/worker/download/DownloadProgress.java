package org.tailkeep.worker.download;

public record DownloadProgress(
        String videoId,
        String jobId,
        String status,
        double progress,
        String size,
        String speed,
        String eta) {
}
