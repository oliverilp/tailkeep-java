package org.tailkeep.api.dto;

import org.tailkeep.adapter.web.video.VideoResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public record DownloadProgressDto(
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
    LocalDateTime completedAt
) {} 
