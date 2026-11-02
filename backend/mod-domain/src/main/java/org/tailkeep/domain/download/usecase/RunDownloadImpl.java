package org.tailkeep.domain.download.usecase;

import org.tailkeep.domain.download.model.DownloadRequest;
import org.tailkeep.domain.download.model.Job;
import org.tailkeep.domain.download.model.ProgressUpdate;
import org.tailkeep.domain.download.port.FindJob;
import org.tailkeep.domain.download.port.PublishDownloadProgress;
import org.tailkeep.domain.download.port.VideoDownloader;
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
