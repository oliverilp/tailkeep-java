package org.tailkeep.adapter.database.video;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tailkeep.adapter.database.channel.ChannelJpaRepository;
import org.tailkeep.adapter.database.download.DownloadProgressRepository;
import org.tailkeep.domain.video.model.Video;
import org.tailkeep.domain.video.model.VideoDownloadCount;
import org.tailkeep.domain.video.port.CountVideoDownloads;
import org.tailkeep.domain.video.port.FindVideo;
import org.tailkeep.domain.video.port.SaveVideo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class VideoDatabaseAdapter implements FindVideo, SaveVideo, CountVideoDownloads {
    private final VideoJpaRepository videos;
    private final ChannelJpaRepository channels;
    private final DownloadProgressRepository downloads;

    public VideoDatabaseAdapter(
            VideoJpaRepository videos, ChannelJpaRepository channels, DownloadProgressRepository downloads) {
        this.videos = videos;
        this.channels = channels;
        this.downloads = downloads;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Video> byId(UUID id) {
        return videos.findById(id).map(VideoEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Video> byYoutubeId(String youtubeId) {
        return videos.findByYoutubeId(youtubeId).map(VideoEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> byIds(Collection<UUID> ids) {
        return videos.findAllById(ids).stream().map(VideoEntityMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Video> newestFirst() {
        return videos.findAllByOrderByCreatedAtDesc().stream().map(VideoEntityMapper::toDomain).toList();
    }

    @Override
    public Video save(Video video) {
        var channel = channels.getReferenceById(video.channelId());
        return VideoEntityMapper.toDomain(videos.saveAndFlush(VideoEntityMapper.toEntity(video, channel)));
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDownloadCount byVideoId(UUID videoId) {
        return new VideoDownloadCount(
                downloads.countByVideoId(videoId),
                downloads.countByVideoIdAndHasEndedTrue(videoId));
    }
}
