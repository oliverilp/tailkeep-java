package org.tailkeep.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelDto(
    UUID id,
    String name,
    String youtubeId,
    String channelUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {} 
