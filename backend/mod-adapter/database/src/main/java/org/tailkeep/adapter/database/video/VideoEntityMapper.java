package org.tailkeep.adapter.database.video;

import org.tailkeep.adapter.database.channel.ChannelEntity;
import org.tailkeep.domain.video.model.Video;

public final class VideoEntityMapper {
    private VideoEntityMapper() {
    }

    public static Video toDomain(VideoEntity entity) {
        return Video.builder()
                .id(entity.getId())
                .youtubeId(entity.getYoutubeId())
                .channelId(entity.getChannel().getId())
                .url(entity.getUrl())
                .title(entity.getTitle())
                .durationString(entity.getDurationString())
                .duration(entity.getDuration())
                .thumbnailUrl(entity.getThumbnailUrl())
                .description(entity.getDescription())
                .viewCount(entity.getViewCount())
                .commentCount(entity.getCommentCount())
                .filename(entity.getFilename())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /** The channel is passed in because a domain video only knows its ID. */
    public static VideoEntity toEntity(Video video, ChannelEntity channel) {
        return VideoEntity.builder()
                .id(video.id())
                .youtubeId(video.youtubeId())
                .channel(channel)
                .url(video.url())
                .title(video.title())
                .durationString(video.durationString())
                .duration(video.duration())
                .thumbnailUrl(video.thumbnailUrl())
                .description(video.description())
                .viewCount(video.viewCount())
                .commentCount(video.commentCount())
                .filename(video.filename())
                .createdAt(video.createdAt())
                .updatedAt(video.updatedAt())
                .build();
    }
}
