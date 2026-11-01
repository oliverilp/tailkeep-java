package org.tailkeep.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DownloadProgressDto(
    UUID id,
    VideoDto video,
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
