package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.UseCase;

@UseCase
public class RunDownloadImpl implements RunDownload {
    private final VideoDownloader videoDownloader;
    private final PublishDownloadProgress publishDownloadProgress;

    public RunDownloadImpl(VideoDownloader videoDownloader, PublishDownloadProgress publishDownloadProgress) {
        this.videoDownloader = videoDownloader;
        this.publishDownloadProgress = publishDownloadProgress;
    }

    @Override
    public void execute(DownloadRequest request) {
        ProgressUpdate last = videoDownloader.download(request, publishDownloadProgress::publish);
        publishDownloadProgress.publish(last);
    }
}
