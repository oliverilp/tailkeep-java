package org.tailkeep.adapter.web.video;

import org.tailkeep.adapter.web.channel.ChannelResponse;

import java.time.LocalDateTime;
import java.util.UUID;

/** A {@link VideoResponse} that also says whether every download of the video has ended. */
public record VideoDetailResponse(
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
        LocalDateTime updatedAt,
        boolean doneDownloading) {
}
