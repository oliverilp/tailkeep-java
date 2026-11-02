package org.tailkeep.domain.download.model;

import java.util.UUID;

/** What the downloader reports about a job at one moment. The last update of a download has {@code hasEnded}. */
public record ProgressUpdate(
        String youtubeId,
        UUID jobId,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta) {
}
