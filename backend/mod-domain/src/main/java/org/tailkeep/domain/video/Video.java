package org.tailkeep.domain.video;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/** A video. It belongs to a channel, which it refers to by ID. {@code id} is null until it is saved. */
@Builder(toBuilder = true)
public record Video(
        UUID id,
        String youtubeId,
        UUID channelId,
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
