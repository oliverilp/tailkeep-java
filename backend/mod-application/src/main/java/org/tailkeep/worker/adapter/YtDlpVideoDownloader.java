package org.tailkeep.worker.adapter;

import org.springframework.stereotype.Component;
import org.tailkeep.api.message.MessageMapper;
import org.tailkeep.domain.download.DownloadRequest;
import org.tailkeep.domain.download.ProgressUpdate;
import org.tailkeep.domain.download.VideoDownloader;
import org.tailkeep.worker.download.YtDlpDownloadService;

import java.util.function.Consumer;

// Legacy: the yt-dlp side of VideoDownloader. Task 04 replaces it with the ytdlp adapter.
@Component
public class YtDlpVideoDownloader implements VideoDownloader {
    private final YtDlpDownloadService downloadService;

    public YtDlpVideoDownloader(YtDlpDownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @Override
    public ProgressUpdate download(DownloadRequest request, Consumer<ProgressUpdate> onProgress) {
        return MessageMapper.toDomain(Futures.await(downloadService.processDownload(
                request.jobId(),
                request.youtubeId(),
                request.url(),
                request.filename(),
                message -> onProgress.accept(MessageMapper.toDomain(message)))));
    }
}
