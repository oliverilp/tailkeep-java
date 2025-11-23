package org.tailkeep.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelDetailDto(
    UUID id,
    String name,
    String youtubeId,
    String channelUrl,
    Long videoCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

