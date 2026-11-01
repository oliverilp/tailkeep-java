package org.tailkeep.adapter.web.video;

import org.tailkeep.adapter.web.channel.ChannelResponseMapper;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.Video;
import org.tailkeep.domain.video.VideoDetails;
import org.tailkeep.domain.video.VideoWithChannel;

public final class VideoResponseMapper {
    private VideoResponseMapper() {
    }

    public static VideoResponse toResponse(VideoWithChannel videoWithChannel) {
        return toResponse(videoWithChannel.video(), videoWithChannel.channel());
    }

    public static VideoResponse toResponse(Video video, Channel channel) {
        return new VideoResponse(
                video.id(),
                video.youtubeId(),
                ChannelResponseMapper.toResponse(channel),
                video.url(),
                video.title(),
                video.durationString(),
                video.duration(),
                video.thumbnailUrl(),
                video.description(),
                video.viewCount(),
                video.commentCount(),
                video.filename(),
                video.createdAt(),
                video.updatedAt());
    }

    public static VideoDetailResponse toDetailResponse(VideoDetails details) {
        Video video = details.video();
        return new VideoDetailResponse(
                video.id(),
                video.youtubeId(),
                ChannelResponseMapper.toResponse(details.channel()),
                video.url(),
                video.title(),
                video.durationString(),
                video.duration(),
                video.thumbnailUrl(),
                video.description(),
                video.viewCount(),
                video.commentCount(),
                video.filename(),
                video.createdAt(),
                video.updatedAt(),
                details.doneDownloading());
    }
}
