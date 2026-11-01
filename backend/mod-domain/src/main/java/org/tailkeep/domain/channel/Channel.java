package org.tailkeep.domain.channel;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/** A YouTube channel. {@code id} is null until the channel is saved. */
@Builder(toBuilder = true)
public record Channel(
        UUID id,
        String youtubeId,
        String name,
        String channelUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
