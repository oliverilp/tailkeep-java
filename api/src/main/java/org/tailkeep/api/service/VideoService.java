package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.api.model.Channel;
import org.tailkeep.api.model.Video;
import org.tailkeep.api.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;

    @Transactional
    public Video createOrUpdateVideo(Metadata metadata, Channel channel) {
        Video video = videoRepository.findByYoutubeId(metadata.youtubeId())
            .orElseGet(() -> {
                Video newVideo = new Video();
                newVideo.setYoutubeId(metadata.youtubeId());
                return newVideo;
            });

        // Update video fields
        video.setChannel(channel);
        video.setUrl(metadata.url());
        video.setTitle(metadata.title());
        video.setDurationString(metadata.durationString());
        video.setDuration(metadata.duration());
        video.setThumbnailUrl(metadata.thumbnailUrl());
        video.setDescription(metadata.description());
        video.setViewCount(metadata.viewCount());
        video.setCommentCount(metadata.commentCount());
        video.setFilename(metadata.filename());

        return videoRepository.save(video);
    }
}
