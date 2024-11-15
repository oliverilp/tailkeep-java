package org.tailkeep.api.model;

public record DownloadProgress(
        String videoId,
        String jobId,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta) {
}
