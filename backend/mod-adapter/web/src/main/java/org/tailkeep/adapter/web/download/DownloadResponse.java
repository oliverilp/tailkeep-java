package org.tailkeep.adapter.web.download;

import org.tailkeep.adapter.web.video.VideoResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record DownloadResponse(
        UUID id,
        VideoResponse video,
        String status,
        boolean hasEnded,
        double progress,
        String size,
        String speed,
        String eta,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt) {
}
