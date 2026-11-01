package org.tailkeep.adapter.web.channel;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String youtubeId,
        String channelUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
