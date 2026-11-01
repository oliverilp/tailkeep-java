package org.tailkeep.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.domain.channel.Channel;
import org.tailkeep.domain.video.FindVideo;
import org.tailkeep.domain.video.SaveVideo;
import org.tailkeep.domain.video.Video;

import lombok.RequiredArgsConstructor;

// Legacy: replaced by the RecordMetadata use case in task 03
@Service
@RequiredArgsConstructor
public class VideoService {
    private final FindVideo findVideo;
    private final SaveVideo saveVideo;

    @Transactional
    public Video createOrUpdateVideo(Metadata metadata, Channel channel) {
        Video video = findVideo.byYoutubeId(metadata.youtubeId())
            .orElseGet(() -> Video.builder().youtubeId(metadata.youtubeId()).build());

        return saveVideo.save(video.toBuilder()
            .channelId(channel.id())
            .url(metadata.url())
            .title(metadata.title())
            .durationString(metadata.durationString())
            .duration(metadata.duration())
            .thumbnailUrl(metadata.thumbnailUrl())
            .description(metadata.description())
            .viewCount(metadata.viewCount())
            .commentCount(metadata.commentCount())
            .filename(metadata.filename())
            .build());
    }
}
