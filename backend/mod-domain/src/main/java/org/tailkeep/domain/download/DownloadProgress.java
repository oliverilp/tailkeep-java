package org.tailkeep.domain.download;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * How far the download of a job has come. It belongs to the {@link Job} with the same ID and is only changed
 * through it. Size, speed and ETA are display strings for now.
 */
@Builder(toBuilder = true)
public record DownloadProgress(
        UUID jobId,
        UUID videoId,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt,
        LocalDateTime deletedAt) {

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
