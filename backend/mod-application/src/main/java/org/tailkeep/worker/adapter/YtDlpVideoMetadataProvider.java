package org.tailkeep.worker.adapter;

import org.springframework.stereotype.Component;
import org.tailkeep.api.message.Metadata;
import org.tailkeep.domain.download.VideoMetadata;
import org.tailkeep.domain.download.VideoMetadataProvider;
import org.tailkeep.worker.metadata.MetadataFetcher;

// Legacy: the yt-dlp side of VideoMetadataProvider. Task 04 replaces it with the ytdlp adapter.
@Component
public class YtDlpVideoMetadataProvider implements VideoMetadataProvider {
    private final MetadataFetcher metadataFetcher;

    public YtDlpVideoMetadataProvider(MetadataFetcher metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    @Override
    public VideoMetadata fetch(String url) {
        Metadata m = Futures.await(metadataFetcher.fetch(url));
        return new VideoMetadata(m.youtubeId(), m.url(), m.title(), m.uploader(), m.channelId(), m.channelUrl(),
                m.durationString(), m.duration(), m.thumbnailUrl(), m.description(), m.viewCount(),
                m.commentCount(), m.filename());
    }
}
