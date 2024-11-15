package org.tailkeep.api.dto;

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
