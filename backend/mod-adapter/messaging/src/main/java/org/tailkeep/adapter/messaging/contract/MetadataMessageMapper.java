package org.tailkeep.adapter.messaging.contract;

import org.tailkeep.domain.download.model.MetadataRequest;
import org.tailkeep.domain.download.model.MetadataResult;
import org.tailkeep.domain.download.model.VideoMetadata;

public final class MetadataMessageMapper {
    private MetadataMessageMapper() {
    }

    public static MetadataRequest toDomain(MetadataRequestMessage message) {
        return new MetadataRequest(message.jobId(), message.url());
    }

    public static MetadataRequestMessage toMessage(MetadataRequest request) {
        return new MetadataRequestMessage(request.jobId(), request.url());
    }

    public static MetadataResult toDomain(MetadataResultMessage message) {
        Metadata m = message.metadata();
        return new MetadataResult(message.jobId(), new VideoMetadata(
                m.youtubeId(), m.url(), m.title(), m.uploader(), m.channelId(), m.channelUrl(),
                m.durationString(), m.duration(), m.thumbnailUrl(), m.description(), m.viewCount(),
                m.commentCount(), m.filename()));
    }

    public static MetadataResultMessage toMessage(MetadataResult result) {
        VideoMetadata m = result.metadata();
        return new MetadataResultMessage(result.jobId(), new Metadata(
                m.youtubeId(), m.url(), m.title(), m.uploader(), m.channelId(), m.channelUrl(),
                m.durationString(), m.duration(), m.thumbnailUrl(), m.description(), m.viewCount(),
                m.commentCount(), m.filename()));
    }
}
