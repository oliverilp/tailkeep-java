package org.tailkeep.api.dto;

import java.time.LocalDateTime;

public record ChannelDto(
    String id,
    String name,
    String youtubeId,
    String channelUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {} 
