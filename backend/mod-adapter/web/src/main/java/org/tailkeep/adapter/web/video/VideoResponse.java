package org.tailkeep.adapter.web.video;

import org.tailkeep.adapter.web.channel.ChannelResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoResponse(
        UUID id,
        String youtubeId,
        ChannelResponse channel,
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
        LocalDateTime updatedAt) {
}
