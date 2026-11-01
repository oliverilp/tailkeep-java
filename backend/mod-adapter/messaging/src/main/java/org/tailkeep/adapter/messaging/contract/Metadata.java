package org.tailkeep.adapter.messaging.contract;

public record Metadata(
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
