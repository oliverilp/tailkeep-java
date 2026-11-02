package org.tailkeep.adapter.messaging.contract;

import org.tailkeep.domain.download.model.DownloadRequest;
import org.tailkeep.domain.download.model.ProgressUpdate;

/** The wire calls the YouTube ID {@code videoId}, the domain {@code youtubeId}. */
public final class DownloadMessageMapper {
    private DownloadMessageMapper() {
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
