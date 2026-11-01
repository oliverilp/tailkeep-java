package org.tailkeep.domain.download;

/** What is known about a video before it is downloaded. */
public record VideoMetadata(
        String youtubeId,
        String url,
        String title,
        String uploader,
        String channelId,
        String channelUrl,
        String durationString,
        double duration,
        String thumbnailUrl,
        String description,
        long viewCount,
        long commentCount,
        String filename) {
}
