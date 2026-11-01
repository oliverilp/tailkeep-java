package org.tailkeep.api.message;

import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.MetadataRequest;
import org.tailkeep.domain.download.MetadataResult;
import org.tailkeep.domain.download.ProgressUpdate;
import org.tailkeep.domain.download.VideoMetadata;

// Legacy: translates the wire records to the domain types and back. Task 05 moves this to the messaging adapter.
public final class MessageMapper {
    private MessageMapper() {
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

    public static DownloadRequest toDomain(DownloadRequestMessage message) {
        return new DownloadRequest(message.jobId(), message.videoId(), message.url(), message.filename());
    }

    public static DownloadRequestMessage toMessage(DownloadRequest request) {
        return new DownloadRequestMessage(request.jobId(), request.youtubeId(), request.url(), request.filename());
    }

    public static ProgressUpdate toDomain(DownloadProgressMessage message) {
        return new ProgressUpdate(message.videoId(), message.jobId(), message.status(), message.hasEnded(),
                message.progress(), message.size(), message.speed(), message.eta());
    }

    public static DownloadProgressMessage toMessage(ProgressUpdate update) {
        return new DownloadProgressMessage(update.youtubeId(), update.jobId(), update.status(), update.hasEnded(),
                update.progress(), update.size(), update.speed(), update.eta());
    }
}
