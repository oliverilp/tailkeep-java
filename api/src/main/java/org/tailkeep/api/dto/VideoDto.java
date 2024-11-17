package org.tailkeep.api.dto;

import java.time.LocalDateTime;

public record VideoDto(
    String id,
    String youtubeId,
    ChannelDto channel,
    String url,
    String title,
    String durationString,
    Double duration,
    String thumbnailUrl,
    String description,
    Long viewCount,
    Long commentCount,
    String filename,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {} 
