package org.tailkeep.domain.download;

import org.tailkeep.domain.shared.UseCase;

@UseCase
public class RunDownloadImpl implements RunDownload {
    private final FindJob findJob;
    private final VideoDownloader videoDownloader;
    private final PublishDownloadProgress publishDownloadProgress;

    public RunDownloadImpl(
            FindJob findJob,
            VideoDownloader videoDownloader,
            PublishDownloadProgress publishDownloadProgress) {
        this.findJob = findJob;
        this.videoDownloader = videoDownloader;
        this.publishDownloadProgress = publishDownloadProgress;
    }

    @Override
    public void execute(DownloadRequest request) {
        if (findJob.byId(request.jobId()).filter(Job::needsNoDownload).isPresent()) {
            return;
        }

        ProgressUpdate last = videoDownloader.download(request, publishDownloadProgress::publish);
        publishDownloadProgress.publish(last);
    }
}
