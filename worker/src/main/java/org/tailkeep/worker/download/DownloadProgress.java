package org.tailkeep.worker.download;

public record DownloadProgress(
        String videoId,
        long jobId,
        String status,
        double progress,
        String size,
        String speed,
        String eta) {
}
